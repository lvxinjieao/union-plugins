package com.u8.sdk.ad;

/**
 * 远程图片信息
 */
public class RemoteImageInfo {

    private int width = 0;      //图片宽
    private int height = 0;     //图片高
    private String url;         //图片url地址

    public RemoteImageInfo() {

    }

    public RemoteImageInfo(String url) {
        this.url = url;
    }

    public RemoteImageInfo(String url, int w, int h) {
        this.url = url;
        this.width = w;
        this.height = h;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
