package com.example.elderly_Platform.core.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key,String value,long seconds){
        stringRedisTemplate.opsForValue().set(key,value,seconds);
    }
    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }
    public boolean hasKey(String key){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

}
