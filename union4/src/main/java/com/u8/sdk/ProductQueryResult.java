package com.u8.sdk;

/**
 * 商品查询结果
 */
public class ProductQueryResult {

    private String productName;            //商品名称
    private String productID;            //商品ID
    private String price;                //商品价格
    private String currency;            //货币单位

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


}

