package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppointmentMapper {
    Appointment selectById(Long id);

    List<Appointment> selectByCondition(Appointment appointment);

    int insert(Appointment appointment);

    int updateById(Appointment appointment);

    int deleteById(Long id);
    int countToday();                      // 今日预约数
    int countPending();                    // 待完成预约数

}
