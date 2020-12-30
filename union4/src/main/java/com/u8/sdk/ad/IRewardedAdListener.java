package com.u8.sdk.ad;

public interface IRewardedAdListener {

    /**
     * 广告加载或者展示失败
     */
    public void onFailed(int code, String msg);

    /**
     * 广告加载成功
     */
    public void onLoaded(RewardedVideoAd ad);

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

    /**
     * 激励视频广告，播放完成，可以发放奖励接口
     */
    public void onReward(String itemName, int count);

}
