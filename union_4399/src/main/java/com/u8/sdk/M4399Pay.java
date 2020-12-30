package com.u8.sdk;

import android.app.Activity;

public class M4399Pay implements IPay {

    public M4399Pay(Activity activity) {

    }

    @Override
    public void pay(PayParams data) {
        M4399SDK.getInstance().pay(data);
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

}
