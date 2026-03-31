package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.ReportDao;
import com.library.dao.ReportDaoImpl;
import com.library.entity.Report;

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

// 接收学生前端发来的举报请求
@WebServlet("/api/submitReport")
public class SubmitReportServlet extends HttpServlet {

    private ReportDao reportDao = new ReportDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
    	response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 1. 读取前端发来的 JSON 数据
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // 2. 解析 JSON 数据
        Gson gson = new Gson();
        Map<String, String> requestData = gson.fromJson(sb.toString(), Map.class);
        
        // 3. 把收到的数据装进 Report 实体类里
        Report newReport = new Report();
        newReport.setReportedAccount(requestData.get("reportedAccount"));
        // 简单起见，如果前端不知道姓名，我们就默认填“未知”，或者你后续可以在 DAO 里通过学号联表查询
        newReport.setReportedName(requestData.getOrDefault("reportedName", "未知姓名")); 
        newReport.setReportType(requestData.get("reportType"));
        newReport.setSeatId(requestData.get("seatId"));

        // 4. 调用 DAO 插入数据库
        boolean success = reportDao.addReport(newReport);

        // 5. 返回结果给前端
        Map<String, Object> resultMap = new HashMap<>();
        if (success) {
            resultMap.put("code", 200);
            resultMap.put("message", "举报已提交，感谢您的监督！管理员将尽快处理。");
        } else {
            resultMap.put("code", 400);
            resultMap.put("message", "提交失败，请稍后再试。");
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(resultMap));
        out.flush();
    }
}