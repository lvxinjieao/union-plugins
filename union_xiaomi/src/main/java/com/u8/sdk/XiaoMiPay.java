package com.u8.sdk;

import android.app.Activity;

public class XiaoMiPay implements IPay {

    public XiaoMiPay(Activity Activity) {
    }

    public void pay(PayParams data) {
        XiaoMiSDK.getInstance().pay(data);
    }

    public boolean isSupportMethod(String methodName) {
        return true;
    }

}
