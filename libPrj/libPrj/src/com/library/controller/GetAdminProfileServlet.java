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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/admin/getProfile")
public class GetAdminProfileServlet extends HttpServlet {

    private UsersDao usersDao = new UserDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        Map<String, Object> result = new HashMap<>();

        // ===== 照妖镜：打印原始参数，方便排查 =====
        String rawAccount = request.getParameter("account");
        System.out.println("====== GetAdminProfileServlet 被调用 ======");
        System.out.println("原始 account 参数: [" + rawAccount + "]");

        // ===== 参数校验 =====
        if (rawAccount == null || rawAccount.trim().isEmpty()
                || rawAccount.trim().equals("null")
                || rawAccount.trim().equals("undefined")) {
            result.put("code", 400);
            result.put("message", "账号参数无效，请重新登录");
            System.out.println(">> 参数非法，拒绝查询");
        } else {
            String account = rawAccount.trim();
            try {
                Users user = usersDao.getUserByAccount(account);
                System.out.println(">> 查询账号: [" + account + "]，结果: " + (user != null ? "找到" : "未找到"));

                if (user != null) {
                    System.out.println(">> phone=[" + user.getPhone() + "], email=[" + user.getEmail() + "]");
                    result.put("code", 200);
                    result.put("data", user);
                } else {
                    result.put("code", 404);
                    result.put("message", "未找到该账号信息");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.put("code", 500);
                result.put("message", "服务器查询异常");
            }
        }

        String json = new Gson().toJson(result);
        System.out.println(">> 返回给前端的JSON: " + json);
        System.out.println("===========================================");

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}