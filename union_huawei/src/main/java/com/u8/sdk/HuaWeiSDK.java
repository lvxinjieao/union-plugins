package com.u8.sdk;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.jos.AppUpdateClient;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.buoy.BuoyClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.jos.games.player.PlayerExtraInfo;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.U8HttpUtils;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HuaWeiSDK {

    private static final int SIGN_IN_INTENT = 8888;
    private static final int PAY_INTENT = 8889;
    private static final int PROTOCOL_INTENT = 8890;

    private static final int STATE_STARTED = 1;             //游戏中
    private static final int STATE_STOPPED = 2;             //暂停中
    private static final int STATE_LIMITED = 3;             //被限制状态

    private static HuaWeiSDK instance;

    private PayParams currPayData;
    private boolean fromPay = false;
    private boolean fromBillingSupport = false;
    private boolean logined = false;
    private String currPlayerID;

    private BuoyClient buoyClient;
    private PlayersClient playersClient;

    private String lastTransID;

    private boolean repaying;    //补单中。。。

//    private Map<String, String> goods;

    private volatile Timer timer = new Timer();
    private volatile int state;

    public static HuaWeiSDK getInstance() {
        if (instance == null) {
            instance = new HuaWeiSDK();
        }
        return instance;
    }

//    private HuaWeiSDK() {
//        goods = new HashMap<>();
//    }

    public void initSDK(SDKParams data) {
        initSDK();
//        loadGoods();
    }

    private void initSDK() {
        try {
            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {

                    Log.d("U8SDK", "onActivityResult. reqCode:" + requestCode);

                    if (requestCode == SIGN_IN_INTENT) {
                        Log.d("U8SDK", "sdk login callback called.");
                        handleSignInResult(data);
                    } else if (requestCode == PAY_INTENT) {
                        PurchaseResultInfo buyResultInfo = Iap.getIapClient(U8SDK.getInstance().getContext()).parsePurchaseResultInfoFromIntent(data);
                        Log.i("U8SDK", "支付结果: " + buyResultInfo.getReturnCode());

                        if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_CANCEL) {
                            currPayData = null;
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                            return;
                        }

                        if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                            currPayData = null;
                            Toast.makeText(U8SDK.getInstance().getContext(), "当前已拥有该商品,请重新支付", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                            String InAppPurchaseData = buyResultInfo.getInAppPurchaseData();
                            String InAppDataSignature = buyResultInfo.getInAppDataSignature();
                            startCompleteTask(InAppPurchaseData, InAppDataSignature);
                        } else if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_FAILED || buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_NET_ERROR || buyResultInfo.getReturnCode() == 30002) {
                            //这里不知道成功还是失败，需要调用getPurchase检测
                            Log.d("U8SDK", "huawei pay result enter checkPurchaseAfterPay::");
                            checkPurchaseAfterPay(currPayData);    //这里这样调用， 华为审核， 30002超时，支付补单
                            currPayData = null;
                        } else {
                            currPayData = null;
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                        }
                    } else if (requestCode == PROTOCOL_INTENT) { //支付协议结果
                        int returnCode = 1;
                        if (data != null) {
                            returnCode = data.getIntExtra("returnCode", -1);
                        }
                        Log.d("U8SDK", "pay protocol return:" + returnCode);
                        if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) { // 请求成功，需要重新购买
                            getBuyIntentWithPrice(currPayData);
                        } else {  // 请求失败
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                        }
                    } else if (requestCode == 1000) { //实名认证界面结果， 这里继续调用查询接口，查询是否成人等
                        checkRealName();
                    }
                }

                @Override
                public void onPause() {
                    try {
                        if (!logined) {
                            return;
                        }
                        buoyClient.hideFloatWindow();
                        exitGameSubmit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onResume() {
                    if (!logined) {
                        return;
                    }
                    showFloatView();
                    enterGameSubmit();
                }
            });

            JosAppsClient appsClient = JosApps.getJosAppsClient(U8SDK.getInstance().getContext(), null);
            appsClient.init();

            buoyClient = Games.getBuoyClient(U8SDK.getInstance().getContext());

            //初始化华为分析
//			HiAnalyticsTools.enableLog();
//			HiAnalyticsInstance instance = HiAnalytics.getInstance(U8SDK.getInstance().getContext());

            Log.d("U8SDK", "huawei sdk inited");
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUpdate();
                }
            }, 3000);
        } catch (Exception e) {
            U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_FAIL, "sdk init failed.");
            Log.e("U8SDK", "bind service error", e);
        }
    }

    private void checkUpdate() {
        final AppUpdateClient client = JosApps.getAppUpdateClient(U8SDK.getInstance().getContext());
        client.checkAppUpdate(U8SDK.getInstance().getContext(), new CheckUpdateCallBack() {
            @Override
            public void onUpdateInfo(Intent intent) {

                if (intent != null) {
                    //更新状态信息
                    int status = intent.getIntExtra(UpdateKey.STATUS, 0);
                    //返回错误码，建议打印
                    int rtnCode = intent.getIntExtra(UpdateKey.FAIL_CODE, 0);
                    //失败信息，建议打印
                    String reason = intent.getStringExtra(UpdateKey.FAIL_REASON);
                    //是否强制更新应用，弹出对话框后按了返回键，整个应用退出
                    boolean isExit = intent.getBooleanExtra(UpdateKey.MUST_UPDATE, false);
                    //更新弹框点击，点击立即更新还是以后再说
                    int buttonStatus = intent.getIntExtra(UpdateKey.BUTTON_STATUS, 0);

                    //获取更新信息
                    Serializable info = intent.getSerializableExtra(UpdateKey.INFO);
                    String updateContent = null;
                    if (info instanceof ApkUpgradeInfo) {
                        ApkUpgradeInfo upgradeInfo = (ApkUpgradeInfo) info;
                        //弹出升级提示框
                        client.showUpdateDialog(U8SDK.getInstance().getContext(), upgradeInfo, false);
                        updateContent = upgradeInfo.toString();
                        Log.e("U8SDK", "onUpdateInfo status: " + status + ",failcause: " + rtnCode + ",isExit: " + isExit + ",updateInfo: " + info.toString());
                    }
                }
            }

            @Override
            public void onMarketInstallInfo(Intent intent) {

            }

            @Override
            public void onMarketStoreError(int i) {

            }

            @Override
            public void onUpdateStoreError(int i) {

            }
        });

    }

    public void login() {
        try {
            doLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitExtraData(UserExtraData data) {

        switch (data.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                Log.d("U8SDK", "begin to check failed order when enter game");
                try {
                    HuaWeiSDK.getInstance().checkPurchases();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    enterGameSubmit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            case UserExtraData.TYPE_EXIT_GAME:
                try {
                    exitGameSubmit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private void currentPlayerInfo(AuthHuaweiId id) {
        playersClient = Games.getPlayersClient(U8SDK.getInstance().getContext());

        // 设置试玩监听
//		playersClient.setGameTrialProcess(new GameTrialProcess() {
//			@Override
//			public void onTrialTimeout() {
//				//试玩时间结束
//			}
//			@Override
//			public void onCheckRealNameResult(boolean hasRealName) {
//				if (hasRealName) {// 已实名，继续后续的游戏登录处理
//					return;
//				}
//				//未实名，建议开发者提示玩家后退出游戏或引导玩家重新登录并实名认证
//			}
//		});

        Task<Player> playerTask = playersClient.getCurrentPlayer();
        playerTask.addOnSuccessListener(new OnSuccessListener<Player>() {

            @Override
            public void onSuccess(Player player) {//获取玩家信息

                if (player != null) {
                    try {
                        currPlayerID = player.getPlayerId();
                        JSONObject json = new JSONObject();
                        json.put("ts", player.getSignTs());
                        json.put("playerId", player.getPlayerId());
                        json.put("playerName", player.getDisplayName());
                        json.put("playerLevel", player.getLevel());
                        json.put("playerSSign", player.getPlayerSign());
                        U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_SUCCESS, json.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "获取玩家信息");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(Exception e) { // 获取玩家信息失败
                U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "获取玩家信息失败");
                if (e instanceof ApiException) {
                    Log.e("U8SDK", "getPlayerInfo failed, status: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    private void showFloatView() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (buoyClient != null) {
                    buoyClient.showFloatWindow();
                }
            }
        });
    }

    private void handleSignInResult(Intent data) {
        if (null == data) {
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed");
            return;
        }

        Task<AuthHuaweiId> task = HuaweiIdAuthManager.parseAuthResultFromIntent(data);

        if (task.isSuccessful()) {  //登录成功，获取用户的华为帐号信息和Authorization Code
            Log.d("U8SDK", "sdk user sign in success.");
            logined = true;
            AuthHuaweiId authHuaweiId = task.getResult();
            currentPlayerInfo(authHuaweiId);
            showFloatView();

            if (fromPay && currPayData != null) {
                fromPay = false;
                getBuyIntentWithPrice(currPayData);
            } else if (fromBillingSupport && currPayData != null) {
                isBillingSupported(currPayData);
            }
        } else {
            Log.e("U8SDK", "sign in failed : " + ((ApiException) task.getException()).getStatusCode());

            if (fromBillingSupport) {
                currPayData = null;
                fromBillingSupport = false;
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                return;
            }

            if (fromPay) {
                currPayData = null;
                fromPay = false;
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                return;
            }
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "sdk login failed");
        }
    }

    private void doLogin() {
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
        HuaweiIdAuthService service = HuaweiIdAuthManager.getService(U8SDK.getInstance().getContext(), authParams);
        U8SDK.getInstance().getContext().startActivityForResult(service.getSignInIntent(), SIGN_IN_INTENT);
    }


    private void enterGameSubmit() {

        if (playersClient == null) {
            Log.w("U8SDK", "enterGameSubmit failed. playersClient is null");
            return;
        }

        if (TextUtils.isEmpty(currPlayerID)) {
            Log.w("U8SDK", "enterGameSubmit failed. curr player id is null");
            //U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "player id is null.");
            return;
        }

        final String transid = UUID.randomUUID().toString().replace("-", "");

        Log.d("U8SDK", "begin submit enter game. transid:" + transid);

        Task<String> task = playersClient.submitPlayerEvent(currPlayerID, transid, "GAMEBEGIN");
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String jsonRequest) {
                Log.d("U8SDK", "enterGameSubmit result: " + jsonRequest);
                try {
                    JSONObject data = new JSONObject(jsonRequest);
                    lastTransID = data.getString("transactionId");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w("U8SDK", "parse jsonArray meet json exception");
                }
                startLocalTime();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                if (e instanceof ApiException) {
                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                    Log.w("U8SDK", "enterGameSubmit failed. " + result);
                    if (((ApiException) e).getStatusCode() == 7022) {
                        Log.d("U8SDK", "enterGameSubmit return 7022, don't need game to apply real name rule");
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void exitGameSubmit() {

        if (playersClient == null || TextUtils.isEmpty(currPlayerID) || TextUtils.isEmpty(lastTransID)) {
            Log.w("U8SDK", "exitGameSubmit failed. playersClient is null or currPlayerID is null or lastTransID is null");
            return;
        }

        Log.d("U8SDK", "exitGameSubmit called. currPlayerID:" + currPlayerID + "; lastTransID:" + lastTransID);

        Task<String> task = playersClient.submitPlayerEvent(currPlayerID, lastTransID, "GAMEEND");
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("U8SDK", "exitGameSubmit result: " + s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
                    Log.w("U8SDK", "exitGameSubmit failed. " + result);
                }
            }
        });
    }


    /**
     * 1、判断当前地区是否支持内购
     * 2、支付之前， 调用getPurchases接口，查询该商品， 上一次支付是否已经完成。 如果上次支付没有完成， 则进行发货补单消耗操作。
     * 3、调用支付接口进行支付，同时本地保存支付中的订单
     * 4、支付完成， 携带票据通知u8server校验； 校验返回成功，调用consume进行消耗。 如果这步断了， 下次再次校验的时候， u8server需要做重复订单校验，防止多次发货。
     * 5、在进入游戏的时候， 调用getPurchases接口， 对未完成的订单， 进行补单， 并消耗。
     *
     * @param params
     */
    public void pay(PayParams params) {

        if (this.currPayData != null) {
            Log.e("U8SDK", "the last pay now is paying. ");
            return;
        }

        fromPay = false;
        fromBillingSupport = false;
        Log.d("U8SDK", "begin to sdk pay...");
        isBillingSupported(params);
    }



    /**
     * 判断当前使用的华为移动服务版本是否在支持支付服务的版本内，同
     * 时支持查询当前设备登录的华为帐号所在的服务国家是否在华为IAP支付服务支持结算的国家和地区中
     */
    private void isBillingSupported(final PayParams payParams) {

        Task<IsEnvReadyResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).isEnvReady();
        task.addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult isBillingSupportedResult) {

                if (isBillingSupportedResult != null) {
                    Log.i("U8SDK", "isBillingSupported: " + isBillingSupportedResult.getReturnCode());
                    if (isBillingSupportedResult.getReturnCode() == 0) {
                        checkPurchaseBeforePay(payParams);
                    } else {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.isBillingSupportedResult.failed");
                    }
                } else {
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                    Log.e("U8SDK", "isBillingSupported check failed. null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        // Not logged in.
                        Log.w("U8SDK", "isBillingSupport check. 未登录");
                        // 未登录场景
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                        if (status.hasResolution()) {
                            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                        // The current region does not support HUAWEI IAP.
                        Log.i("U8SDK", "当前服务地不支持IAP");
                        Toast.makeText(U8SDK.getInstance().getContext(), "当前位置不支持内购", Toast.LENGTH_SHORT).show();
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "not support iap");
                    }
                }
            }
        });
    }

    /**
     * 进入游戏的时候， 进行补单检查
     * 查询用户所有已订购商品信息，商品包括消耗商品、非消耗商品和自动续费订阅商品
     * priceType 需要查询的商品类型。0 : 消耗型商品1 : 非消耗型商品2 : 自动续费订阅商品3 : 预留值，表示非续订订阅商品
     * continuationToken  支持分页查询的数据定位标志。第1次查询时可以不传该参数，调用接口后在返回信息会包含该参数，在下次调用接口时如果需要支持分页查询，则可以在第2次调用时输入
     */
    public void checkPurchases() {

        OwnedPurchasesReq ownedPurchasesReq = new OwnedPurchasesReq();
        ownedPurchasesReq.setPriceType(0);
        Task<OwnedPurchasesResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                //获取查询结果
                if (result != null) {

                    Log.i("U8SDK", "checkPurchases onSuccess:" + result.getItemList().toString());

                    //检查上次支付未完成， 需要进行补单发货的操作
                    List<String> inAppPList = result.getInAppPurchaseDataList();
                    List<String> appSignList = result.getInAppSignature();

                    int index = 0;
                    for (String p : inAppPList) {
                        try {

                            if (appSignList != null && appSignList.size() > index) {
                                String appSign = appSignList.get(index);
                                repaying = true;
                                startCompleteTask(p, appSign);

                            } else {
                                Log.e("U8SDK", "checkPurchases failed. app sign size err.");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            ++index;
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // e为IapApiException实例
                e.printStackTrace();

                if (e instanceof IapApiException) {
                    Status status = ((IapApiException) e).getStatus();
                    int statuscode = status.getStatusCode();
                    Log.i("U8SDK", "checkPurchases faile code:" + statuscode + "; msg:" + status.getStatusMessage() + ";err str:" + status.getErrorString());
                }
            }
        });
    }


    /**
     * 可以调用此接口自行设置商品价格并完成支付，而不是从PMS获取价格
     * productId 应用自定义的商品ID，商品ID用于唯一标识一个商品，不能重复。
     * priceType 商品类型。0 : 消耗型商品1 : 非消耗型商品3 : 预留值，表示非自动续订商品
     * productName 商品名称，由应用自定义
     * amount 商品金额，商品需要支付的金额。此金额将会在支付时显示给用户确认。
     * country可选参数 国家码.建议无特殊需要，不传
     * currency可选参数 币种 选填.建议无特殊需要不传此参数。目前仅支持CNY，默认CNY
     * sdkChannel渠道信息0 代表自有应用，无渠道1 代表应用市场渠道2 代表预装渠道3 代表游戏中心4 代表运动健康渠道
     * serviceCatalog  商品所属的产品类型。X4：主题;X5：应用商店;X6：游戏;X7：天际通;X8：云空间;X9：电子书;X10：华为学习;X11：音乐;X12 视频 ;V0：vmall实体商品;X31：话费充值;X32：机票/酒店;X33：电影票;X34：团购;X35：手机预购;X36：公共缴费;X37：基金理财;X38：彩票;X39：流量充值
     * developerPayload 商户侧保留信息。若该字段有值，在支付成功后的回调结果中会原样返回给应用
     */
    private void getBuyIntentWithPrice(final PayParams currPayData) {

//		  PurchaseIntentWithPriceReq req = new PurchaseIntentWithPriceReq();
//        req.setProductId(currPayData.getProductId());
//        req.setPriceType(0);
//        req.setProductName(currPayData.getProductName());
//        req.setAmount(currPayData.getPrice() * 100 + "");
//        req.setSdkChannel("3");
//        req.setServiceCatalog("X6");
//        req.setCountry("CN");
//        req.setCurrency("CNY");
//        req.setDeveloperPayload(currPayData.getOrderID());
//		  Task<PurchaseIntentResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).createPurchaseIntentWithPrice(req);


//        String waresid = getWaresid(currPayData.getProductId());
//        Log.d("U8SDK", "huawei pay start. waresid:" + waresid);

        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(currPayData.getProductId());
        req.setPriceType(0);
        req.setDeveloperPayload(currPayData.getOrderID());


        Task<PurchaseIntentResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).createPurchaseIntent(req);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result != null) {   //获取执行结果
                    Status status = result.getStatus();
                    Log.i("U8SDK", "createPurchaseIntent result:" + status.getStatusCode() + status.getStatusMessage() + status.getErrorString());
                    if (status.hasResolution()) {
                        try {
                            //拉起支付收银台
                            HuaWeiSDK.this.currPayData = currPayData;
                            status.startResolutionForResult(U8SDK.getInstance().getContext(), PAY_INTENT);
                        } catch (Exception e) {
                            HuaWeiSDK.this.currPayData = null;
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("U8SDK", "createPurchaseIntent result. has no resolution");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // e为IapApiException实例
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        Log.i("U8SDK", "getBuyIntentWithPrice:未登录");
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                        if (status.hasResolution()) {
                            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS, "");
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT) {
                        Log.i("U8SDK", "getBuyIntentWithPrice:未同意支付协议");
                        Toast.makeText(U8SDK.getInstance().getContext(), "请先查阅支付协议，并点击同意", Toast.LENGTH_SHORT).show();
                        try {
                            HuaWeiSDK.this.currPayData = currPayData;
                            status.startResolutionForResult(U8SDK.getInstance().getContext(), PROTOCOL_INTENT);
                        } catch (Exception ex) {
                            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed.");
                            HuaWeiSDK.this.currPayData = null;
                            ex.printStackTrace();
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                        Log.i("U8SDK", "已拥有该商品");
                        Toast.makeText(U8SDK.getInstance().getContext(), "您已拥有该商品， 大退重新进入游戏，将自动为您补发", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i("U8SDK", "getBuyIntentWithPrice错误码：" + status.getStatusCode() + "msg:" + status.getStatusMessage() + ";err:" + status.getErrorString());
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                    }
                }
            }
        });
    }

    private void startRepay(final String purchaseData, final String purchaseSign) {
        AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
        builder.setTitle("补单确认");
        builder.setMessage("亲爱的玩家，您上一次购买还没有到账，我们将为您完成补单操作，点击确定之后，稍等一会请查看是否成功到账，如果没有到账，请联系客服");
        builder.setCancelable(false);
        builder.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        repaying = true;
                        startCompleteTask(purchaseData, purchaseSign);
                    }
                });
        builder.show();
    }

    /**
     * 支付之前，先调用该接口， 查询同样的商品，是否需要补单
     * 查询用户所有已订购商品信息，商品包括消耗商品
     * priceType 需要查询的商品类型。0 : 消耗型商品1 : 非消耗型商品2 : 自动续费订阅商品3 : 预留值，表示非续订订阅商品
     * continuationToken  支持分页查询的数据定位标志。第1次查询时可以不传该参数，调用接口后在返回信息会包含该参数，在下次调用接口时如果需要支持分页查询，则可以在第2次调用时输入
     */
    private void checkPurchaseBeforePay(final PayParams payParams) {

        OwnedPurchasesReq ownedPurchasesReq = new OwnedPurchasesReq();
        ownedPurchasesReq.setPriceType(0);
        Task<OwnedPurchasesResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {//获取查询结果
                if (result != null) {
                    Log.i("U8SDK", "getPurchases onSuccess:" + result.getItemList().toString());
                    //检查上次支付未完成， 需要进行补单发货的操作
                    List<String> inAppPList = result.getInAppPurchaseDataList();
                    List<String> appSignList = result.getInAppSignature();
                    boolean found = false;
                    int index = 0;
                    for (String p : inAppPList) {
                        try {
                            JSONObject json = new JSONObject(p);
                            String productId = json.optString("productId", "");
                            if (!TextUtils.isEmpty(productId)) {
                                found = true;
                                if (appSignList != null && appSignList.size() > index) {
                                    String appSign = appSignList.get(index);
                                    startRepay(p, appSign); //检测到丢单，需要重新补单的时候， 中断本次支付
                                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay canceled");
                                } else {
                                    Log.e("U8SDK", "check purchase failed. app sign size err.");
                                }
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            ++index;
                        }
                    }

                    if (!found) { //正常支付
                        getBuyIntentWithPrice(payParams);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                if (e instanceof IapApiException) {
                    Status status = ((IapApiException) e).getStatus();
                    int statuscode = status.getStatusCode();
                    Log.i("U8SDK", "getPurchases faile code:" + statuscode + "; msg:" + status.getStatusMessage() + ";err str:" + status.getErrorString());
                    //查询失败， 正常支付
                    getBuyIntentWithPrice(payParams);
                }
            }
        });
    }

    /**
     * 支付之后， 华为SDK返回-1，需要调用checkPurchase来判断是否支付成功
     */
    private void checkPurchaseAfterPay(final PayParams payParams) {

        if (payParams == null) {
            Log.e("U8SDK", "checkPurchaseAtferPay failed. payParams is null");
            U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
            return;
        }

        OwnedPurchasesReq getPurchaseReq = new OwnedPurchasesReq();
        getPurchaseReq.setPriceType(0);
        Task<OwnedPurchasesResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).obtainOwnedPurchases(getPurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                //获取查询结果
                if (result != null) {

                    Log.i("U8SDK", "getPurchases in checkPurchaseAfterPay onSuccess:" + result.getItemList().toString());

                    //检查上次支付未完成， 需要进行补单发货的操作
                    List<String> inAppPList = result.getInAppPurchaseDataList();
                    List<String> appSignList = result.getInAppSignature();
                    boolean found = false;
                    int index = 0;
                    for (String p : inAppPList) {
                        try {
                            JSONObject json = new JSONObject(p);
                            String developerPayload = json.optString("developerPayload");
                            if (developerPayload.equals(payParams.getOrderID())) {
                                found = true;
                                if (appSignList != null && appSignList.size() > index) {
                                    String appSign = appSignList.get(index);
                                    //检测到丢单，需要重新补单的时候， 中断本次支付
                                    Log.d("U8SDK", "check pay after paying. start complete task.");
                                    startCompleteTask(p, appSign);
                                } else {
                                    Log.e("U8SDK", "check purchase failed. app sign size err.");
                                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                                }
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            ++index;
                        }
                    }

                    if (!found) {   //失败
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // e为IapApiException实例
                e.printStackTrace();

                if (e instanceof IapApiException) {
                    Status status = ((IapApiException) e).getStatus();
                    int statuscode = status.getStatusCode();
                    Log.i("U8SDK", "getPurchases faile code:" + statuscode + "; msg:" + status.getStatusMessage() + ";err str:" + status.getErrorString());
                    //查询失败， 正常支付
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "sdk pay failed");
                }
            }
        });
    }

    /**
     * 针对消耗型商品，在商品支付成功后，应用需要调用此接口对消耗商品执行消耗后才能向用户发放商品
     * purchaseToken  用户购买商品的标识，在支付商品时由支付服务器生成并在inAppPurchaseData中返回给应用。应用传入此参数供支付服务器对订单状态进行更新后再发放商品。
     * developerChallenge 开发者自定义的挑战字，唯一标识此次消耗请求。消耗成功后此挑战字会记录在购买信息中并返回。
     */
    private void consumePurchase(String purchaseToken, String orderID) {
        Log.d("U8SDK", "begin consume purchase. purchaseToken:" + purchaseToken + ";orderID:" + orderID);
        ConsumeOwnedPurchaseReq consumePurchaseReq = new ConsumeOwnedPurchaseReq();
        consumePurchaseReq.setPurchaseToken(purchaseToken);
        consumePurchaseReq.setDeveloperChallenge(orderID);
        Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(U8SDK.getInstance().getContext()).consumeOwnedPurchase(consumePurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {  //获取执行结果
                if (result != null) {
                    Log.i("U8SDK", "consumePurchase code " + result.getReturnCode());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) { // e为IapApiException实例
                if (e instanceof IapApiException) {
                    IapApiException iapApiException = (IapApiException) e;
                    Status status = iapApiException.getStatus();
                    Log.i("U8SDK", "consumePurchase错误码：" + status.getStatusCode());
                }
            }
        });
    }

    private void onOrderComplete(String result) {
        Log.d("U8SDK", "onOrderComplete result:" + result);
        if (result == null) {
            Log.e("U8SDK", "onOrderComplete result is null");
            this.currPayData = null;
            if (!repaying) {
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
            }
            repaying = false;
            return;
        }

        try {
            JSONObject json = new JSONObject(result);
            int code = json.optInt("result", -1);
            if (code != -1) {

                String purchaseToken = json.getString("purchaseToken");
                String orderID = json.getString("orderID");

                consumePurchase(purchaseToken, orderID);

                if (!repaying) {

                    if (code == 1) {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    } else {
                        U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
                    }
                } else {
                    if (code == 1) {
                        U8SDK.getInstance().onResult(U8Code.CODE_CHECK_ORDER_SUCCESS, orderID);
                    } else {
                        U8SDK.getInstance().onResult(U8Code.CODE_CHECK_ORDER_FAILED, orderID);
                    }
                }
            } else {
                if (!repaying) {
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "purchase failed");
                }
            }
        } catch (Exception e) {
            if (!repaying) {
                U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed");
            }
            e.printStackTrace();
        } finally {
            this.currPayData = null;
            this.repaying = false;
        }
    }

    //默认的AsyncTask的执行顺序可能会有些影响，导致队列中的任务并不能被及时执行
    @SuppressLint("NewApi")
    private void startCompleteTask(String purchaseData, String purchaseSign) {
        OrderCompleteTask authTask = new OrderCompleteTask();
        if (Build.VERSION.SDK_INT >= 11) {
            authTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, purchaseData, purchaseSign);
        } else {
            authTask.execute(purchaseData, purchaseSign);
        }
    }

    @SuppressLint("NewApi")
    class OrderCompleteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {

            String purchaseData = args[0];
            String purchaseSign = args[1];
            Log.d("U8SDK", "begin to complete order. purchase data:" + purchaseData);
            Log.d("U8SDK", "begin to complete order. ppurchase sign:" + purchaseSign);
            String url = U8SDK.getInstance().getU8ServerURL() + "/pay/huawei/checkPay";

            Map<String, String> params = new HashMap<String, String>();
            params.put("appID", U8SDK.getInstance().getAppID() + "");
            params.put("purchaseData", purchaseData);
            params.put("purchaseSign", purchaseSign);
            String signStr = "appID=" + U8SDK.getInstance().getAppID() + "&purchaseData=" + purchaseData + "&purchaseSign=" + purchaseSign + U8SDK.getInstance().getAppKey();
            String sign = EncryptUtils.md5(signStr);
            params.put("sign", sign);
            String completeResult = U8HttpUtils.httpPost(url, params);
            return completeResult;
        }

        protected void onPostExecute(String result) {
            try {
                onOrderComplete(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void checkRealName() {
        try {
            Task<PlayerExtraInfo> task = playersClient.getPlayerExtraInfo(lastTransID);
            task.addOnSuccessListener(new OnSuccessListener<PlayerExtraInfo>() {
                @Override
                public void onSuccess(PlayerExtraInfo extra) {
                    if (extra != null) {
                        Log.d("U8SDK", "IsRealName: " + extra.getIsRealName() + ", IsAdult: " + extra.getIsAdult() + ", PlayerId: " + extra.getPlayerId() + ", PlayerDuration: " + extra.getPlayerDuration());
                        int age = 0;
                        if (extra.getIsAdult()) {
                            age = 18;
                        } else if (extra.getIsRealName()) {
                            age = 16;
                        }

                        if (age < 18) {
                            int mintues = extra.getPlayerDuration();
                            if (mintues >= 90) { // 限制玩家的登录
                                showRealNameTip();
                                return;
                            }
                        } else {
                            Log.d("U8SDK", "user alread over 18. ");
                            stopLocalTime();
                        }
                    } else {
                        Log.e("U8SDK", "Player extra info is empty.");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d("U8SDK", "check real name failed with exception:" + e.getMessage());
                    if (e instanceof IapApiException) {
                        IapApiException iapApiException = (IapApiException) e;
                        Status status = iapApiException.getStatus();
                        Log.i("U8SDK", "consumePurchase错误码：" + status.getStatusCode());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startLocalTime() {

        stopLocalTime();

        Log.d("U8SDK", "begin start local timer to check real name.");
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkRealName();
                }
            }, 5 * 1000, 900 * 1000);
            // 15分钟查询一次
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopLocalTime() {
        Log.d("U8SDK", "begin stop local timer.");
        if (timer != null) {
            try {
                timer.cancel();
                timer.purge();
                timer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showRealNameTip() { //游戏自己的退出确认框
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(U8SDK.getInstance().getContext());
                builder.setTitle("防沉迷提示");
                builder.setMessage("您当前未实名或者是未成年，根据游戏健康系统，您今日累计游戏超1.5小时，已达到防沉迷限制。为了您的健康，请明日再游戏");
                builder.setCancelable(false);
                builder.setNeutralButton("退出游戏",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) { //退出游戏
                                U8SDK.getInstance().getContext().finish();
                                System.exit(0);
                            }
                        });
                builder.show();
            }
        });
    }


    //=====================================================================================
    //=====================================================================================
    //=====================================================================================
    //=====================================================================================
    //=====================================================================================

   /* private String getWaresid(String productID) {
        if (this.goods.containsKey(productID)) {
            return this.goods.get(productID);
        }
        return productID;
    }*/

    /*public void loadGoods() {
        String xmlGoods = SDKTools.getAssetConfigs(U8SDK.getInstance().getContext(), "huawei_pay.xml");
        if (xmlGoods == null) {
            Log.e("U8SDK", "huawei_pay.xml 文件加载失败");
            return;
        }

        Log.e("U8SDK", "huawei_pay.xml 内容 ：" + xmlGoods);

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xmlGoods));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("good".equals(tag)) {
                            String productId = parser.getAttributeValue(null, "productID");
                            String wareid = parser.getAttributeValue(null, "waresid");
                            if (!this.goods.containsKey(productId)) {
                                this.goods.put(productId, wareid);
                            } else {
                                Log.e("U8SDK", "Curr Good: " + productId + " has duplicated.");
                            }
                            Log.d("U8SDK", "Curr Good: " + productId + "; waresid:" + wareid);
                        }
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
