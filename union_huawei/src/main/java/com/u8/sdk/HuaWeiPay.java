package com.u8.sdk;

import android.app.Activity;

public class HuaWeiPay implements IAdditionalPay {

    public HuaWeiPay(Activity activity) {

    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    @Override
    public void pay(PayParams data) {
        HuaWeiSDK.getInstance().pay(data);
    }

    @Override
    public void checkFailedOrder(PayParams data) {

    }

    @Override
    public boolean needQueryResult() {
        return true;
    }
}
