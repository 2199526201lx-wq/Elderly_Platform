package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    Message selectById(Long id);

    List<Message> selectByCondition(Message message);

    int insert(Message message);

    int updateById(Message message);

    int deleteById(Long id);
}
