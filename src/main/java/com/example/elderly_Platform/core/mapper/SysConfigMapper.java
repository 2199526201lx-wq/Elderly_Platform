package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigMapper {
    SysConfig selectById(Long id);

    List<SysConfig> selectByCondition(SysConfig config);

    int insert(SysConfig config);

    int updateById(SysConfig config);

    int deleteById(Long id);
}
