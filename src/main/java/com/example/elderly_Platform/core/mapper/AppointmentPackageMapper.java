package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.AppointmentPackage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppointmentPackageMapper {
    AppointmentPackage selectById(Long id);

    List<AppointmentPackage> selectByCondition(AppointmentPackage pkg);

    int insert(AppointmentPackage pkg);

    int updateById(AppointmentPackage pkg);

    int deleteById(Long id);
}
