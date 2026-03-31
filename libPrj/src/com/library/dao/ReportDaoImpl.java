package com.library.dao;

import com.library.entity.Report;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl implements ReportDao {
	@Override
    public boolean updateReportStatus(int id) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // 把对应 id 的举报记录状态改成 handled（已处理）
            String sql = "UPDATE report SET status = 'handled' WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return isSuccess;
    }
	@Override
    public boolean addReport(Report report) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 🌟 第一步：大内密探查名字！拿着前端传来的学号，去用户表里查真名
            String realName = "未知学生"; 
            
            // ⚠️ 极其重要：把这里的 Users 换成你真正的用户表名（如果你用的是 sys_user 就换成 sys_user）
            String querySql = "SELECT name FROM users WHERE account = ?"; 
            pstmt = conn.prepareStatement(querySql);
            pstmt.setString(1, report.getReportedAccount());
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                realName = rs.getString("name"); // 查到真名了！
            }
            
            // 查完名字后，先释放这两个专门用来查询的资源
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();

            // 🌟 第二步：带着查到的真实姓名，正式插入举报记录
            // ⚠️ 这里也是，确保表名 report 是你真实的举报表名
            String insertSql = "INSERT INTO report (reported_account, reported_name, report_type, seat_id, status) VALUES (?, ?, ?, ?, 'pending')";
            pstmt = conn.prepareStatement(insertSql);
            
            pstmt.setString(1, report.getReportedAccount());
            pstmt.setString(2, realName); // 💥 核心魔法：用查到的 realName 替换掉前端传来的"未知"
            pstmt.setString(3, report.getReportType());
            pstmt.setString(4, report.getSeatId());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 第三步：统一关门释放连接
            DBUtil.close(conn, pstmt, rs);
        }
        return isSuccess;
    }
    @Override
    public List<Report> getAllReports() {
        List<Report> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // SQL：查出所有举报，并且把 pending 的排在前面，按时间倒序
            String sql = "SELECT * FROM report ORDER BY status DESC, report_time DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setReportedAccount(rs.getString("reported_account"));
                report.setReportedName(rs.getString("reported_name"));
                report.setReportType(rs.getString("report_type"));
                
                // 数据库里是 DATETIME，取出来转成 String 发给前端
                report.setReportTime(rs.getString("report_time")); 
                report.setSeatId(rs.getString("seat_id"));
                report.setStatus(rs.getString("status"));
                
                list.add(report);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    @Override
    public boolean freezeUserAccount(String account) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();   
            String sql = "UPDATE users SET status = 'frozen' WHERE account = ?";
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
}