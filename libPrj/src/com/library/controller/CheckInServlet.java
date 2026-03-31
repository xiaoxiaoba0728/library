package com.library.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;
import com.library.entity.lib_seat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

// 🌟 处理签到入座的接口
@WebServlet("/api/checkIn")
public class CheckInServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();
    // 定义超时时间：30分钟 (毫秒)
    private static final long TIMEOUT_MILLIS = 30 * 60 * 1000;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> resultMap = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String userAccount = jsonObject.get("userAccount").getAsString();

            // 获取该用户当前的座位信息
            lib_seat seat = seatDao.getSeatByUserAccount(userAccount);

            if (seat == null) {
                resultMap.put("code", 404);
                resultMap.put("message", "未找到您的预约记录");
            } else if (!"booked".equals(seat.getStatus())) {
                resultMap.put("code", 400);
                resultMap.put("message", "当前座位状态不是【已预约】，无法签到");
            } else {
                // 🌟 核心逻辑：校验 30 分钟是否超时 (惰性释放策略)
                long currentTime = System.currentTimeMillis();
                long bookTime = seat.getBookTime() != null ? seat.getBookTime().getTime() : 0;
                
                if (bookTime == 0 || (currentTime - bookTime) > TIMEOUT_MILLIS) {
                    // 【已超时】：强制释放座位
                    seatDao.updateSeatStatus(seat.getSeatId(), "free", null, false);
                    resultMap.put("code", 403);
                    resultMap.put("message", "签到失败！已超过30分钟保留期，座位已被系统强制释放。");
                    // TODO: 在第四步中，我们还会在这里加上“记录违约并扣减信用分”的代码
                } else {
                    // 【未超时】：允许签到入座
                    boolean success = seatDao.checkInSeat(seat.getSeatId(), userAccount);
                    if (success) {
                        resultMap.put("code", 200);
                        resultMap.put("message", "签到成功！座位已进入【使用中】状态。祝您学习愉快！");
                    } else {
                        resultMap.put("code", 500);
                        resultMap.put("message", "签到处理异常，请联系管理员");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", 500);
            resultMap.put("message", "服务器解析异常");
        }

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}