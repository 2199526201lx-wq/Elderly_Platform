package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RefreshTokenMapper {

    RefreshToken selectById(Long id);

    /**
     * 根据 token 值查询
     */
    RefreshToken selectByToken(@Param("token") String token);

    /**
     * 查询用户的所有有效 Refresh Token
     */
    List<RefreshToken> selectByUserId(@Param("userId") Long userId);

    int insert(RefreshToken refreshToken);

    int updateById(RefreshToken refreshToken);

    /**
     * 根据 token 值删除（登出时使用）
     */
    int deleteByToken(@Param("token") String token);

    /**
     * 删除用户的所有 Refresh Token（强制下线时使用）
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 物理删除已过期的 token（定时清理）
     */
    int deleteExpired();
}
