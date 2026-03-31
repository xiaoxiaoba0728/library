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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 专门给管理员加载举报列表的接口
@WebServlet("/api/admin/reports")
public class AdminReportServlet extends HttpServlet {

    private ReportDao reportDao = new ReportDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // 1. 调用 DAO 查数据
        List<Report> reportList = reportDao.getAllReports();

        // 2. 打包成 JSON 格式
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200);
        resultMap.put("message", "查询成功");
        resultMap.put("data", reportList);

        // 3. 送客
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(resultMap));
        out.flush();
    }
}