package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Institution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 寄养机构数据访问接口
 * 继承MyBatis Plus的BaseMapper，提供基本的CRUD操作
 */
@Mapper
public interface InstitutionMapper extends BaseMapper<Institution> {
}
