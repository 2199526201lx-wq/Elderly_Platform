package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionMapper {
    Question selectById(Long id);

    List<Question> selectByCondition(Question question);

    int insert(Question question);

    int updateById(Question question);

    int deleteById(Long id);
}
