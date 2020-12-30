package com.u8.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import com.lang.lun.open.AnnounceTimeCallBack;
import com.lang.lun.open.ApiFactory;
import com.lang.lun.open.ExitListener;
import com.lang.lun.open.InitListener;
import com.lang.lun.open.LoginListener;
import com.lang.lun.open.LogoutListener;
import com.lang.lun.open.OrderInfo;
import com.lang.lun.open.PayListener;
import com.lang.lun.open.UploadRoleCallBack;
import com.lang.lun.open.UserResult;
import com.u8.sdk.log.Log;

import android.app.Activity;
import android.text.TextUtils;

public class LangLunSDK {

	public static LangLunSDK instance;
	public ApiFactory factory;
	public Activity activity;

	public static LangLunSDK getInstance() {
		if (instance == null) {
			instance = new LangLunSDK();
		}
		return instance;
	}

	public void init(Activity activity) {
		this.activity = activity;
		this.factory = ApiFactory.getInstance();

		factory.init(activity, new InitListener() {

			@Override
			public void initResult(int arg0, String arg1) {
				if (arg0 == 1) {
					U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "初始化成功");
					Log.i("U8SDK", "初始化成功");
				} else {
					U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "初始化失败");
					Log.i("U8SDK", "初始化失败");
				}

			}
		}, new LogoutListener() {

			@Override
			public void logoutResult(int arg0, String arg1) {
				if (arg0 == 1) {
					U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "注销成功");
					Log.i("U8SDK", "注销成功");
				} else {
					U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_FAIL, "注销失败");
					Log.i("U8SDK", "注销失败");
				}

			}
		}, new ExitListener() {

			@Override
			public void exitResult(int arg0, String arg1) {
				if (arg0 == 1) {
					U8SDK.getInstance().onExitResult(U8Code.CODE_EXIT_GAME, "退出成功");
					Log.i("U8SDK", "退出成功");
				}
			}
		},true);

		U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
			@Override
			public void onResume() {
				super.onResume();
				ApiFactory.getInstance().onResume(U8SDK.getInstance().getContext());
			}

			@Override
			public void onPause() {
				super.onPause();
				ApiFactory.getInstance().onPause(U8SDK.getInstance().getContext());
			}

			@Override
			public void onStop() {
				super.onStop();
				ApiFactory.getInstance().onStop(U8SDK.getInstance().getContext());
			}

			@Override
			public void onDestroy() {
				super.onDestroy();
			}
		});
	}

	public void login() {
		Log.i("U8SDK", "login");
		this.factory.login(activity, loginListener);
	}

	public LoginListener loginListener = new LoginListener() {

		@Override
		public void loginResult(UserResult res) {
			switch (res.getCode()) {
			case UserResult.USER_RESULT_LOGIN_FAIL:
				Log.i("U8SDK", "登录失败");
				U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "登录失败");
				break;
			case UserResult.USER_RESULT_LOGIN_SUCC:
				Log.i("U8SDK", "登录成功");
				String userId = res.getUserId();// 登录用户id
				String userName = res.getUserName();
				String token = res.getToken();
				try {
					JSONObject json = new JSONObject();
					json.put("userId", userId);
					json.put("userName", userName);
					json.put("token", token);
					String str = json.toString();
					U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, str);
					// 设置防沉迷回调
					ApiFactory.getInstance().startFloating(activity);
					ApiFactory.getInstance().setAnnounceTimeCallBack(announceTimeCallBack);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * 防沉迷时间回掉
	 */
	private AnnounceTimeCallBack announceTimeCallBack = new AnnounceTimeCallBack() {
		@Override
		public void callback(String result) {

			if (TextUtils.isEmpty(result)) {
				return;
			}

			if ("1".equals(result)) {
				// 第一次到时通知
				// Log.i(TAG, "第一次到时通知");
			}

			if ("2".equals(result)) {
				// 第二次到时通知,玩家进入疲劳游戏时间
				// Log.i(TAG, "第二次到时通知");
			}

			if ("3".equals(result)) {
				// Log.e(TAG, "时间到，停止计时。");
			}

		}
	};

	public void logout() {
		Log.i("U8SDK", "logout");
		ApiFactory.getInstance().logout(activity);
	}

	public void exit() {
		Log.i("U8SDK", "exit");
		ApiFactory.getInstance().exit(activity);
	}

	public void submitExtendData(UserExtraData extraData) {
		Log.i("U8SDK", "submitExtendData");

		String serverId = String.valueOf(extraData.getServerID());
		String serverName = extraData.getServerName();
		String roleName = extraData.getRoleName();
		String roleLevel = extraData.getRoleLevel();
		this.factory.uploadRole(activity, serverId, serverName, roleName, roleLevel, this.uploadRoleCallBack);
	}

	private UploadRoleCallBack uploadRoleCallBack = new UploadRoleCallBack() {

		@Override
		public void onUploadComplete(String result) {

			if ("1".equals(result)) {
				Log.i("U8SDK", "上传角色回调: " + result);
			}
		}
	};

	public void pay(Activity activity, PayParams data) {
		Log.i("U8SDK", "pay");
		OrderInfo order = new OrderInfo();
		order.setProductName(data.getProductName());
		order.setProductDesc(data.getProductDesc());
		order.setAmount(data.getPrice() * 100);
		order.setExtendInfo(data.getExtension());
		order.setServerName(data.getServerName());
		order.setRoleName(data.getRoleName());
		order.setGameServerId(data.getServerId());

		this.factory.pay(activity, order, new PayListener() {

			@Override
			public void payResult(String arg0) {
				if ("0".equals(arg0)) {
					// ToastUtil.show(MainActivity.this, "支付成功");
					U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "支付成功");
				} else {
					// ToastUtil.show(MainActivity.this, "支付失败");
					U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "支付失败");
				}
			}
		});
	}
}
