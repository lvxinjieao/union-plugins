package com.u8.sdk;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.u8.sdk.utils.Logs;


/**
 * 游戏中的AndroidManifest.xml中application节点的android:name属性一定要设置为com.u8.sdk.U8Application
 * 如果游戏需要在application生命周期方法中执行部分操作，那么需要定义一个类去实现IApplicationListener接口
 * 在该接口的实现方法中去完成。
 * 然后在application节点下面建立一个meta-data节点，meta-data节点的name属性为U8_Game_Application
 * value属性就是刚刚实现的类的完整类名
 * 
 * @author xiaohei
 *
 */
public class U8Application extends Application{
	
	public void onCreate(){
		super.onCreate();
		U8SDK.getInstance().onAppCreateAll(this);
		if(isMainProcess(this)){
			U8SDK.getInstance().onAppCreate(this);
		}else{
			Logs.d("U8SDK", "application oncreate called in sub process. do not init again...");
		}
	}
	
	/**
	 * 注意：这个attachBaseContext方法是在onCreate方法之前调用的
	 */
	public void attachBaseContext(Context base){
		super.attachBaseContext(base);
		if(isMainProcess(base)){
			U8SDK.getInstance().onAppAttachBaseContext(this, base);
		}
		
	}
	
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if(isMainProcess(this)){
			U8SDK.getInstance().onAppConfigurationChanged(this, newConfig);
		}
		
	}
	
	public void onTerminate(){
		super.onTerminate();
		if(isMainProcess(this)){
			U8SDK.getInstance().onTerminate();
		}
	}
	
	
	private boolean isMainProcess(Context context){
		try{
			return context.getPackageName().equals(getCurrentProcessName(context));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	private String getCurrentProcessName(Context context) {
	    int pid = android.os.Process.myPid();
	    String processName = "";
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
	        if (process.pid == pid) {
	            processName = process.processName;
	        }
	    }
	    return processName;
	}
	
}
