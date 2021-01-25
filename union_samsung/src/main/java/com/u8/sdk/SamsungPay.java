package com.u8.sdk;

import android.app.Activity;

public class SamsungPay implements IPay {

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	public SamsungPay(Activity activity){}
	
	@Override
	public void pay(PayParams data) {
		SamsungSDK.getInstance().pay(data);
	}

}
