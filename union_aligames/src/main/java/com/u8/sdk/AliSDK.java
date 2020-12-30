package com.u8.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.OrderInfo;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.uc.gamesdk.*;

import com.u8.sdk.log.Log;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.verify.UToken;

public class AliSDK {

	private static AliSDK instance;
	
	private int gameId;
	private String apiKey;
	private String orientation;
	
	public boolean mRepeatCreate = false;
	
	private enum STATE{
		STATE_NONE,
		STATE_INITING,
		STATE_INITED,
		STATE_LOGIN
	}
	
	private STATE state = STATE.STATE_NONE;
	private boolean loginAfterInit = false;
	
	public static AliSDK getInstance(){
		if(instance == null){
			instance = new AliSDK();
		}
		return instance;
	}
	
	private void parseSDKParams(SDKParams params) {
		this.gameId = params.getInt("UCGameId");
		this.apiKey = params.getString("UCApiKey");
		this.orientation = params.getString("ali_screen_orientation");
	}
	
	private SDKEventReceiver eventReceiver = new SDKEventReceiver(){
		
        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() {
            //初始化成功
        	state = STATE.STATE_INITED;
        	Log.d("U8SDK", "sdk init success");
        	U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
        	
        	if(loginAfterInit){
        		loginAfterInit = false;
        		login();
        	}
        	
        }

        @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
        private void onInitFailed(String data) {
            //初始化失败
        	Log.d("U8SDK", "sdk init failed");
        	state = STATE.STATE_NONE;
        	U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "init failed");
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid) {
        	state = STATE.STATE_LOGIN;
        	Log.d("U8SDK", "sdk login success."+sid);
        	U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, sid);
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
        private void onLoginFailed(String desc) {
        	Log.d("U8SDK", "sdk login failed."+desc);
        	U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed."+desc);
        }

        @Subscribe(event = SDKEventKey.ON_LOGOUT_SUCC)
        private void onLogoutSucc() {
        	U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS,"logout success.");
        }

        @Subscribe(event = SDKEventKey.ON_LOGOUT_FAILED)
        private void onLogoutFailed() {
        	
        }

        @Subscribe(event = SDKEventKey.ON_EXIT_SUCC)
        private void onExit(String desc) {
        	U8SDK.getInstance().getContext().finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        @Subscribe(event = SDKEventKey.ON_EXIT_CANCELED)
        private void onExitCanceled(String desc) {
     
        }	
        
        @Subscribe(event = SDKEventKey.ON_CREATE_ORDER_SUCC)
        private void onCreateOrderSucc(OrderInfo orderInfo) {
        	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
        }

        @Subscribe(event = SDKEventKey.ON_PAY_USER_EXIT)
        private void onPayUserExit(OrderInfo orderInfo) {
        	//U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
        }        
	};
	
	
	
	public void initSDK(SDKParams data){
		try{
	 
			
			Log.d("U8SDK", "sdk init begin");
		   
		   if(state == STATE.STATE_INITING || state == STATE.STATE_INITED){
			   Log.d("U8SDK", "initing or inited. just return");
			   return;
		   }
		   
			state = STATE.STATE_INITING;
			this.parseSDKParams(data);
			
			UCGameSdk.defaultSdk().registerSDKEventReceiver(eventReceiver);
			U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter(){
				
				
				@Override
				public void onDestroy() {
					
//					if(mRepeatCreate){
//						
//						return;
//					}
					
					UCGameSdk.defaultSdk().unregisterSDKEventReceiver(eventReceiver);
				}
			});
			
			ParamInfo gameParamInfo = new ParamInfo();
	        gameParamInfo.setGameId(this.gameId);
	        
	        
	        
			// 设置SDK登录界面为竖屏
	        if("landscape".equalsIgnoreCase(this.orientation)){
	        	gameParamInfo.setOrientation(UCOrientation.LANDSCAPE);
	        }else{
	        	gameParamInfo.setOrientation(UCOrientation.PORTRAIT);
	        }        
	        
	        cn.gundam.sdk.shell.param.SDKParams sdkParams = new cn.gundam.sdk.shell.param.SDKParams();
	        sdkParams.put(SDKParamKey.GAME_PARAMS, gameParamInfo);

	        UCGameSdk.defaultSdk().initSdk(U8SDK.getInstance().getContext(), sdkParams);			
	        Log.d("U8SDK", "sdk init end");
			
		}catch(Exception e){
			state = STATE.STATE_NONE;
			U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void login(){
		try{
			
			if(state.ordinal() < STATE.STATE_INITED.ordinal()){
				
				loginAfterInit = true;
				initSDK(U8SDK.getInstance().getSDKParams());
				return;
			}
			
			Log.d("U8SDK", "sdk now to login");
			UCGameSdk.defaultSdk().login(U8SDK.getInstance().getContext(), null);
			
			Log.d("U8SDK", "sdk login finished.");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private long roleCreateTime = 0;
	
	public void submitExtraData(UserExtraData data){
		

		Log.d("U8SDK", "aligames submitData data:");
		Log.d("U8SDK", "dataType:"+data.getDataType());
		Log.d("U8SDK", "serverId:"+data.getServerID());
		Log.d("U8SDK", "serverName:"+data.getServerName());
		Log.d("U8SDK", "roleCreateTime:"+data.getRoleCreateTime());
		Log.d("U8SDK", "roleLevelUpTime:"+data.getRoleLevelUpTime());
		
		if(data.getDataType() == UserExtraData.TYPE_CREATE_ROLE){
			roleCreateTime = data.getRoleCreateTime();
		}else{
			data.setRoleCreateTime(roleCreateTime);
		}
		
		cn.gundam.sdk.shell.param.SDKParams sdkParams = new cn.gundam.sdk.shell.param.SDKParams();
        sdkParams.put(SDKParamKey.STRING_ROLE_ID, data.getRoleID());
        sdkParams.put(SDKParamKey.STRING_ROLE_NAME, data.getRoleName());
        sdkParams.put(SDKParamKey.LONG_ROLE_LEVEL, TextUtils.isEmpty(data.getRoleLevel()) ? 1 : Long.valueOf(data.getRoleLevel()));
        sdkParams.put(SDKParamKey.STRING_ZONE_ID, data.getServerID()+"");
        sdkParams.put(SDKParamKey.STRING_ZONE_NAME, data.getServerName());
        sdkParams.put(SDKParamKey.LONG_ROLE_CTIME, data.getRoleCreateTime());

        try {
            UCGameSdk.defaultSdk().submitRoleData(U8SDK.getInstance().getContext(), sdkParams);
        } catch (Exception e) {
            e.printStackTrace();
        }	
	}
	
	public void logout(){
		try {
			UCGameSdk.defaultSdk().logout(U8SDK.getInstance().getContext(), null);
		} catch (AliLackActivityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AliNotInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void exitSDK(){
		try {
			UCGameSdk.defaultSdk().exit(U8SDK.getInstance().getContext(), null);
		} catch (AliLackActivityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AliNotInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pay(PayParams data){
		try{
			
			UToken token = U8SDK.getInstance().getUToken();
			if(token == null){
				Toast.makeText(U8SDK.getInstance().getContext(), "登录失效，请重新登录", Toast.LENGTH_SHORT).show();
				return;
			}
			
			JSONObject json = new JSONObject(data.getExtension());
			String payCallbackUrl = json.getString("payCallbackUrl");
//			String sign = json.getString("sign");
//			String accountId = json.getString("accountId");
			
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put(SDKParamKey.CALLBACK_INFO, data.getOrderID());
            //paramMap.put(SDKParamKey.SERVER_ID, "0");
            //paramMap.put(SDKParamKey.ROLE_ID, data.getRoleId());
            //paramMap.put(SDKParamKey.ROLE_NAME, data.getRoleName());
            //paramMap.put(SDKParamKey.GRADE, data.getRoleLevel()+"");
            paramMap.put(SDKParamKey.NOTIFY_URL, payCallbackUrl); //在后台配置
            paramMap.put(SDKParamKey.AMOUNT, data.getPrice()+".00");
            paramMap.put(SDKParamKey.CP_ORDER_ID, data.getOrderID());
            paramMap.put(SDKParamKey.ACCOUNT_ID, token.getSdkUserID());
            
            String signStr = generateUrlSortedParamString(paramMap, true);
            signStr += this.apiKey;
            
            String sign = EncryptUtils.md5(signStr).toLowerCase();
            
            Log.d("U8SDK", "pay signStr:"+signStr);
            Log.d("U8SDK", "sign:"+sign);
            
            paramMap.put(SDKParamKey.SIGN_TYPE, "MD5");

            cn.gundam.sdk.shell.param.SDKParams sdkParams = new cn.gundam.sdk.shell.param.SDKParams();

            Map<String, Object> map = new HashMap<String, Object>();
            map.putAll(paramMap);
            sdkParams.putAll(map);

            sdkParams.put(SDKParamKey.SIGN, sign);
            UCGameSdk.defaultSdk().pay(U8SDK.getInstance().getContext(), sdkParams);		
			
		}catch(Exception e){
			U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, e.getMessage());
			e.printStackTrace();
		}
	}
	
    public static String generateUrlSortedParamString(Map<String,String> params, boolean nullExcluded) {

        StringBuffer content = new StringBuffer();
        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key) == null ? "" : params.get(key).toString();
            if(nullExcluded && value.length() == 0){
                continue;
            }
            content.append( key + "=" + value);
//            if(!StringUtils.isEmpty(splitChar)){
//                content.append(splitChar);
//            }
        }
//        if(content.length() > 0 && !StringUtils.isEmpty(splitChar)){
//            content.deleteCharAt(content.length() - 1);
//        }

        return content.toString();
    }	
	
}
