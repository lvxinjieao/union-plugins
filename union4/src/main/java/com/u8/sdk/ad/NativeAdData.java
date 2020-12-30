package com.u8.sdk.ad;

import java.util.ArrayList;
import java.util.List;

/**
 * 渲染一个广告需要的所有元素
 */
public class NativeAdData {

    private int id;             //原生广告唯一ID

    private String title;       //原生广告的广告标题

    private String description; //原生广告的广告描述

    private String source;      //广告来源

    private RemoteImageInfo icon;     //广告icon

    private List<RemoteImageInfo> imageList;      //广告图片列表

    private int interactionType;        //交互类型 1、点击 2:在浏览器打开网页，3:在app中打开，4:下载应用，5:拨打电话 其它：未知类型

    private int imageMode;          //原生广告图片模式  2小图 3 大图 4 组图 5 视频 其它：未知类型

    private int patternType;        //广告样式  暂时不用


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public RemoteImageInfo getIcon() {
        return icon;
    }

    public void setIcon(RemoteImageInfo icon) {
        this.icon = icon;
    }

    public List<RemoteImageInfo> getImageList() {
        return imageList;
    }

    public void setImageList(List<RemoteImageInfo> imageList) {
        this.imageList = imageList;
    }

    public void setImage(RemoteImageInfo img) {
        if (this.imageList == null) {
            this.imageList = new ArrayList<RemoteImageInfo>();
        }
        if (!this.imageList.contains(img)) {
            this.imageList.add(img);
        }
    }

    public int getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(int interactionType) {
        this.interactionType = interactionType;
    }

    public int getImageMode() {
        return imageMode;
    }

    public void setImageMode(int imageMode) {
        this.imageMode = imageMode;
    }

    public int getPatternType() {
        return patternType;
    }

    public void setPatternType(int patternType) {
        this.patternType = patternType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
