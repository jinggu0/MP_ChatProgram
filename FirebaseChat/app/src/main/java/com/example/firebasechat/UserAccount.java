package com.example.firebasechat;

import java.util.HashMap;
import java.util.Map;

public class UserAccount {
    private String uID;
    private String emailId;
    private String password;
    private String userName;
    private String nickName;

    public UserAccount() {
    }

    public UserAccount(String uId, String emailId, String password, String userName, String nickName) {
        this.uID = uId;
        this.emailId = emailId;
        this.password = password;
        this.userName = userName;
        this.nickName = nickName;
    }

    public String getuID() {
        return uID;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uID", uID);
        result.put("emailId", emailId);
        result.put("password", password);
        result.put("userName", userName);
        result.put("nickName", nickName);

        return result;
    }
}