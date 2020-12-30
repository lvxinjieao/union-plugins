package com.u8.sdk;

import org.json.JSONObject;

/***
 * 支付参数
 * @author xiaohei
 *
 */
public class PayParams {

    private String productId;
    private String productName;
    private String productDesc;
    private int price;
    private int ratio;    //兑换比例，暂时无用
    private int buyNum;
    private int coinNum;
    private String serverId;
    private String serverName;
    private String roleId;
    private String roleName;
    private int roleLevel;
    private String payNotifyUrl;
    private String vip;
    private String orderID;
    private String extension;

    //单机游戏
    private String channelOrderID;      //渠道订单号/ 作为单机渠道使用
    private int state;                  //订单状态。 单机类型，中间流转使用，游戏层无需使用。 0：下单, 1:支付成功


    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("productId", productId);
            json.put("price", price);
            json.put("orderID", orderID);
            json.put("payNotifyUrl", payNotifyUrl);
            json.put("extension", extension);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(int buyNum) {
        this.buyNum = buyNum;
    }

    public int getCoinNum() {
        return coinNum;
    }

    public void setCoinNum(int coinNum) {
        this.coinNum = coinNum;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public String getPayNotifyUrl() {
        return payNotifyUrl;
    }

    public void setPayNotifyUrl(String payNotifyUrl) {
        this.payNotifyUrl = payNotifyUrl;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getChannelOrderID() {
        return channelOrderID;
    }

    public void setChannelOrderID(String channelOrderID) {
        this.channelOrderID = channelOrderID;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
