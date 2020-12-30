package com.u8.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import com.mini.sdk.PluginApi;
import com.mini.sdk.listener.ExitListener;
import com.mini.sdk.listener.InitListener;
import com.mini.sdk.listener.LoginListener;
import com.mini.sdk.listener.LoginResult;
import com.mini.sdk.listener.LogoutListener;
import com.mini.sdk.listener.OrderInfo;
import com.mini.sdk.listener.PayListener;
import com.mini.sdk.listener.UpdateRoleListener;

import android.app.Activity;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8SDK;
import com.u8.sdk.log.Log;
import com.u8.sdk.PayParams;

public class NianWanSDK {

	public static NianWanSDK instance;
	public PluginApi factory;
	public Activity activity;

	public static NianWanSDK getInstance() {
		if (instance == null) {
			instance = new NianWanSDK();
		}
		return instance;
	}

	public void init(Activity activity) {
		this.activity = activity;
		this.factory = PluginApi.getInstance(activity);

		this.factory.init(new InitListener() {

			@Override
			public void result(int arg0, String arg1) {
				if (arg0 == InitListener.SDK_INIT_SUCCESS) {
					U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "初始化成功");
					Log.i("U8SDK", "初始化成功");
				} else {
					U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "初始化失败");
					Log.i("U8SDK", "初始化失败");
				}
			}
		}, new LogoutListener() {

			@Override
			public void result(int arg0, String arg1) {
				if (arg0 == LogoutListener.SDK_LOGOUT_SUCCESS) {
					U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "注销成功");
					Log.i("U8SDK", "注销成功");
				} else {
					U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_FAIL, "注销失败");
					Log.i("U8SDK", "注销失败");
				}
			}
		}, new ExitListener() {

			@Override
			public void result(int arg0, String arg1) {
				if (arg0 == ExitListener.SDK_EXIT_SUCCESS) {
					U8SDK.getInstance().onExitResult(U8Code.CODE_EXIT_GAME, "退出成功");
					Log.i("U8SDK", "退出成功");
				}
			}
		});

		U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
			@Override
			public void onResume() {
				super.onResume();
				NianWanSDK.this.factory.onResume();
			}

			@Override
			public void onPause() {
				super.onPause();
				NianWanSDK.this.factory.onPause();
			}
		});
	}

	public void login() {
		Log.i("U8SDK", "login");
		this.factory.login(this.loginListener);
	}

	public LoginListener loginListener = new LoginListener() {

		@Override
		public void result(LoginResult res) {
			switch (res.getCode()) {
			case LoginResult.SDK_LOGIN_FAILED:
				Log.i("U8SDK", "登录失败");
				U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "登录失败");
				break;
			case LoginResult.SDK_LOGIN_SUCCESS:
				Log.i("U8SDK", "登录成功");
				String account = res.getAccount();
				String account_id = res.getAccountId();
				String token = res.getToken();
				String sign = res.getSign();
				try {
					JSONObject json = new JSONObject();
					json.put("account", account);
					json.put("account_id", account_id);
					json.put("token", token);
					json.put("sign", sign);
					String str = json.toString();
					U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, str);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public void logout() {
		Log.i("U8SDK", "logout");
		PluginApi.getInstance(this.activity).logout();
	}

	public void exit() {
		Log.i("U8SDK", "exit");
		PluginApi.getInstance(this.activity).exit();
	}

	public void submitExtendData(UserExtraData extraData) {
		Log.i("U8SDK", "submitExtendData");
		String serverId = String.valueOf(extraData.getServerID());
		String serverName = extraData.getServerName();
		String roleName = extraData.getRoleName();
		String roleLevel = extraData.getRoleLevel();
		this.factory.uploadRole(serverId, serverName, roleName, roleLevel, this.uploadRoleCallBack);
	}

	private UpdateRoleListener uploadRoleCallBack = new UpdateRoleListener() {
		public void result(String message) {
			Log.i("U8SDK", "上传角色回调: " + message);
		}
	};

	public void pay(Activity activity, PayParams data) {
		Log.i("U8SDK", "pay");
		OrderInfo order = new OrderInfo();
		order.setProductName(data.getProductName());
		order.setProductDesc(data.getProductDesc());
		order.setAmount(data.getPrice() * 100);
		order.setExtendInfo(data.getOrderID());
		order.setServerName(data.getServerName());
		order.setRoleName(data.getRoleName());
		order.setGameServerId(data.getServerId());

		this.factory.pay(order, new PayListener() {
			public void result(int code, String message) {
				switch (code) {
				case PayListener.SDK_PAY_SUCCEED:// 支付成功
					U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "支付成功");
					break;

				case PayListener.SDK_PAY_CANCEL:// 支付取消
					U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "支付取消");
					break;

				case PayListener.SDK_PAY_FAILED:// 支付失败
					U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "支付失败");
					break;
				}
			}
		});
	}
}
