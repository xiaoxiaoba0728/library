package com.library.entity;
import java.sql.Timestamp;

public class lib_seat {
	// 1. 属性定义（与数据库 lib_seat 表字段对应）
    private String seatId;       // 对应 seat_id，如 A-203
    private String floor;        // 对应 floor，如 1F
    private String area;         // 对应 area，如 A区
    private String status;       // 对应 status，如 free, booked, used 等
    private String userAccount;  // 对应 user_account，当前占用/预约人的学号
    private Timestamp bookTime;
    private Timestamp leaveTime;

    //private Date updateTime;     // 对应 update_time，状态最后更新时间

    // 2. 无参构造函数（框架在反射创建对象时往往需要用到）
    public  lib_seat () {
    }

    // 3. Getter 和 Setter 方法
    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }
    public Timestamp getBookTime() {
        return bookTime;
    }

    public void setBookTime(Timestamp bookTime) {
        this.bookTime = bookTime;
    }
   
    public Timestamp getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Timestamp leaveTime) {
        this.leaveTime = leaveTime;
    }
//    public Date getUpdateTime() {
//        return updateTime;
//    }
//
//    public void setUpdateTime(Date updateTime) {
//        this.updateTime = updateTime;
//    }
    
    // 可选：重写 toString() 方法，方便在控制台打印测试数据时查看座位详情
    @Override
    public String toString() {
        return "lib_seat{" +
                "seatId='" + seatId + '\'' +
                ", floor='" + floor + '\'' +
                ", area='" + area + '\'' +
                ", status='" + status + '\'' +
                ", userAccount='" + userAccount + '\'' +
                ", bookTime=" + bookTime +
                '}';
    }
}
