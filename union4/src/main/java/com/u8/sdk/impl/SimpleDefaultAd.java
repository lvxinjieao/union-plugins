package com.u8.sdk.impl;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.u8.sdk.IAd;
import com.u8.sdk.U8SDK;
import com.u8.sdk.ad.IAdListener;
import com.u8.sdk.ad.INativeAdListener;
import com.u8.sdk.ad.IRewardVideoAdListener;
import com.u8.sdk.ad.IRewardedAdListener;
import com.u8.sdk.ad.NativeAdData;

import java.util.List;

public class SimpleDefaultAd implements IAd {
    @Override
    public void showBannerAd(int pos, IAdListener listener) {
        tip("调用[展示Banner广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void showBannerAd(ViewGroup containerView, IAdListener listener) {
        tip("调用[展示Banner广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void showSplashAd(IAdListener listener) {
        tip("调用[展示开屏广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void showPopupAd(IAdListener listener) {
        tip("调用[展示插屏广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void showVideoAd(IAdListener listener) {
        tip("调用[展示视频广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void showRewardVideoAd(String itemName, int itemNum, IRewardVideoAdListener listener) {
        tip("调用[展示激励视频广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void loadRewardVideoAd(String itemName, int itemNum, IRewardedAdListener listener) {
        tip("调用[加载激励视频广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public void loadNativeAd(Activity context, INativeAdListener listener) {
        tip("调用[加载本地广告]接口成功，还需要经过打包工具来打出最终的广告包");
    }

    @Override
    public boolean bindAdToView(Activity context, NativeAdData data, ViewGroup containerView, List<View> clickableViews, View dislikeView, IAdListener listener) {
        tip("调用[展示本地广告]接口成功，还需要经过打包工具来打出最终的广告包");
        return true;
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    private void tip(String msg) {
        Toast.makeText(U8SDK.getInstance().getContext(), msg, Toast.LENGTH_LONG).show();
    }
}
