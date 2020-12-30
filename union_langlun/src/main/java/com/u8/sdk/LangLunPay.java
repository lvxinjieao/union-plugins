package com.u8.sdk;

import android.app.Activity;
import com.u8.sdk.IPay;
import com.u8.sdk.LangLunSDK;
import com.u8.sdk.PayParams;

public class LangLunPay implements IPay {

	public Activity activity;

	public LangLunPay(Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		LangLunSDK.getInstance().pay(this.activity, data);
	}

}
