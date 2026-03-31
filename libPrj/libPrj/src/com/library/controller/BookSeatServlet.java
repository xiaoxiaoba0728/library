package com.library.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;
import com.library.dao.UsersDao;
import com.library.dao.UserDaoImpl;
import com.library.entity.Users;
import com.library.entity.lib_seat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/bookSeat")
public class BookSeatServlet extends HttpServlet {
    private SeatDao seatDao = new SeatDaoImpl();
    private UsersDao usersDao = new UserDaoImpl(); // 引入 UsersDao

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        Map<String, Object> resultMap = new HashMap<>();
        PrintWriter out = response.getWriter();

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String seatId = jsonObject.get("seatId").getAsString();
            String userAccount = jsonObject.get("userAccount").getAsString();

            // =========================================================
            // 🌟 1. 黑名单拦截器：检查账号是否被冻结
            // =========================================================
            Users currentUser = usersDao.getUserByAccount(userAccount);
            
            if (currentUser != null && "frozen".equals(currentUser.getStatus())) {
                long currentTime = System.currentTimeMillis();
                long freezeEndTime = currentUser.getFreezeEndTime() != null ? currentUser.getFreezeEndTime().getTime() : 0;
                
                if (currentTime < freezeEndTime) {
                    // 还在冻结期内，无情拒绝，直接返回！
                    resultMap.put("code", 403);
                    resultMap.put("message", "预约失败！您的账号因多次违规已被冻结，解冻时间：" + currentUser.getFreezeEndTime());
                    out.print(new Gson().toJson(resultMap));
                    out.flush();
                    return; 
                } else {
                    // 冻结期已过，自动帮他解冻！
                    usersDao.unfreezeUser(userAccount);
                    System.out.println("[系统日志] 用户 " + userAccount + " 冻结期已满，自动解冻。");
                }
            }

            // =========================================================
            // 2. 检查该用户是否已经有座位了
            // =========================================================
            lib_seat existingSeat = seatDao.getSeatByUserAccount(userAccount);
            
            if (existingSeat != null && !existingSeat.getSeatId().isEmpty()) {
                resultMap.put("code", 403);
                resultMap.put("message", "预约失败！您当前已拥有座位 [" + existingSeat.getSeatId() + "]。");
            } else {
                // =========================================================
                // 3. 一切正常，执行预约并记录时间
                // =========================================================
                boolean success = seatDao.bookSeatWithTime(seatId, userAccount);
                if (success) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "预约成功！请在30分钟内到馆签到。");
                } else {
                    resultMap.put("code", 500);
                    resultMap.put("message", "预约失败，座位已被抢占。");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", 500);
            resultMap.put("message", "服务器解析异常");
        }

        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}