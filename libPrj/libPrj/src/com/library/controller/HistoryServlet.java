package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;

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

// 查询个人中心历史记录的专属接口
@WebServlet("/api/history")
public class HistoryServlet extends HttpServlet {

    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String account = request.getParameter("account");
        String type = request.getParameter("type"); // book, finish, violation

        Map<String, Object> resultMap = new HashMap<>();

        if (account != null && type != null) {
            List<Map<String, String>> historyList = seatDao.getUserHistory(account, type);
            resultMap.put("code", 200);
            resultMap.put("message", "查询成功");
            resultMap.put("data", historyList);
        } else {
            resultMap.put("code", 400);
            resultMap.put("message", "参数缺失");
        }

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}