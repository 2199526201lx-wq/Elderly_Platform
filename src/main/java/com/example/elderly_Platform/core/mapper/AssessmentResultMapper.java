package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.AssessmentResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssessmentResultMapper {
    AssessmentResult selectById(Long id);

    List<AssessmentResult> selectByCondition(AssessmentResult result);

    int insert(AssessmentResult result);

    int updateById(AssessmentResult result);

    int deleteById(Long id);
}
