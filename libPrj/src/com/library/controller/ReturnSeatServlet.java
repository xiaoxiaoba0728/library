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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/returnSeat")
public class ReturnSeatServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();
    // 暂离超时时间：30分钟
    private static final long LEAVE_TIMEOUT_MILLIS = 30 * 60 * 1000;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        Map<String, Object> resultMap = new HashMap<>();

        try {
            JsonObject jsonObject = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String userAccount = jsonObject.get("userAccount").getAsString();
            String scannedSeatId = jsonObject.get("seatId").getAsString(); // 模拟扫码拿到的座位号

            lib_seat seat = seatDao.getSeatByUserAccount(userAccount);

            if (seat == null || !"temporary".equals(seat.getStatus())) {
                resultMap.put("code", 400);
                resultMap.put("message", "回归失败，您当前没有处于暂离状态的座位");
            } else if (!seat.getSeatId().equals(scannedSeatId)) {
                resultMap.put("code", 403);
                resultMap.put("message", "二维码不匹配，这不是您预约的座位");
            } else {
                long currentTime = System.currentTimeMillis();
                long leaveTime = seat.getLeaveTime() != null ? seat.getLeaveTime().getTime() : 0;

                // 校验是否超过 30 分钟
                if (leaveTime == 0 || (currentTime - leaveTime) > LEAVE_TIMEOUT_MILLIS) {
                    // 超时了，无情释放
                    seatDao.updateSeatStatus(seat.getSeatId(), "free", null, false);
                    resultMap.put("code", 403);
                    resultMap.put("message", "回归失败！已超过30分钟，座位已被系统释放。");
                } else {
                    // 没超时，正常回归
                    boolean success = seatDao.returnSeat(seat.getSeatId(), userAccount);
                    if (success) {
                        resultMap.put("code", 200);
                        resultMap.put("message", "回归成功，恢复使用");
                    } else {
                        resultMap.put("code", 500);
                        resultMap.put("message", "系统异常");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", 500);
            resultMap.put("message", "解析异常");
        }

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}