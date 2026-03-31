package com.library.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;

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

// 专门处理 入座、暂离、恢复 的万能接口
@WebServlet("/api/seatAction")
public class SeatActionServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> resultMap = new HashMap<>();

        try {
            // 解析前端传来的 JSON 数据
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            
            String seatId = jsonObject.get("seatId").getAsString();
            String action = jsonObject.get("action").getAsString(); // checkIn, tempLeave, resume
            String account = jsonObject.get("account").getAsString();

            // 🌟 核心：根据动作类型，决定数据库里应该改成什么状态
            String targetStatus = "";
            boolean isFirstCheckIn = false; // 🌟 新增标志位：判断是否是真正的第一次扫码签到

            if ("checkIn".equals(action)) {
                targetStatus = "occupied"; // 签到入座 -> 使用中
                isFirstCheckIn = true;     // 标记为首次入座，通知 DAO 记录时间！
            } else if ("tempLeave".equals(action)) {
                targetStatus = "temporary"; // 暂离 -> 暂离中
            } else if ("resume".equals(action)) {
                targetStatus = "occupied"; // 恢复入座 -> 回到使用中
            }

            if (!targetStatus.isEmpty()) {
                // 🌟 注意：这里给 updateSeatStatus 多传了一个 isFirstCheckIn 参数
                boolean success = seatDao.updateSeatStatus(seatId, targetStatus, account, isFirstCheckIn);
                
                if (success) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "操作成功");
                } else {
                    resultMap.put("code", 500);
                    resultMap.put("message", "操作失败，请重试");
                }
            } else {
                resultMap.put("code", 400);
                resultMap.put("message", "未知的动作类型");
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