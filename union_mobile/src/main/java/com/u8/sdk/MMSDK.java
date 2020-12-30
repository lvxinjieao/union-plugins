package com.u8.sdk;

import com.cmic.mpay.MPay;
import com.cmic.mpay.MmRoleInfo;
import com.cmic.mpay.OnExitListener;
import com.cmic.mpay.OnLoginListener;
import com.cmic.mpay.OnLogoutListener;
import com.cmic.mpay.OnMPayListener;
import com.cmic.mpay.net.LoginType;
import com.cmic.mpay.net.PrePayOrder;
import com.u8.sdk.log.Log;
import com.u8.sdk.utils.EncryptUtils;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class MMSDK {

	private static MMSDK instance;

	private String AppId;
	private String AppKey;
	private String AppName;

	private int mOrientation;

	/**
	 * 注意: channelId通过读取assets/mmeng.xml文件获取
	 * 当前项目中的mmeng.xml文件为Demo专用，正式工程接入时注意替换mmeng.xml文件 具体说明请参考开发文档
	 */
	private String channelId = "";
	private String nonceStr = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS";
	private String sign = "";
	private String prePayId = "";

	public static MMSDK getInstance() {
		if (instance == null) {
			instance = new MMSDK();
		}
		return instance;
	}

	public void initSDK(SDKParams params) {

		try {
			MPay.getInstance().setLogShow(true);// SDK日志开关

			AppId = params.getString("AppId");
			AppKey = params.getString("AppKey");
			AppName = params.getString("AppName");

			Log.i("U8SDK", "AppId=" + AppId);
			Log.i("U8SDK", "AppKey=" + AppKey);
			Log.i("U8SDK", "AppName=" + AppName);

			if (U8SDK.getInstance().getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else {
				mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
			U8SDK.getInstance().getContext().setRequestedOrientation(mOrientation);

			MPay.getInstance().init(U8SDK.getInstance().getApplication(), AppId, AppKey, "2.2.0", AppName,
					new OnMPayListener() {

						@Override
						public void onPayFinish(boolean success, String code, String message) {
							if (success) {
								U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "支付成功");
							} else {
								U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "支付失败:" + code);
							}
						}
					}, new OnExitListener() {

						@Override
						public void onExit(boolean arg0, String arg1) {
							if (arg0) {
								System.exit(0);
							}
						}
					});

			channelId = MPay.getInstance().getChannelID(U8SDK.getInstance().getContext());

			U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "初始化成功");
			Log.i("U8SDK", "初始化成功");
		} catch (Exception e) {
			U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "初始化失败");
			Log.i("U8SDK", "初始化失败");
		}
	}

	public void login() {
		Log.i("U8SDK", "login");
		MPay.getInstance().login(U8SDK.getInstance().getContext(), loginListener, LoginType.TYPE_DEFAULT);
	}

	private OnLoginListener loginListener = new OnLoginListener() {

		@Override
		public void onLogin(boolean arg0, String userId, int loginType) {
			if (arg0) {
				Log.i("U8SDK", "登录成功");
				U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, userId);
			} else {
				Log.i("U8SDK", "登录失败");
				U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "登录失败");
			}
		}
	};

	public void logout() {
		Log.i("U8SDK", "logout");
		MPay.getInstance().logout(U8SDK.getInstance().getContext(), new OnLogoutListener() {

			@Override
			public void onLogoutSuccess() {
				U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "注销成功");
				Log.i("U8SDK", "注销成功");
			}

			@Override
			public void onLogoutFailed(String arg0) {
				U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_FAIL, "注销失败");
				Log.i("U8SDK", "注销失败：" + arg0);
			}
		});
	}

	public void exit() {
		Log.i("U8SDK", "exit");
		MPay.getInstance().exit(U8SDK.getInstance().getContext());
	}

	public void submitExtendData(UserExtraData extraData) {
		Log.i("U8SDK", "submitExtendData");
		String serverId = String.valueOf(extraData.getServerID());
		String serverName = extraData.getServerName();
		String roleID = extraData.getRoleID();
		String roleName = extraData.getRoleName();
		String roleLevel = extraData.getRoleLevel();

		MmRoleInfo info = new MmRoleInfo();
		info.setRoleId(roleID);
		info.setRoleName(roleName);
		info.setRoleGrade(roleLevel);
		info.setDistrictId(serverId);
		info.setDistrictName(serverName);
		info.setExtendInfo("EXT");
		MPay.getInstance().reportRoleInfo(info);
	}

	public void pay(final Activity activity, final PayParams data) {
		Log.i("U8SDK", "pay");

		prePayId = data.getOrderID();
		int totalFee = data.getPrice() * 100;
		String productName = data.getProductName();
		String productDesc = data.getProductDesc();
		sign = EncryptUtils.md5(AppKey + prePayId + totalFee + channelId + AppId).toUpperCase();
		String url = data.getExtension();// 后台配置回调地址

		/**
		 * 创建订单号
		 */
		PrePayOrder.Builder builder = new PrePayOrder.Builder();
		builder.setSign(sign);
		builder.setPrePayId(prePayId);// 订单号
		builder.setTotalFee(totalFee);// 充值金额
		builder.setBody(productName);// 商品信息

		builder.setNonceStr(nonceStr);
		builder.setSignType("HMAC-SHA256");
		builder.setDetail(productDesc);// 商品描述
		builder.setAttach("attach");
		builder.setSpbillCreateIp("127.0.0.1");
		builder.setNotifyUrl(url);
		builder.setTimeStart(Long.toString(System.currentTimeMillis()));
		builder.setTimeExpire(Long.toString(System.currentTimeMillis())).setFeeType("CNY");
		builder.setNotifyUrl(url);

		MPay.getInstance().uploadPrePayOrderInfo(builder.build(), new PrePayOrder.IUploadPrePayInfoListener() {

			@Override
			public void onFailed(Exception arg0) {
				// TODO Auto-generated method stub
				U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "支付失败:" + arg0.getStackTrace());
			}

			@Override
			public void onSuccess() {// 创建成功后-支付
				// TODO Auto-generated method stub
				MPay.getInstance().pay(activity, AppId, prePayId, nonceStr, sign, prePayId);
			}
		});
	}

}
