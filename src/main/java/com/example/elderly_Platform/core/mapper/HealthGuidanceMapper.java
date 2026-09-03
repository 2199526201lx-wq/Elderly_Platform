package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.HealthGuidance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthGuidanceMapper {
    HealthGuidance selectById(Long id);

    List<HealthGuidance> selectByCondition(HealthGuidance guidance);

    int insert(HealthGuidance guidance);

    int updateById(HealthGuidance guidance);

    int deleteById(Long id);

    int selectRule(@Param("userId")Long userId,@Param("indicator")String indicator,@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

}
