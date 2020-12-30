package com.u8.sdk.verify;

public class UOrder {

    private String order;
    private String extension;
    private String productID;        //u8server配置商品映射时，对应映射的商品ID；没有则就是PayParams中的productID

    public UOrder(String order, String ext, String productID) {
        this.order = order;
        this.extension = ext;
        this.productID = productID;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }
}
