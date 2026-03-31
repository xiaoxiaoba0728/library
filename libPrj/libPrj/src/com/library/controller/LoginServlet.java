package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.UsersDao;
import com.library.dao.UserDaoImpl;
import com.library.entity.Users; // 注意：如果你的类名是 Users，这里记得改成 Users

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

// @WebServlet 定义了前端访问这个接口的网址路径
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    // 实例化我们要调用的 Model 层打工人
    private UsersDao userDao = new UserDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");

    	// 1. 设置响应头的编码和格式为 JSON，并允许跨域
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 2. 接收前端发过来的 JSON 格式数据
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // 3. 用 Gson 把前端发来的 JSON 字符串，转换成 Java 的 User 对象
        Gson gson = new Gson();
        Users loginData = gson.fromJson(sb.toString(), Users.class); 

        // 4. 派活：调用 DAO 去数据库里查这个账号密码对不对
        Users dbUser = userDao.getUserByAccountAndPassword(loginData.getAccount(), loginData.getPassword());

        // 5. 准备返回给前端的结果集
        Map<String, Object> resultMap = new HashMap<>();
        
     // 校验：数据库查到了人，并且前端选的角色（student/admin）和数据库里存的一致
        if (dbUser != null && dbUser.getRole().equals(loginData.getRole())) {
            
            // 🌟🌟🌟 新增的封号拦截大闸 🌟🌟🌟
            if ("frozen".equals(dbUser.getStatus())) {
                // 发现账号是冻结状态，直接打回！
                resultMap.put("code", 403); // 403 代表 Forbidden (禁止访问)
                resultMap.put("message", "🚫 您的账号因违规已被冻结，请联系辅导员解封！");
            } else {
                // 如果没有被冻结（normal），才执行原本的登录成功逻辑
                resultMap.put("code", 200);
                resultMap.put("message", "登录成功");
                
                // 把用户的脱敏信息返回给前端
                Map<String, String> userData = new HashMap<>();
                userData.put("name", dbUser.getName());
                userData.put("role", dbUser.getRole());
                userData.put("token", "token_" + System.currentTimeMillis()); 
                
                resultMap.put("data", userData);
            }
            // 🌟🌟🌟 拦截大闸结束 🌟🌟🌟

        } else {
            resultMap.put("code", 400);
            resultMap.put("message", "账号、密码或身份错误");
        }

        // 6. 送客：把 resultMap 转成 JSON 字符串，输出给前端浏览器
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(resultMap));
        out.flush();
    }
}