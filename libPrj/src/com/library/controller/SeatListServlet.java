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
import java.util.List;
import java.util.Map;

// 这个接口地址专门用来获取座位列表
@WebServlet("/api/seats")
public class SeatListServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 🌟 1. 新增：防止中文乱码（非常重要）
        request.setCharacterEncoding("UTF-8");
        
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 2. 获取前端传来的参数（比如 ?floor=1&area=A）
        String floor = request.getParameter("floor");
        String area = request.getParameter("area");

        // 🌟 3. 修改：默认值与数据库的数据格式保持一致（1 和 A，而不是 1F 和 A区）
        if (floor == null) floor = "1";
        if (area == null) area = "A";

        // 4. 调用 DAO 查数据库
        List<lib_seat> seats = seatDao.getSeatsByFloorAndArea(floor, area);

        // 5. 封装结果并返回 JSON
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200);
        resultMap.put("message", "获取成功");
        resultMap.put("data", seats); // 把整个列表塞进去

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}