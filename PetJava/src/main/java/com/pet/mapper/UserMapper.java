package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 * 继承MyBatis Plus的BaseMapper，提供基本的CRUD操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
