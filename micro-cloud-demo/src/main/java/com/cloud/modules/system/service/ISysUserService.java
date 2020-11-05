package com.cloud.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.common.api.Result;
import com.cloud.modules.system.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {

    /**
     * 密码重置
     * @param username：账号
     * @param oldpassword：旧密码
     * @param newpassword：新密码
     * @param confirmpassword：确认密码
     * @return
     */
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword);

    /**
     * 用户删除
     */
    public boolean deleteUser(String userId);

    /**
     * 批量删除
     */
    public boolean deleteBatchUsers(String userIds);

    /**
     * 账号查用户名
     */
    public SysUser getUserByName(String username);

    /**
     * 检查用户是否有效
     */
    Result checkUserIsEffective(SysUser sysUser);

    /**
     * 保存第三方用户信息
     */
    void saveThirdUser(SysUser sysUser);


    /**
     * 添加用户
     */
    public void addUserWithRole(SysUser user);

}
