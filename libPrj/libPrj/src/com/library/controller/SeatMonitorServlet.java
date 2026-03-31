package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;
import com.library.entity.lib_seat;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/admin/seatMonitor")
public class SeatMonitorServlet extends HttpServlet {
    private SeatDao seatDao = new SeatDaoImpl();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        List<lib_seat> allSeats = seatDao.getAllSeats(); // ²éÈ«²¿
        
        long occupiedCount = allSeats.stream().filter(s -> "occupied".equals(s.getStatus()) || "used".equals(s.getStatus())).count();
        long tempCount = allSeats.stream().filter(s -> "temporary".equals(s.getStatus())).count();
        long bookedCount = allSeats.stream().filter(s -> "booked".equals(s.getStatus())).count();

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("data", allSeats);
        res.put("total", allSeats.size());
        res.put("occupied", occupiedCount);
        res.put("temporary", tempCount);
        res.put("booked", bookedCount);

        resp.getWriter().print(new Gson().toJson(res));
    }
}