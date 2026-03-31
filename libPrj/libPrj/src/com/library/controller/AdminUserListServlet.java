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
import java.util.List;
import java.util.Map;

// 专门给管理员加载用户列表的接口
@WebServlet("/api/admin/users")
public class AdminUserListServlet extends HttpServlet {

    private UsersDao usersDao = new UserDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 防止中文乱码的三板斧
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 1. 调用 DAO 查询所有学生
        List<Users> studentList = usersDao.getAllStudents();

        // 2. 封装返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200);
        resultMap.put("message", "查询成功");
        resultMap.put("data", studentList);

        // 3. 转换为 JSON 并输出给前端
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}