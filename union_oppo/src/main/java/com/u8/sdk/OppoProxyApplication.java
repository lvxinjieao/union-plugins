package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;

public class OppoProxyApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        OppoSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams(), U8SDK.getInstance().getApplication());
    }

    @Override
    public void onProxyAttachBaseContext(Context base) {
    }

    @Override
    public void onProxyConfigurationChanged(Configuration config) {
    }

    @Override
    public void onProxyTerminate() {
    }


}
