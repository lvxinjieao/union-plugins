package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;

public class XiaoMiProxyApplication implements IApplicationListener {

    @Override
    public void onProxyAttachBaseContext(Context context) {
    }

    @Override
    public void onProxyCreate() {
        XiaoMiSDK.getInstance().initSDK(U8SDK.getInstance().getApplication(), U8SDK.getInstance().getSDKParams());
    }

    @Override
    public void onProxyConfigurationChanged(Configuration paramConfiguration) {
    }

    @Override
    public void onProxyTerminate() {
    }


}
