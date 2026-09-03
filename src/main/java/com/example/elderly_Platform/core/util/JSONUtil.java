package com.example.elderly_Platform.core.util;


import com.example.elderly_Platform.core.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JSONUtil(){}

    public static String toJson(Object object){
        try{
            return MAPPER.writeValueAsString(object);
        }catch (JsonProcessingException e){
            throw new BusinessException("序列化JSON失败");
        }
    }

    public static <T> T fromJson(String json,Class<T> clazz){
        try{
            return MAPPER.readValue(json,clazz);
        }catch (JsonProcessingException e) {
            throw new BusinessException("反序列化 JSON 失败");
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference){
        try {
            return MAPPER.readValue(json, typeReference);   // ← 填空4：调用哪个方法？
        } catch (JsonProcessingException e) {
            throw new BusinessException("反序列化 JSON 失败");
        }
    }
}
