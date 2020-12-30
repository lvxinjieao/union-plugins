package com.u8.sdk.ad;

/**
 * 广告回调监听接口
 */
public interface IAdListener {

    /**
     * 广告加载或者展示失败
     */
    public void onFailed(int code, String msg);

    /**
     * 广告加载成功
     */
    public void onLoaded();

    /**
     * 广告展示成功
     */
    public void onShow();

    /**
     * 广告被点击
     */
    public void onClicked();

    /**
     * 广告被关闭
     */
    public void onClosed();

    /**
     * 广告点击跳过
     */
    public void onSkip();

}
