package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionnaireMapper {
    Questionnaire selectById(Long id);

    List<Questionnaire> selectByCondition(Questionnaire questionnaire);

    int insert(Questionnaire questionnaire);

    int updateById(Questionnaire questionnaire);

    int deleteById(Long id);
}
