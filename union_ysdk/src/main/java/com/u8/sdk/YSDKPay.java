package com.u8.sdk;

import android.app.Activity;

public class YSDKPay implements IAdditionalPay {

    public YSDKPay(Activity activity) {
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    @Override
    public void pay(PayParams data) {
        YSDK.getInstance().pay(data);
    }

    @Override
    public void checkFailedOrder(PayParams data) {
    }

    @Override
    public boolean needQueryResult() {
        return true;
    }

}
