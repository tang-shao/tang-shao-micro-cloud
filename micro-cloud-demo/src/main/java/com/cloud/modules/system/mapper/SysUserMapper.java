package com.cloud.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据账户 查用户
     */
    public SysUser getUserByName(@Param("username") String username);



}
