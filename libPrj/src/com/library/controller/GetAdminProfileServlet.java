package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.UsersDao;
import com.library.dao.UserDaoImpl;
import com.library.entity.Users;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/admin/getProfile")
public class GetAdminProfileServlet extends HttpServlet {
    private UsersDao usersDao = new UserDaoImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String account = request.getParameter("account");
        Map<String, Object> result = new HashMap<>();
        try {
            Users user = usersDao.getUserByAccount(account);
            if (user != null) {
                result.put("code", 200);
                result.put("data", user); 
            } else {
                result.put("code", 404);
                result.put("message", "未找到该账号信息");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
        }
        response.getWriter().print(new Gson().toJson(result));
    }
}