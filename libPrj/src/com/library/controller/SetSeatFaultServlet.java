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

// 专门用于管理员设置或解除座位故障的接口
@WebServlet("/api/admin/setSeatFault")
public class SetSeatFaultServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> resultMap = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            
            String seatId = jsonObject.get("seatId").getAsString();
            String action = jsonObject.get("action").getAsString(); // setFault (设为故障) 或 clearFault (解除故障)

            // 如果是设为故障，状态变成 closed；如果是解除，状态变回 available
            String targetStatus = "setFault".equals(action) ? "closed" : "available";

            // 🌟 核心：不仅要改状态，还要传入 null 彻底清空可能残留的用户学号
            boolean success = seatDao.updateSeatStatus(seatId, targetStatus, null,false);
            
            if (success) {
                resultMap.put("code", 200);
                resultMap.put("message", "设备状态更新成功");
            } else {
                resultMap.put("code", 500);
                resultMap.put("message", "操作失败，请重试");
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