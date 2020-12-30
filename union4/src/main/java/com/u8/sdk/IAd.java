package com.u8.sdk;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.u8.sdk.ad.INativeAdListener;
import com.u8.sdk.ad.IRewardVideoAdListener;
import com.u8.sdk.ad.IRewardedAdListener;
import com.u8.sdk.ad.NativeAdData;
import com.u8.sdk.base.Constants;
import com.u8.sdk.ad.IAdListener;

import java.util.List;

/**
 * 广告插件接口
 */
public interface IAd extends IPlugin {

    public static final int PLUGIN_TYPE = Constants.PLUGIN_TYPE_AD;

    /**
     * 展示banner广告
     *
     * @param pos      banner广告位展示位置 1：顶部；1：底部
     * @param listener
     */
    public void showBannerAd(int pos, IAdListener listener);

    /**
     * 展示banner广告，自定义view
     *
     * @param containerView
     * @param listener
     */
    public void showBannerAd(ViewGroup containerView, IAdListener listener);

    /**
     * 展示开屏广告
     *
     * @param listener
     */
    public void showSplashAd(IAdListener listener);

    /**
     * 展示插屏广告
     *
     * @param listener
     */
    public void showPopupAd(IAdListener listener);

    /**
     * 展示视频广告
     *
     * @param listener
     */
    public void showVideoAd(IAdListener listener);

    /**
     * 展示激励视频广告
     *
     * @param itemName
     * @param itemNum
     * @param listener
     */
    public void showRewardVideoAd(String itemName, int itemNum, IRewardVideoAdListener listener);

    public void loadRewardVideoAd(String itemName, int itemNum, IRewardedAdListener listener);

    /**
     * 加载原生广告
     *
     * @param listener
     */
    public void loadNativeAd(Activity context, INativeAdListener listener);

    /**
     * 原生广告加载成功，游戏层完成组件渲染之后，调用该方法绑定原生广告展示组件
     *
     * @param containerView
     * @param clickableViews
     * @param dislikeView
     * @return
     */
    public boolean bindAdToView(Activity context, NativeAdData data, ViewGroup containerView, List<View> clickableViews, View dislikeView, IAdListener listener);
}
