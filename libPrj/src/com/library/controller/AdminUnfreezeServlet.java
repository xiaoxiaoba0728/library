package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.UsersDao;
import com.library.dao.UserDaoImpl;

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

// 专门处理解封账号请求的接口
@WebServlet("/api/admin/unfreeze")
public class AdminUnfreezeServlet extends HttpServlet {

    private UsersDao usersDao = new UserDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 防止中文乱码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 1. 读取前端发来的 JSON 数据（里面包含要解封的学号）
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // 2. 解析拿到学号
        Gson gson = new Gson();
        Map<String, String> requestData = gson.fromJson(sb.toString(), Map.class);
        String account = requestData.get("account");

        // 3. 调用 DAO 执行解封
        boolean success = usersDao.unfreezeUser(account);

        // 4. 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        if (success) {
            resultMap.put("code", 200);
            resultMap.put("message", "账号解封成功！");
        } else {
            resultMap.put("code", 400);
            resultMap.put("message", "解封失败，请检查账号状态");
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(resultMap));
        out.flush();
    }
}