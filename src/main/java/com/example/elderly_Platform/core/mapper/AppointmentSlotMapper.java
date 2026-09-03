package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.AppointmentSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppointmentSlotMapper {
    AppointmentSlot selectById(Long id);

    List<AppointmentSlot> selectByCondition(AppointmentSlot slot);

    int insert(AppointmentSlot slot);

    int updateById(AppointmentSlot slot);

    int deleteById(Long id);

    /**
     * 行锁：锁定这一行，防止两个请求同时预约最后一个名额
     */
    AppointmentSlot selectForUpdate(@Param("id") Long id);

    /**
     * 原子增加人数：current_count = current_count + 1
     */
    int incrementCount(@Param("id") Long id);

    /**
     * 原子减少人数：current_count = current_count - 1
     */
    int decrementCount(@Param("id") Long id);

    /**
     * 根据时段 ID 查套餐价格
     */
    int selectPackagePrice(@Param("slotId") Long slotId);
}
