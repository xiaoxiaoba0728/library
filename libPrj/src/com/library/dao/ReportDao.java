package com.library.dao;

import com.library.entity.Report;
import java.util.List;

public interface ReportDao {
    /**
     * 获取所有的举报记录（管理员上帝视角）
     */
    List<Report> getAllReports();
    /**
     * 处理举报记录：把状态改为已处理
     */
    boolean updateReportStatus(int id);
    /**
     * 新增一条举报记录（供学生端使用）
     */
    boolean addReport(Report report);
    /**
     * 冻结违规用户的账号
     */
    boolean freezeUserAccount(String account);
}