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

@WebServlet("/api/leaveSeat")
public class LeaveSeatServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        Map<String, Object> resultMap = new HashMap<>();

        try {
            JsonObject jsonObject = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String userAccount = jsonObject.get("userAccount").getAsString();

            lib_seat seat = seatDao.getSeatByUserAccount(userAccount);

            if (seat == null || !"used".equals(seat.getStatus())) {
                resultMap.put("code", 400);
                resultMap.put("message", "暂离失败，您当前没有正在使用的座位");
            } else {
                boolean success = seatDao.leaveSeat(seat.getSeatId(), userAccount);
                if (success) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "已暂离，座位将为您保留30分钟");
                } else {
                    resultMap.put("code", 500);
                    resultMap.put("message", "操作失败");
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