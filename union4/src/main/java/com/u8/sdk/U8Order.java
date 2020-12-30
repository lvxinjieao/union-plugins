package com.u8.sdk;

/**
 * 单机游戏使用，订单信息
 */
public class U8Order {

    private String orderId;
    private String productId;
    private String productName;
    private String extension;

    public U8Order(String orderId, String productId, String productName, String extension) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.extension = extension;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
