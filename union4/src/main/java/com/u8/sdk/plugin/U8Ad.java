package com.u8.sdk.plugin;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.u8.sdk.IAd;
import com.u8.sdk.ad.IAdListener;
import com.u8.sdk.ad.INativeAdListener;
import com.u8.sdk.ad.IRewardVideoAdListener;
import com.u8.sdk.ad.IRewardedAdListener;
import com.u8.sdk.ad.NativeAdData;
import com.u8.sdk.base.PluginFactory;
import com.u8.sdk.impl.SimpleDefaultAd;
import com.u8.sdk.impl.SimpleDefaultUser;

import java.util.List;

/**
 * 广告组件调用接口
 */
public class U8Ad {

    private static U8Ad instance;

    private IAd adPlugin;

    private U8Ad(){

    }

    public static U8Ad getInstance(){
        if(instance == null){
            instance = new U8Ad();
        }

        return instance;
    }

    public void init(){
        this.adPlugin = (IAd) PluginFactory.getInstance().initPlugin(IAd.PLUGIN_TYPE);
        if(this.adPlugin == null){
            this.adPlugin = new SimpleDefaultAd();
        }
    }


    /**
     * 展示banner广告
     * @param  pos  banner广告位展示位置 1：顶部；1：底部
     * @param listener
     */
    public void showBannerAd(int pos, IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showBannerAd(pos, listener);
        }
    }

    public void showBannerAd(ViewGroup containerView, IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showBannerAd(containerView, listener);
        }
    }

    public void showSplashAd(IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showSplashAd(listener);
        }
    }

    public void showPopupAd(IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showPopupAd(listener);
        }
    }

    public void showVideoAd(IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showVideoAd(listener);
        }
    }

    public void showRewardVideoAd(String itemName, int itemNum, IRewardVideoAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.showRewardVideoAd(itemName, itemNum, listener);
        }
    }

    public void loadRewardVideoAd(String itemName, int itemNum, IRewardedAdListener listener){
        if(isPluginInited()){
            this.adPlugin.loadRewardVideoAd(itemName, itemNum, listener);
        }
    }

    public void loadNativeAd(Activity context, INativeAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.loadNativeAd(context, listener);
        }
    }

    public boolean bindAdToView(Activity context, NativeAdData data, ViewGroup containerView, List<View> clickableViews, View dislikeView, IAdListener listener) {
        if(isPluginInited()){
            this.adPlugin.bindAdToView(context, data, containerView, clickableViews, dislikeView, listener);
        }
        return false;
    }

    private boolean isPluginInited(){
        if(this.adPlugin == null){
            Log.e("U8SDK", "The ad plugin is not inited or inited failed.");
            return false;
        }
        return true;
    }
}
