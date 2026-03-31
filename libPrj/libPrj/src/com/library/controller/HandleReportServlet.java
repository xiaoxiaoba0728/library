package com.library.controller;

import com.google.gson.Gson;
import com.library.dao.ReportDao;
import com.library.dao.ReportDaoImpl;

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

// 处理违规记录的接口 (动真格版)
@WebServlet("/api/admin/handleReport")
public class HandleReportServlet extends HttpServlet {

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

        // 2. 解析前端传来的 3 个参数
        Gson gson = new Gson();
        Map<String, Object> requestData = gson.fromJson(sb.toString(), Map.class);
        
        int reportId = ((Double) requestData.get("reportId")).intValue();
        String penaltyType = (String) requestData.get("penaltyType"); 
        String account = (String) requestData.get("account"); // 拿到违规学号

        // 3. 第一步：先把这条举报记录的状态改为“已处理”
        boolean success = reportDao.updateReportStatus(reportId);

        // 4. 第二步（动真格）：如果管理员选了“冻结账号”，立刻去封号！
        if (success && "冻结账号".equals(penaltyType)) {
            reportDao.freezeUserAccount(account);
        }

        // 5. 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        if (success) {
            resultMap.put("code", 200);
            resultMap.put("message", "处理成功！");
        } else {
            resultMap.put("code", 400);
            resultMap.put("message", "处理失败，请稍后再试");
        }

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(resultMap));
        out.flush();
    }
}