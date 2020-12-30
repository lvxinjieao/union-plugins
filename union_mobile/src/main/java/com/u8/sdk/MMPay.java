package com.u8.sdk;

import android.app.Activity;
import com.u8.sdk.IPay;
import com.u8.sdk.MMSDK;
import com.u8.sdk.PayParams;

public class MMPay implements IPay {

	public Activity activity;

	public MMPay(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void pay(PayParams data) {
		MMSDK.getInstance().pay(this.activity, data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

}
