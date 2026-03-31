package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    // 数据库连接配置 (请根据你自己的 MySQL 账号密码进行修改)
    private static final String URL = "jdbc:mysql://localhost:3306/library_seat_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root"; // 你的 MySQL 用户名
    private static final String PASSWORD = "zxy200674@"; // 你的 MySQL 密码

    // 静态代码块：确保驱动只加载一次
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 8.0 驱动
            // 如果是 MySQL 5.7，用 "com.mysql.jdbc.Driver"
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("数据库驱动加载失败，请检查是否导入了 jar 包！");
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 释放资源（用完数据库必须随手关门）
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}