package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.HealthRecord;
import com.example.elderly_Platform.core.vo.HealthRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthRecordMapper {
    HealthRecord selectHealthyRecordById(Long id);

    int insertHealthyRecord(HealthRecord record);
    int updateHealthyRecordById(HealthRecord record);
    int deleteHealthyRecordById(Long id);

    List<HealthRecord> selectByCondition(HealthRecord record);

    HealthRecordVO selectStats(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}
