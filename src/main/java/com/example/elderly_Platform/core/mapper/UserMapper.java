package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByPhone(@Param("phone") String phone);
    int insert(User user);
    int updateById(User user);
    int deleteById(Long id);
    int deductPoints(@Param("id") Long id, @Param("amount") int amount);
    int addPoints(@Param("id") Long id, @Param("amount") int amount);

    int countMembers();
    int countMembersToday();

    List<User> selectByCondition(User user);
}

