package com.u8.sdk.ad;

import java.util.List;

public interface INativeAdListener extends IAdListener{

    /**
     * 广告加载成功
     */
    public void onNativeAdLoaded(NativeAdData ad);

}
