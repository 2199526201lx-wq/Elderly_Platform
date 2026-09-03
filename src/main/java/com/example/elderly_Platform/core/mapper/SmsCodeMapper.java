package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.SmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface SmsCodeMapper {

    SmsCode selectById(Long id);

    /**
     * 查询指定手机号 + 验证码的有效记录（未使用且未过期）
     */
    SmsCode selectValid(@Param("phone") String phone, @Param("code") String code);

    /**
     * 查询手机号最近一条验证码
     */
    SmsCode selectLatestByPhone(@Param("phone") String phone);

    int insert(SmsCode smsCode);

    /**
     * 标记验证码已使用
     */
    int markUsed(@Param("id") Long id);

    /**
     * 物理删除过期验证码（该表无 deleted 字段，采用物理删除清理过期数据）
     */
    int deleteExpired();
}
