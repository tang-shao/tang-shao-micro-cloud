package com.cloud.modules.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.api.Result;
import com.cloud.common.constant.CommonConstant;
import com.cloud.modules.system.mapper.SysUserMapper;
import com.cloud.modules.system.service.ISysUserService;
import com.cloud.modules.system.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword) {
        return null;
    }

    @Override
    public boolean deleteUser(String userId) {
        return false;
    }

    @Override
    public boolean deleteBatchUsers(String userIds) {
        return false;
    }

    @Override
    public Result checkUserIsEffective(SysUser sysUser) {
        Result<?> res = new Result<Object>();
        // 根据账户查用户 用户不存在
        if(sysUser == null){
            res.error500("该用户不存在，请注册");
            return res;
        }

        // 该用户已注销
        if(CommonConstant.DEL_FLAG_1.toString().equals(sysUser.getDelFlag())){
            res.error500("该用户已注销");
            return res;
        }

        // 该用户已冻结
        if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
            res.error500("该用户已冻结");
            return res;
        }
        return res;

    }

    @Override
    public SysUser getUserByName(String username) {
        return userMapper.selectOne(new QueryWrapper<SysUser>().lambda()
                .eq(SysUser::getUsername, username));

    }

    @Override
    public void saveThirdUser(SysUser sysUser) {

    }

    @Override
    public void addUserWithRole(SysUser user) {
        this.save(user);
    }
}
