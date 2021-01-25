package com.u8.sdk;

import android.app.Activity;

public class QuickPay implements IPay {

	public QuickPay(Activity activity){
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		MyQuickSDK.getInstance().pay(data);
	}

}
