package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.config.LazyInputStream;
import com.huawei.hms.api.HuaweiMobileServicesUtil;

import java.io.IOException;
import java.io.InputStream;

public class HuaWeiProxyApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        try {
            HuaweiMobileServicesUtil.setApplication(U8SDK.getInstance().getApplication());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProxyAttachBaseContext(Context context) {

        try {
            AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(context);
            config.overlayWith(new LazyInputStream(context) {
                public InputStream get(Context context) {
                    try {
                        return context.getAssets().open("agconnect-services.json");
                    } catch (IOException e) {
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProxyConfigurationChanged(Configuration config) {

    }

    @Override
    public void onProxyTerminate() {

    }
}
