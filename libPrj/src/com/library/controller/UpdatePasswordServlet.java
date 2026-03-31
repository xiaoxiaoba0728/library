package com.library.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.dao.UsersDao;
import com.library.dao.UserDaoImpl;
import com.library.entity.Users;

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

// 🌟 修改密码专属接口
@WebServlet("/api/user/updatePassword")
public class UpdatePasswordServlet extends HttpServlet {

    private UsersDao usersDao = new UserDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> resultMap = new HashMap<>();

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            
            String account = jsonObject.get("account").getAsString();
            String oldPassword = jsonObject.get("oldPassword").getAsString();
            String newPassword = jsonObject.get("newPassword").getAsString();

            // 1. 复用你之前写的登录校验方法，检查原密码是否正确！
            Users user = usersDao.getUserByAccountAndPassword(account, oldPassword);
            
            if (user == null) {
                resultMap.put("code", 400);
                resultMap.put("message", "原密码错误，修改失败！");
            } else {
                // 2. 原密码正确，执行更新
                boolean success = usersDao.updatePassword(account, newPassword);
                if (success) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "密码修改成功，请重新登录！");
                } else {
                    resultMap.put("code", 500);
                    resultMap.put("message", "修改失败，请稍后重试");
                }
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