package com.library.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

// 🌟 注意这里的路径映射，必须和前端 fetch 里的 URL 保持一致！
@WebServlet("/api/admin/updateProfile")
public class AdminUpdateProfileServlet extends HttpServlet {

    private UsersDao usersDao = new UserDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> resultMap = new HashMap<>();

        try {
            // 1. 读取前端传过来的 JSON 数据
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            
            // 🌟 核心修改 1：加上 .trim()，把前端可能传过来的多余空格全部切掉！
            String account = jsonObject.has("account") ? jsonObject.get("account").getAsString().trim() : "";
            String phone = jsonObject.has("phone") ? jsonObject.get("phone").getAsString().trim() : "";
            String email = jsonObject.has("email") ? jsonObject.get("email").getAsString().trim() : "";

            // 🌟 核心修改 2：加入“照妖镜”打印！看看前端到底传了个啥鬼东西过来
            System.out.println("====== 接收到修改个人信息的请求 ======");
            System.out.println("尝试更新的账号(Account): [" + account + "]");
            System.out.println("更新的电话(Phone): [" + phone + "]");
            System.out.println("更新的邮箱(Email): [" + email + "]");
            System.out.println("=======================================");

            // 防止前端缓存没清干净，传过来字符串的 "null" 或 "undefined"
            if (account.isEmpty() || account.equals("null") || account.equals("undefined")) {
                resultMap.put("code", 400);
                resultMap.put("message", "无法识别当前登录账号，请重新登录！");
            } else {
                // 2. 调用 DAO 层执行数据库更新
                boolean success = usersDao.updateAdminProfile(account, phone, email);
                
                // 3. 根据结果返回响应
                if (success) {
                    resultMap.put("code", 200);
                    resultMap.put("message", "个人信息修改成功！");
                    System.out.println(">> 更新成功！");
                } else {
                    resultMap.put("code", 500);
                    resultMap.put("message", "修改失败，请检查账号是否存在");
                    System.out.println(">> 更新失败：在数据库中找不到账号为 " + account + " 的记录！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", 500);
            resultMap.put("message", "服务器解析异常");
        }

        // 输出 JSON 结果给前端
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}