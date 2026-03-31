package com.library.controller;

import com.google.gson.Gson;
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

// 查询“我的当前座位”的接口
@WebServlet("/api/mySeat")
public class MySeatServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 1. 获取前端传来的学号
        String userAccount = request.getParameter("userAccount");

        // 2. 去数据库查询该学号对应的座位
        lib_seat mySeat = seatDao.getSeatByUserAccount(userAccount);

        // 3. 封装并返回 JSON
        Map<String, Object> resultMap = new HashMap<>();
        if (mySeat != null) {
            resultMap.put("code", 200);
            resultMap.put("message", "查询成功");
            
            // 🌟 核心改进：把座位对象转成 Map，方便额外塞入 usedSeconds 字段
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("seatId", mySeat.getSeatId());
            dataMap.put("status", mySeat.getStatus());
            dataMap.put("userAccount", mySeat.getUserAccount());
            
            // 🌟 调用 DAO 获取实时计算的已使用秒数
            // 注意：你需要确保你的 seatDao 中 getUsedSecondsByAccount 方法已经按照我之前的 SQL 写好
            int usedSeconds = seatDao.getUsedSecondsByAccount(userAccount);
            dataMap.put("usedSeconds", usedSeconds); 
            
            resultMap.put("data", dataMap);
        } else {
            resultMap.put("code", 404);
            resultMap.put("message", "当前暂无预约座位");
        }

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}