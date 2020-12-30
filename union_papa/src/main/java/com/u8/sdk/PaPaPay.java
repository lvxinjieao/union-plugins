package com.u8.sdk;

import android.app.Activity;

public class PaPaPay implements IPay{

	public PaPaPay(Activity context){
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		PaPaSDK.getInstance().pay(data);
	}

}
