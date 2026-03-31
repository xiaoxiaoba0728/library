package com.library.dao;

import com.library.entity.Users;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 使用 implements 关键字实现刚才的接口
public class UserDaoImpl implements UsersDao {

    @Override
    public Users getUserByAccountAndPassword(String account, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Users user = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM Users WHERE account = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = new Users();
                user.setAccount(rs.getString("account"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return user; 
    }

    @Override
    public List<Users> getAllStudents() {
        List<Users> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM users WHERE role = 'student' ORDER BY account ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Users user = new Users();
                user.setAccount(rs.getString("account"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status")); 
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    @Override
    public List<Users> getAdmin() {
    	List<Users> list = new ArrayList<>();
    	 Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         try {
             conn = DBUtil.getConnection();
             String sql = "SELECT * FROM users WHERE role = 'admin' ORDER BY account ASC";
             pstmt = conn.prepareStatement(sql);
             rs = pstmt.executeQuery();
             
             while (rs.next()) {
                 Users user = new Users();
                 user.setAccount(rs.getString("account"));
                 user.setName(rs.getString("name"));
                 user.setRole(rs.getString("role"));
                 user.setStatus(rs.getString("status")); 
                 list.add(user);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             DBUtil.close(conn, pstmt, rs);
         }
         return list;
    }

    @Override
    public boolean unfreezeUser(String account) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // 一键解封！把状态改回 normal，并清空冻结时间
            String sql = "UPDATE Users SET status = 'normal', freeze_end_time = NULL WHERE account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            
            if (pstmt.executeUpdate() > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return isSuccess;
    }

    // 🌟 补充完整：根据账号获取用户详细信息（包含冻结时间和信用分）
    @Override
    public Users getUserByAccount(String account) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Users user = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM users WHERE account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = new Users();
                user.setAccount(rs.getString("account"));
                user.setName(rs.getString("name"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                user.setCredit(rs.getInt("credit"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setFreezeEndTime(rs.getTimestamp("freeze_end_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return user; 
    }

    // 🌟 新增方法：阶梯式违规惩罚逻辑
    @Override
    public void punishUserForViolation(String account) {
        Connection conn = null;
        PreparedStatement pstmtSelect = null;
        PreparedStatement pstmtUpdate = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            
            // 1. 查出该用户历史总共违规了几次（从 seat_record 账本表查）
            String sqlCount = "SELECT COUNT(*) as v_count FROM seat_record WHERE account = ? AND action_type LIKE 'violation%'";
            pstmtSelect = conn.prepareStatement(sqlCount);
            pstmtSelect.setString(1, account);
            rs = pstmtSelect.executeQuery();
            
            int violationCount = 0;
            if (rs.next()) {
                violationCount = rs.getInt("v_count");
            }
            
            // 2. 根据违规次数，制定惩罚套餐
            int deductPoints = 5; // 默认扣5分
            int freezeDays = 0;   // 默认不冻结
            
            if (violationCount == 2) {
                deductPoints = 10;
                freezeDays = 1; // 违规2次，冻结1天
            } else if (violationCount >= 3) {
                deductPoints = 20;
                freezeDays = 3; // 违规3次及以上，冻结3天
            }
            
            // 3. 执行惩罚 (写入 Users 表)
            String updateSql;
            if (freezeDays > 0) {
                // 冻结账号：状态改为 'frozen'
                updateSql = "UPDATE Users SET credit = credit - ?, status = 'frozen', freeze_end_time = DATE_ADD(NOW(), INTERVAL ? DAY) WHERE account = ?";
                pstmtUpdate = conn.prepareStatement(updateSql);
                pstmtUpdate.setInt(1, deductPoints);
                pstmtUpdate.setInt(2, freezeDays);
                pstmtUpdate.setString(3, account);
            } else {
                // 仅扣分，不冻结
                updateSql = "UPDATE Users SET credit = credit - ? WHERE account = ?";
                pstmtUpdate = conn.prepareStatement(updateSql);
                pstmtUpdate.setInt(1, deductPoints);
                pstmtUpdate.setString(2, account);
            }
            
            pstmtUpdate.executeUpdate();
            System.out.println("[系统惩罚] 用户 " + account + " 第 " + violationCount + " 次违规，扣除 " + deductPoints + " 分，冻结 " + freezeDays + " 天。");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmtSelect, rs);
            if (pstmtUpdate != null) {
                try { pstmtUpdate.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
 // 🌟 新增方法：仅用于更新密码
    @Override
    public boolean updatePassword(String account, String newPassword) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE Users SET password = ? WHERE account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setString(2, account);

            if (pstmt.executeUpdate() > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return isSuccess;
    }
 // 🌟 实现更新管理员基本信息的方法
    @Override
    public boolean updateAdminProfile(String account, String phone, String email) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // 根据账号更新对应的手机号和邮箱
            String sql = "UPDATE users SET phone = ?, email = ? WHERE account = ?";
            pstmt = conn.prepareStatement(sql);
            
            // 🚨 检查这里！必须严格按照上面的问号顺序来！
            pstmt.setString(1, phone);  // 第 1 个必须是电话
            pstmt.setString(2, email);  // 第 2 个必须是邮箱
            pstmt.setString(3, account); // 第 3 个必须是账号

            if (pstmt.executeUpdate() > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return isSuccess;
    }
}