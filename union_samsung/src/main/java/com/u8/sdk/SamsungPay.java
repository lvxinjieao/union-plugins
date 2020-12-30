package com.u8.sdk;

import android.app.Activity;

public class SamsungPay implements IPay {

	public SamsungPay(Activity context){
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		SamsungSDK.getInstance().pay(data);
	}

}
