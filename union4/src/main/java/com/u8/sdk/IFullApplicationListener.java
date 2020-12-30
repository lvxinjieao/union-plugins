package com.u8.sdk;

public interface IFullApplicationListener extends IApplicationListener{

	//如果应用有多个进程，那么每个进程Application的onCreate都会触发该方法
	public void onProxyCreateAll();
	
}
