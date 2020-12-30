package com.u8.sdk.impl.listeners;

public interface ISDKPayListener {
	
	public void onSuccess(String content);

	public void onFailed(int code);
}
