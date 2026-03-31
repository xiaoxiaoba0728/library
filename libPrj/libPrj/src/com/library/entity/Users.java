package com.library.entity;

import java.util.Date;

public class Users {
    // 1. 属性定义（与数据库 sys_user 表字段对应）
    private String account;
    private String password;
    private String name;
    private String role;
    private int credit;
    private String status;
    private java.sql.Timestamp freezeEndTime;
    private String phone;
    private String email;
//    private Date createTime;

    // 2. 无参构造函数（必须有）
    public Users() {
    }

    // 3. Getter 和 Setter 方法 
    // (在 Eclipse 中可以右键 -> Source -> Generate Getters and Setters 自动生成)
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
 

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public int getCredit() { 
    	return credit; 
    	}
    public void setCredit(int credit) {
    	this.credit = credit; 
    	}
    
    public java.sql.Timestamp getFreezeEndTime() { return freezeEndTime; }
    public void setFreezeEndTime(java.sql.Timestamp freezeEndTime) { this.freezeEndTime = freezeEndTime; }
    public String getPhone() {
        return phone;
    }
    public String getEmail() {
        return email;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    //    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
}