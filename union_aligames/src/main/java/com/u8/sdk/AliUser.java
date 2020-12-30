package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class AliUser extends UserAdapter {

	private String[] supportedMethods = {"login","switchLogin","logout","submitExtraData","exit", "queryAntiAddiction"};
	
	public AliUser(Activity context){
		AliSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}
	
	@Override
	public void login() {
		AliSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		AliSDK.getInstance().login();
	}	
	
	@Override
	public void logout() {
		AliSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		switch(extraData.getDataType()){
		case UserExtraData.TYPE_CREATE_ROLE:
		case UserExtraData.TYPE_ENTER_GAME:
		case UserExtraData.TYPE_LEVEL_UP:
			AliSDK.getInstance().submitExtraData(extraData);
			break;
		}
		
	}

	@Override
	public void queryAntiAddiction() {

		//阿里游戏托管了实名认证和限制规则。 这里直接返回18岁， 屏蔽游戏层的实名认证机制
		U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, "18");
	}

	@Override
	public void exit() {
		AliSDK.getInstance().exitSDK();
	}	

}
