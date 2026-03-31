package com.library.controller;

import com.google.gson.Gson;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/userProfile")
public class UserProfileServlet extends HttpServlet {
    private UsersDao usersDao = new UserDaoImpl();
    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String account = request.getParameter("account");
        Map<String, Object> resultMap = new HashMap<>();

        try {
            if (account == null || account.isEmpty()) {
                resultMap.put("code", 400);
                resultMap.put("message", "未提供账号参数");
            } else {
                // 1. 获取用户信息（含信誉分）
                Users user = usersDao.getUserByAccount(account);
                // 2. 获取当前座位信息
                lib_seat seat = seatDao.getSeatByUserAccount(account);

                if (user != null) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "获取成功");
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", user.getName());
                    data.put("account", user.getAccount());
                    data.put("credit", user.getCredit()); // 你的信誉分
                    data.put("status", user.getStatus());
                    
                    if (seat != null && seat.getSeatId() != null) {
                        data.put("seatId", seat.getSeatId());
                        data.put("seatStatus", seat.getStatus());
                        // 传给前端时间戳(毫秒)，前端用来算倒计时
                        data.put("bookTime", seat.getBookTime() != null ? seat.getBookTime().getTime() : null);
                        data.put("leaveTime", seat.getLeaveTime() != null ? seat.getLeaveTime().getTime() : null);
                        // 获取已经使用的秒数（你之前写的极好用的方法）
                        data.put("usedSeconds", seatDao.getUsedSecondsByAccount(account));
                    } else {
                        data.put("seatId", null);
                    }
                    
                    resultMap.put("data", data);
                } else {
                    resultMap.put("code", 404);
                    resultMap.put("message", "用户不存在");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", 500);
            resultMap.put("message", "服务器异常");
        }

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}