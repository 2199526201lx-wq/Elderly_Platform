package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ActivityRegistrationMapper {
    ActivityRegistration selectById(Long id);

    List<ActivityRegistration> selectByCondition(ActivityRegistration registration);

    int insert(ActivityRegistration registration);

    int updateById(ActivityRegistration registration);

    int deleteById(Long id);

    int countToday();

    int updateCheckIn( @Param("checkInTime") LocalDateTime checkInTime, @Param("userId") Long userId, @Param("activityId") Long activityId);

    ActivityRegistration selectByUserAndActivity(@Param("userId")  Long userId, @Param("activityId") Long activityId);

}
