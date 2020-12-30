package com.u8.sdk;

import android.app.Activity;
import com.u8.sdk.IPay;
import com.u8.sdk.NianWanSDK;
import com.u8.sdk.PayParams;

public class NianWanPay implements IPay {

	public Activity activity;

	public NianWanPay(Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		NianWanSDK.getInstance().pay(this.activity, data);
	}

}
