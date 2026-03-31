package com.library.dao;

import com.library.entity.Users;
import java.util.List;

public interface UsersDao {
    // 根据账号密码查询
    Users getUserByAccountAndPassword(String account, String password);
    
    // 获取所有学生
    List<Users> getAllStudents();
    
    List<Users> getAdmin();
    
    // 解冻账号
    boolean unfreezeUser(String account);
    
    // 根据账号查询详情
    Users getUserByAccount(String account);
    
    // 违规惩罚逻辑
    void punishUserForViolation(String account);

    // 🌟 核心修复：必须在这里声明修改密码的方法！
    boolean updatePassword(String account, String newPassword);
 // 🌟 新增：更新用户（管理员）的基本信息
    boolean updateAdminProfile(String account, String phone, String email);
}