package com.library.dao;

import com.library.entity.lib_seat;
import com.library.util.DBUtil; // 注意检查你的包名是 util 还是 utils

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatDaoImpl implements SeatDao {
    
    @Override
    public List<lib_seat> getSeatsByFloorAndArea(String floor, String area) {
        List<lib_seat> seatList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM lib_seat WHERE floor = ? AND area = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, floor);
            pstmt.setString(2, area);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                lib_seat seat = new lib_seat();
                seat.setSeatId(rs.getString("seat_id"));
                seat.setFloor(rs.getString("floor"));
                seat.setArea(rs.getString("area"));
                seat.setStatus(rs.getString("status"));
                seat.setUserAccount(rs.getString("user_account"));
                seat.setLeaveTime(rs.getTimestamp("leave_time"));
                seatList.add(seat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return seatList;
    }

    @Override
    public List<lib_seat> getAllSeats() {
        List<lib_seat> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM lib_seat"; 
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                lib_seat seat = new lib_seat();
                seat.setSeatId(rs.getString("seat_id"));
                seat.setFloor(rs.getString("floor")); // 统一用小写防止报错
                seat.setArea(rs.getString("area"));
                seat.setStatus(rs.getString("status")); 
                list.add(seat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public List<lib_seat> getOccupiedSeats() {
        List<lib_seat> seatList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // 🌟 关键：确保 SQL 语句查了所有字段，或者明确包含了 user_account
            String sql = "SELECT * FROM lib_seat WHERE status IN ('booked', 'used', 'occupied', 'temporary')";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                lib_seat seat = new lib_seat();
                seat.setSeatId(rs.getString("seat_id"));
                seat.setFloor(rs.getString("floor"));
                seat.setArea(rs.getString("area"));
                seat.setStatus(rs.getString("status"));
                // 🌟 核心：这一行绝对不能少！管理员认人全靠它
                seat.setUserAccount(rs.getString("user_account")); 
                
                seatList.add(seat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return seatList;
    }

    @Override
    public lib_seat getSeatByUserAccount(String userAccount) {
        lib_seat seat = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM lib_seat WHERE user_account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userAccount);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                seat = new lib_seat();
                seat.setSeatId(rs.getString("seat_id"));
                seat.setFloor(rs.getString("floor"));
                seat.setArea(rs.getString("area"));
                seat.setStatus(rs.getString("status"));
                seat.setUserAccount(rs.getString("user_account"));
                
                // 🌟 核心修复：把数据库里的时间读取出来塞给实体类！
                // (这样 UserProfileServlet 才能把时间打包发给前端)
                seat.setBookTime(rs.getTimestamp("book_time"));
                seat.setLeaveTime(rs.getTimestamp("leave_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return seat;
    }

    // 修复 1：补全根据座位号查座位的逻辑，退座时需要用它来找原来的主人
    @Override
    public lib_seat getSeatById(String seatId) {
        lib_seat seat = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM lib_seat WHERE seat_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, seatId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                seat = new lib_seat();
                seat.setSeatId(rs.getString("seat_id"));
                seat.setFloor(rs.getString("floor"));
                seat.setArea(rs.getString("area"));
                seat.setStatus(rs.getString("status"));
                seat.setUserAccount(rs.getString("user_account"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return seat;
    }

    @Override
    public boolean updateSeatStatus(String seatId, String status, String userAccount, boolean isFirstCheckIn) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        // 🌟 修复 2：在座位被清空前，先查出这个座位的主人是谁（为了退座时能记账）
        String historyAccount = userAccount;
        if (historyAccount == null || historyAccount.trim().isEmpty()) {
            lib_seat currentSeat = getSeatById(seatId); // 假设你的实体类叫 lib_seat
            if (currentSeat != null) {
                historyAccount = currentSeat.getUserAccount();
            }
        }

        try {
            conn = DBUtil.getConnection();
            String sql;

            // 🌟 核心分流判断：动态决定用哪条 SQL 语句
            if (isFirstCheckIn) {
                // 1. 如果是首次扫码签到，把状态改为使用中，并用 NOW() 记录精确时间
                sql = "UPDATE lib_seat SET status = ?, user_account = ?, check_in_time = NOW() WHERE seat_id = ?";
            } else if (userAccount == null || userAccount.trim().isEmpty()) {
                // 2. 如果是退座(传进来了空值)，不仅要清空账号，还要把时间也设为 NULL 清空
                sql = "UPDATE lib_seat SET status = ?, user_account = ?, check_in_time = NULL WHERE seat_id = ?";
            } else {
                // 3. 只是暂离、恢复或单纯预约，千万别动 check_in_time
                sql = "UPDATE lib_seat SET status = ?, user_account = ? WHERE seat_id = ?";
            }

            pstmt = conn.prepareStatement(sql);
            
            // 第1个问号永远是 status
            pstmt.setString(1, status);
            
            // 第2个问号是 user_account，如果是退座就明确设为 NULL
            if (userAccount == null || userAccount.trim().isEmpty()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, userAccount);
            }
            
            // 第3个问号永远是 seat_id
            pstmt.setString(3, seatId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 🌟 修复 3：智能判断是预约还是退座，然后记账！
                if (historyAccount != null && !historyAccount.trim().isEmpty()) {
                    String actionType = "book"; // 默认是预约
                    // 如果状态变为了 available 或 free，说明是退座
                    if ("available".equals(status) || "free".equals(status)) {
                        actionType = "finish";
                    }
                    // 把真凶（historyAccount）记到账本里
                    addSeatRecord(historyAccount, seatId, actionType);
                }
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
    public List<Map<String, String>> getUserHistory(String account, String type) {
        List<Map<String, String>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "";
            
            if ("violation".equals(type)) {
                sql = "SELECT seat_id, report_type as detail, report_time as time FROM report WHERE reported_account = ? ORDER BY report_time DESC";
            } else {
                sql = "SELECT seat_id, action_type as detail, create_time as time FROM seat_record WHERE account = ? AND action_type = ? ORDER BY create_time DESC";
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            
            if (!"violation".equals(type)) {
                pstmt.setString(2, type);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> record = new HashMap<>();
                record.put("seatId", rs.getString("seat_id"));
                record.put("detail", rs.getString("detail"));
                record.put("time", rs.getString("time"));
                list.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    //  新增的记账小能手 (无需修改，保持原样)
    public void addSeatRecord(String account, String seatId, String actionType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO seat_record (account, seat_id, action_type) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            pstmt.setString(2, seatId);
            pstmt.setString(3, actionType); 
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
    @Override
    public int getUsedSecondsByAccount(String account) {
        int seconds = 0;
        String sql = "SELECT TIMESTAMPDIFF(SECOND, check_in_time, NOW()) as diff FROM lib_seat WHERE user_account = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                seconds = rs.getInt("diff");
                if (rs.wasNull()) seconds = 0; // 如果还没签到，时间就是 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seconds;
    }
 // 实现预约并记录时间的逻辑
    @Override
    public boolean bookSeatWithTime(String seatId, String userAccount) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 状态改为 booked，并且把 book_time 设置为当前时间
            String sql = "UPDATE lib_seat SET status = 'booked', user_account = ?, book_time = NOW() WHERE seat_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userAccount);
            pstmt.setString(2, seatId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 记账（复用你之前的记账逻辑）
                addSeatRecord(userAccount, seatId, "book");
                isSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
        return isSuccess;
    }

    // 实现签到并清空预约时间、开始计时的逻辑
    @Override
    public boolean checkInSeat(String seatId, String userAccount) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 状态转为 used，记录正式入座时间 check_in_time，并把倒计时用的 book_time 置空
            String sql = "UPDATE lib_seat SET status = 'used', check_in_time = NOW(), book_time = NULL WHERE seat_id = ? AND user_account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, seatId);
            pstmt.setString(2, userAccount);
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
    public boolean leaveSeat(String seatId, String userAccount) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 状态改为 temporary，并记录当前时间为 leave_time
            String sql = "UPDATE lib_seat SET status = 'temporary', leave_time = NOW() WHERE seat_id = ? AND user_account = ? AND status = 'used'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, seatId);
            pstmt.setString(2, userAccount);
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
    public boolean returnSeat(String seatId, String userAccount) {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            // 状态恢复为 used，并清空 leave_time
            String sql = "UPDATE lib_seat SET status = 'used', leave_time = NULL WHERE seat_id = ? AND user_account = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, seatId);
            pstmt.setString(2, userAccount);
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
    public void autoReleaseTimeoutSeats() {
        Connection conn = null;
        PreparedStatement pstmtSelect = null;
        PreparedStatement pstmtUpdate = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            
            String updateSql = "UPDATE lib_seat SET status = 'free', user_account = NULL, book_time = NULL, leave_time = NULL, check_in_time = NULL WHERE seat_id = ?";
            pstmtUpdate = conn.prepareStatement(updateSql);

            // ==========================================
            // 1. 抓取【预约未到】的违约者
            // ==========================================
            String sqlBooked = "SELECT seat_id, user_account FROM lib_seat WHERE status = 'booked' AND TIMESTAMPDIFF(MINUTE, book_time, NOW()) >= 30";
            pstmtSelect = conn.prepareStatement(sqlBooked);
            rs = pstmtSelect.executeQuery();
            
            while (rs.next()) {
                String seatId = rs.getString("seat_id");
                String account = rs.getString("user_account");
                
                pstmtUpdate.setString(1, seatId);
                pstmtUpdate.executeUpdate();
                
                // 🌟 修复点 1：把超长的 "violation_book_timeout" 改为 "violation"
                addSeatRecord(account, seatId, "violation");
                System.out.println("[系统巡检] 座位 " + seatId + " 预约超时，已释放。记录用户 " + account + " 违约。");
                
                // 触发阶梯扣分与冻结
                new UserDaoImpl().punishUserForViolation(account);
            }
            rs.close();
            pstmtSelect.close();
            
            // ==========================================
            // 2. 抓取【暂离超时】的违约者
            // ==========================================
            String sqlLeave = "SELECT seat_id, user_account FROM lib_seat WHERE status = 'temporary' AND TIMESTAMPDIFF(MINUTE, leave_time, NOW()) >= 30";
            pstmtSelect = conn.prepareStatement(sqlLeave);
            rs = pstmtSelect.executeQuery();
            
            while (rs.next()) {
                String seatId = rs.getString("seat_id");
                String account = rs.getString("user_account");
                
                pstmtUpdate.setString(1, seatId);
                pstmtUpdate.executeUpdate();
                
                // 🌟 修复点 2：把超长的 "violation_leave_timeout" 改为 "violation"
                addSeatRecord(account, seatId, "violation");
                System.out.println("[系统巡检] 座位 " + seatId + " 暂离超时，已释放。记录用户 " + account + " 违约。");
                
                // 触发阶梯扣分与冻结
                new UserDaoImpl().punishUserForViolation(account);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmtSelect, rs);
            if (pstmtUpdate != null) {
                try { pstmtUpdate.close(); } catch (java.sql.SQLException e) { e.printStackTrace(); }
            }
        }
    }
}