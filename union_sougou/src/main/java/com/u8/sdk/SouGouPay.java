package com.u8.sdk;

import android.app.Activity;

public class SouGouPay implements IPay{

	public SouGouPay(Activity context){
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		SouGouSDK.getInstance().pay(data);
	}

}
