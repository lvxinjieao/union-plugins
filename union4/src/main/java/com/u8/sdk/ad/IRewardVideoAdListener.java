package com.u8.sdk.ad;

public interface IRewardVideoAdListener extends IAdListener{

    /**
     * 激励视频广告，播放完成，可以发放奖励接口
     */
    public void onReward(String itemName, int count);

}
