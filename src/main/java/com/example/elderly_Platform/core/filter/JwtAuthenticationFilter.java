package com.example.elderly_Platform.core.filter;

import com.example.elderly_Platform.core.util.JwtUtil;
import com.example.elderly_Platform.core.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws
            ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Long userId = jwtUtil.getUserId(token);
                String role = jwtUtil.getRole(token);

                // Redis 黑名单 / 密码变更检查（Redis 不可用时跳过，不做校验）
                try {
                    if (redisUtil.hasKey("blacklist:access:" + token)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    String pwChanged = redisUtil.get("pw_changed:" + userId);
                    if (pwChanged != null) {
                        long changedTime = Long.parseLong(pwChanged);
                        if (jwtUtil.getIssuedAt(token).getTime() < changedTime) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            return;
                        }
                    }
                } catch (Exception redisEx) {
                    // Redis 不可用，跳过黑名单和密码变更检查 — 仍允许合法 JWT 正常认证
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // Token 无效/过期，忽略，交给 SecurityConfig 返回 401
            }
        }
        filterChain.doFilter(request,response);
    }

}