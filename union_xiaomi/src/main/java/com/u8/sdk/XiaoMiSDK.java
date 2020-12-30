package com.u8.sdk;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.RoleData;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import org.json.JSONObject;

import java.util.List;

public class XiaoMiSDK {

    private static XiaoMiSDK instance;

    static enum SDKState {
        StateDefault, StateIniting, StateInited, StateLogin, StateLogined;
    }

    private SDKState state = SDKState.StateDefault;
    private String appID;
    private String appKey;
    private MiBuyInfo miBuyInfo;

//	private Map<String, GoodInfo> goods;
//	private boolean fixedPay = false;

    private boolean loginAfterInited = false;

    public static XiaoMiSDK getInstance() {
        if (instance == null) {
            instance = new XiaoMiSDK();
        }
        return instance;
    }

    private XiaoMiSDK() {
        //this.goods = new HashMap();
    }

    private void parseSDKParams(SDKParams params) {
        this.appID = params.getString("MiAppID");
        this.appKey = params.getString("MiAppKey");
        //this.fixedPay = params.getBoolean("MiFixedPay");
    }

    public void initSDK(Application application, SDKParams params) {
        Log.d("U8SDK", "init sdk begin.");
        parseSDKParams(params);
        initSDK(application);
    }

    private void initSDK(Application application) {

        if (this.state == SDKState.StateIniting) {
            Log.d("U8SDK", "sdk now initing...");
            return;
        }

        this.state = SDKState.StateIniting;
        try {
            MiAppInfo appInfo = new MiAppInfo();
            appInfo.setAppId(this.appID);
            appInfo.setAppKey(this.appKey);

            MiCommplatform.Init(application, appInfo, new OnInitProcessListener() {

                @Override
                public void finishInitProcess(List<String> arg0, int arg1) {
                    Log.d("U8SDK", "xiaomi sdk init finished." + arg1);

                    XiaoMiSDK.this.state = SDKState.StateInited;
                    if (XiaoMiSDK.this.loginAfterInited) {
                        XiaoMiSDK.this.loginAfterInited = false;
                        XiaoMiSDK.this.login();
                    }
                }

                @Override
                public void onMiSplashEnd() {
                }
            });
        } catch (Exception e) {
            this.state = SDKState.StateDefault;
            e.printStackTrace();
        }
    }


    public void initOnCreate() {
        try {

            MiCommplatform.getInstance().onMainActivityCreate(U8SDK.getInstance().getContext());

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onDestroy() {
                    super.onDestroy();
                    MiCommplatform.getInstance().onMainActivityCreate(U8SDK.getInstance().getContext());
                }
            });
            //loadGoods();
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
        } catch (Exception e) {
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "init failed");
            Log.e("U8SDK", "xiaomi sdk init exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private String encodeLoginResult(String sid, String token) {
        JSONObject json = new JSONObject();
        try {
            json.put("sid", sid);
            json.put("token", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void login() {

        if (this.state.ordinal() < SDKState.StateInited.ordinal()) {
            this.loginAfterInited = true;
            initSDK(U8SDK.getInstance().getApplication(), U8SDK.getInstance().getSDKParams());
        }


        MiCommplatform.getInstance().miLogin(U8SDK.getInstance().getContext(), new OnLoginProcessListener() {

            @Override
            public void finishLoginProcess(int code, MiAccountInfo account) {
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: // 登陆成功
                        XiaoMiSDK.this.state = SDKState.StateLogined;
                        String uid = account.getUid();
                        String session = account.getSessionId();
                        String loginResult = encodeLoginResult(uid, session);
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, loginResult);
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        break;
                }
            }
        });
    }

    public void switchLogin() {
        MiCommplatform.getInstance().miChangeAccountLogin(U8SDK.getInstance().getContext(), new OnLoginProcessListener() {

            @Override
            public void finishLoginProcess(int code, MiAccountInfo account) {
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: // 登陆成功
                        XiaoMiSDK.this.state = SDKState.StateLogined;
                        String uid = account.getUid();
                        String session = account.getSessionId();
                        String loginResult = XiaoMiSDK.this.encodeLoginResult(uid, session);
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, loginResult);
                        break;
                }
            }
        });
    }

    public void submitGameData(UserExtraData myData) {
        Log.d("U8SDK", "xiaomi submitGameData called. type:" + myData.getDataType());
        if ((myData.getDataType() == 2) || (myData.getDataType() == 3) || (myData.getDataType() == 4)) {
            RoleData data = new RoleData();
            data.setLevel(myData.getRoleLevel());
            data.setRoleId(myData.getRoleID());
            data.setRoleName(myData.getRoleName());
            data.setServerId(myData.getServerID() + "");
            data.setServerName(myData.getServerName());
            data.setZoneId(myData.getServerID() + "");
            data.setZoneName(myData.getServerName());
            MiCommplatform.getInstance().submitRoleData(U8SDK.getInstance().getContext(), data);
        }
    }

    public void exit() {
        MiCommplatform.getInstance().miAppExit(U8SDK.getInstance().getContext(), new OnExitListner() {
            public void onExit(int code) {
                if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                    U8SDK.getInstance().onExitResult(U8Code.CODE_EXIT_GAME, "exit game ...");
                    U8SDK.getInstance().getContext().finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }

    public void pay(PayParams params) {

        if (!isLogined()) {
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "The sdk is not logined.");
            return;
        }

        miBuyInfo = new MiBuyInfo();
        miBuyInfo.setCpOrderId(params.getOrderID());// 订 单号
        miBuyInfo.setCpUserInfo(params.getOrderID());// 给⽹游透 传参数

//		if (this.fixedPay) {
//			GoodInfo info = getGoodInfo(params.getProductId());
//			if (info != null) {
//				Log.d("U8SDK", "pay product id:" + params.getProductId() + "; xiaomi product id:" + info.getWaresid());
//				this.miBuyInfo.setProductCode(info.getWaresid());// 商品代码，开发者申请获得
//				this.miBuyInfo.setCount(params.getBuyNum() <= 0 ? 1 : params.getBuyNum());// 购买数量
//			} else {
//				Log.d("U8SDK", "cannot find good info in assets. just use defualt product id");
//				this.miBuyInfo.setProductCode(params.getProductId());// 商品代码，开发者申请获得
//				this.miBuyInfo.setCount(1);// 购买数量
//			}
//		} else {
//			this.miBuyInfo.setAmount(params.getPrice()); // 必须是⼤于1的整数，10代表10⽶币，即10元⼈⺠币（不为空） //⽤⼾信息，⽹游必须设置、单机游戏或应⽤可选
//		}

        miBuyInfo.setAmount(params.getPrice()); // 必须是⼤于1的整数，10代表10⽶币，即10元⼈⺠币（不为空）

        Bundle mBundle = new Bundle();
        mBundle.putString("balance", params.getCoinNum() + "");
        mBundle.putString("vip", params.getVip());
        mBundle.putString("lv", params.getRoleLevel() + "");
        mBundle.putString("partyName", "partyName");
        mBundle.putString("roleName", params.getRoleName());
        mBundle.putString("roleId", params.getRoleId());
        mBundle.putString("serverName", params.getServerName());
        miBuyInfo.setExtraInfo(mBundle);

        MiCommplatform.getInstance().miUniPay(U8SDK.getInstance().getContext(), this.miBuyInfo, new OnPayProcessListener() {

            @Override
            public void finishPayProcess(int code) {
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: // 购买成功
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL: // 取消购买
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE: // 购买失败
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failure");
                        break;
                    default:
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed. code:" + code);
                        break;
                }
            }
        });
    }

    public boolean isLogined() {
        return this.state.ordinal() >= SDKState.StateLogined.ordinal();
    }

	/*private GoodInfo getGoodInfo(String productID) {
		if (this.goods.containsKey(productID)) {
			return (GoodInfo) this.goods.get(productID);
		}
		return null;
	}*/


	/*public void loadGoods() {
		if (!this.fixedPay) {
			Log.d("U8SDK", "now not fix pay. ");
			return;
		}
		String xmlGoods = SDKTools.getAssetConfigs(U8SDK.getInstance().getContext(), "xiaomi_pay.xml");
		if (xmlGoods == null) {
			Log.e("U8SDK", "fail to load xiaomi_pay.xml");
			return;
		}
		Log.e("U8SDK", "The xiaomi_pay Str:" + xmlGoods);

		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(xmlGoods));

			int eventType = parser.getEventType();
			while (eventType != 1) {
				switch (eventType) {
				case 2:
					String tag = parser.getName();
					if ("good".equals(tag)) {
						String productId = parser.getAttributeValue(null, "productID");
						String wareid = parser.getAttributeValue(null, "waresid");

						GoodInfo good = new GoodInfo(productId, wareid);
						if (!this.goods.containsKey(productId)) {
							this.goods.put(productId, good);
						} else {
							Log.e("U8SDK", "Curr Good: " + productId + " has duplicated.");
						}
						Log.d("U8SDK", "Curr Good: " + productId + "; waresid:" + wareid);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
