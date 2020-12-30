package com.u8.sdk;

import com.u8.sdk.IPay;
import android.app.Activity;
import com.u8.sdk.PayParams;

public class ViVoPay implements IPay {

	public ViVoPay(Activity activity){
	}
	
	@Override
	public void pay(PayParams data) {
		ViVoSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

}
