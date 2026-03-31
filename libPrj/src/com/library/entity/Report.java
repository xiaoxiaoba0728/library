package com.library.entity;

public class Report {
    private int id;
    private String reportedAccount;
    private String reportedName;
    private String reportType;
    private String reportTime; // 为了简单起见，时间我们在实体类里先用 String 接收
    private String seatId;
    private String status;

    // 默认构造函数
    public Report() {}

    // 下面全都是 Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReportedAccount() { return reportedAccount; }
    public void setReportedAccount(String reportedAccount) { this.reportedAccount = reportedAccount; }

    public String getReportedName() { return reportedName; }
    public void setReportedName(String reportedName) { this.reportedName = reportedName; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}