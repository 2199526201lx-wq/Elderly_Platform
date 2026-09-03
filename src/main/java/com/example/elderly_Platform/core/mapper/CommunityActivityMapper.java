package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.CommunityActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityActivityMapper {
    CommunityActivity selectById(Long id);

    List<CommunityActivity> selectByCondition(CommunityActivity activity);

    List<CommunityActivity> selectForMember(CommunityActivity activity);

    int insert(CommunityActivity activity);

    int updateById(CommunityActivity activity);

    int deleteById(Long id);

    CommunityActivity selectForUpdate(Long id);

    int incrementCount(Long id);
}
