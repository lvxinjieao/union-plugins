package com.u8.sdk.verify;

import org.json.JSONObject;

public class UToken {

    private boolean suc;
    private int userID;                    //U8Server中生成的用户唯一ID（对应uuser表中的id）
    private String username;            //U8Server生成的用户名(比如2345435345.uc, 45645645.baidu)
    private String token;                //U8Server返回的会话token，用于游戏服务器去U8Server做二次登录认证使用
    private String sdkUserID;            //当前渠道SDK平台的用户ID（渠道SDK那边返回的）
    private String sdkUsername;            //当前渠道SDK平台的用户名（如果渠道那边没有返回的话，可能为空）
    private String extension;            //扩展数据，部分渠道有一些特殊数据需求，放在这里返回，客户端具体渠道SDK里面进行解析，一般为空。
    private String timestamp;            //时间戳

    public UToken() {
        this.suc = false;
    }

    public UToken(int userID, String sdkUserID, String username, String sdkUsername, String token, String extension, String timestamp) {
        this.userID = userID;
        this.sdkUserID = sdkUserID;
        this.username = username;
        this.sdkUsername = sdkUsername;
        this.token = token;
        this.extension = extension;
        this.timestamp = timestamp;
        this.suc = true;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("suc", suc);
            json.put("userID", userID);
            json.put("sdkUserID", sdkUserID);
            json.put("username", username);
            json.put("sdkUsername", sdkUsername);
            json.put("token", token);
            json.put("extension", extension);
            json.put("timestamp", timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getSdkUserID() {
        return sdkUserID;
    }

    public void setSdkUserID(String sdkUserID) {
        this.sdkUserID = sdkUserID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuc() {
        return suc;
    }

    public void setSuc(boolean suc) {
        this.suc = suc;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSdkUsername() {
        return sdkUsername;
    }

    public void setSdkUsername(String sdkUsername) {
        this.sdkUsername = sdkUsername;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
