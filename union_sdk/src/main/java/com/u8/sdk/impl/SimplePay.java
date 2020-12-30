package com.u8.sdk.impl;

import android.app.Activity;

import com.u8.sdk.IPay;
import com.u8.sdk.PayParams;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8SDK;
import com.u8.sdk.impl.listeners.ISDKPayListener;

public class SimplePay implements IPay {

    public SimplePay(Activity activity) {

    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    @Override
    public void pay(PayParams data) {

        DefaultSDKPlatform.getInstance().pay(U8SDK.getInstance().getContext(), data, new ISDKPayListener() {

            @Override
            public void onSuccess(String content) {
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
            }

            @Override
            public void onFailed(int code) {
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed.");
            }
        });

    }

}
