package com.u8.sdk;

import android.util.Log;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.AntiAddiction.listener.AntiAddictListener;
import com.tencent.ysdk.module.AntiAddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.bugly.BuglyListener;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

public class YSDKCallback implements UserListener, BuglyListener,PayListener, AntiAddictListener {

    public YSDKCallback() { }

	@Override
	public void OnPayNotify(PayRet ret) {

        if(PayRet.RET_SUCC == ret.ret){
            //支付流程成功
            switch (ret.payState){
                //支付成功
                case PayRet.PAYSTATE_PAYSUCC:
                	//U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay sucess");
					//支付成功向U8Server发送通知，调用查询余额接口并扣费
					Log.e("U8SDK", "pay success. now to req charge to u8server");
					YSDK.getInstance().chargeWhenPaySuccess();
                    break;
                //取消支付
                case PayRet.PAYSTATE_PAYCANCEL:
                    //用户取消
                	YSDK.showTip("支付被取消");
                	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                    break;
                //支付结果未知
                case PayRet.PAYSTATE_PAYUNKOWN:
                    //查询余额
                	YSDK.showTip("未知的支付状态,"+ret.msg);
                	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_UNKNOWN, "pay unknown");
                    break;
                //支付失败
                case PayRet.PAYSTATE_PAYERROR:
                	YSDK.showTip("支付失败："+ret.msg);
                    U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "pay failed:"+ret.msg);
                    break;
            }
        }else{
            Log.e("U8SDK", "sdk pay ret:" + ret.toString());
            switch (ret.flag){
                case eFlag.Login_TokenInvalid:
                    //用户取消支付
                	YSDK.showTip("登录token失效，请重新登录");
                    U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS,"请重新登录！");
                    break;
                case eFlag.Pay_User_Cancle:
                    //用户取消支付
                	YSDK.showTip("支付被取消");
                	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                    break;
                case eFlag.Pay_Param_Error:
                	YSDK.showTip("支付参数错误");
                	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL,"支付失败，参数错误"+ret.toString());
                    break;
                case eFlag.Error:
                default:
                	YSDK.showTip("支付异常");
                	U8SDK.getInstance().onPayResult(U8Code.CODE_PAY_FAIL, "支付异常"+ret.toString());
                    break;
            }
        }
	}


	@Override
	public byte[] OnCrashExtDataNotify() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String OnCrashExtMessageNotify() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void OnLoginNotify(UserLoginRet ret) {
		
		Log.d("U8SDK", "login notify:"+ret.flag);
		YSDK.getInstance().afterLogout = false;
		
		if(!YSDK.getInstance().useLogin){
			Log.d("U8SDK", "curr config no login need. OnLoginNotify just return.");
			return;
		}
		
        switch (ret.flag) {
        case eFlag.Succ:
        	YSDK.getInstance().letUserLogin(false);
            break;
        // 游戏逻辑，对登录失败情况分别进行处理
        case eFlag.QQ_UserCancel:
            //YSDK.showTip("用户取消授权，请重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "user cancel");
            //YSDK.logout();
            
            break;
        case eFlag.QQ_LoginFail:
            //YSDK.showTip("QQ登录失败，请重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            
            break;
        case eFlag.QQ_NetworkErr:
            //YSDK.showTip("QQ登录异常，请重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            
            break;
        case eFlag.QQ_NotInstall:
            YSDK.showTip("手机未安装手Q，请安装后重试");
            Log.d("U8SDK", "QQ not install . login failed.");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            break;
        case eFlag.QQ_NotSupportApi:
            YSDK.showTip("手机手Q版本太低，请升级后重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            
            break;
        case eFlag.WX_NotInstall:
            YSDK.showTip("手机未安装微信，请安装后重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            break;
        case eFlag.WX_NotSupportApi:
            YSDK.showTip("手机微信版本太低，请升级后重试");
            
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            break;
        case eFlag.WX_UserCancel:
            //YSDK.showTip("用户取消授权，请重试");
            //U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            
            break;
        case eFlag.WX_UserDeny:
            YSDK.showTip("用户拒绝了授权，请重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            break;
        case eFlag.WX_LoginFail:
            YSDK.showTip("微信登录失败，请重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            if (!YSDK.getInstance().isStartLogined()){
                YSDKApi.logout();
            }
            break;
        case eFlag.Login_TokenInvalid:
            YSDK.showTip("您尚未登录或者之前的登录已过期，请重试");
        	if(YSDK.getInstance().isStartLogined()){
        		Log.d("U8SDK", "token invalid after user login. now to retry");
        		YSDK.getInstance().logout();
        		switch(YSDK.getInstance().lastLoginType){
        		case YSDK.LOGIN_TYPE_QQ:
        			Log.d("U8SDK", "token invalid after user login. now to retry qq login");
        			YSDKApi.login(ePlatform.QQ);
        			break;
        		case YSDK.LOGIN_TYPE_WX:
        			Log.d("U8SDK", "token invalid after user login. now to retry wx login");
        			YSDKApi.login(ePlatform.WX);
        			break;
        		case YSDK.LOGIN_TYPE_GUEST:
        			Log.d("U8SDK", "token invalid after user login. now to retry guest login");
        			YSDKApi.login(ePlatform.Guest);
        			break;
        		default:
        			Log.d("U8SDK", "token invalid after user login. now to retry default login");
        			YSDK.getInstance().login(YSDK.getInstance().lastLoginType);
        			break;
        		}
        		
                //U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
        	}else{
        		try{
        			Log.d("U8SDK", "token invalid before user login. just logout");
        			YSDK.getInstance().logout();
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}

            
            break;
        case eFlag.Login_NeedRegisterRealName:
            // 未实名认证
            YSDK.showTip("您的账号没有进行实名认证，请实名认证后重试");
            //新版本，ysdk会在实名认证之后，触发登陆成功回调
//            YSDKApi.logout();
//            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            break;
        case eFlag.Login_NotRegisterRealName:
            // 显示登录界面
            YSDK.showTip("您的账号没有进行实名认证，请实名认证后重试");
            U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //YSDK.logout();
            break;
        case eFlag.Login_Free_Login_Auth_Failed:
            YSDK.showTip("免登录校验失败，请重启重试");
            break;
        case eFlag.Login_User_Logout:
            Log.d("U8SDK", "logout from sdk");
            YSDKApi.logout();
            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS,"请重新登录！");
            break;
        default:
            // 显示登录界面
        	U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
        	//YSDK.logout();
            break;
        }
		
	}


	@Override
	public void OnRelationNotify(UserRelationRet arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnWakeupNotify(WakeupRet ret) {
		
        // TODO GAME 游戏需要在这里增加处理异账号的逻辑
        if (eFlag.Wakeup_YSDKLogining == ret.flag) {
            // 用拉起的账号登录，登录结果在OnLoginNotify()中回调
        } else if (ret.flag == eFlag.Wakeup_NeedUserSelectAccount) {
            // 异账号时，游戏需要弹出提示框让用户选择需要登录的账号
        	YSDK.getInstance().showDiffLogin();
        } else if (ret.flag == eFlag.Wakeup_NeedUserLogin) {
            // 没有有效的票据，登出游戏让用户重新登录
            Log.d("U8SDK","need login");
            //YSDK.logout();
            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS,"请重新登录！");
        } else {
        	//YSDK.logout();
            U8SDK.getInstance().onLogoutResult(U8Code.CODE_LOGOUT_SUCCESS,"请重新登录！");
        }
	}

    @Override
    public void onLoginLimitNotify(AntiAddictRet ret) {
        Log.d("U8SDK", "ysdk onLoginLimitNotify called."+ret.ret+";family:"+ret.ruleFamily);
        if (AntiAddictRet.RET_SUCC == ret.ret) {
            // 防沉迷指令
            switch (ret.ruleFamily) {
                case AntiAddictRet.RULE_WORK_TIP:
                case AntiAddictRet.RULE_WORK_NO_PLAY:
                case AntiAddictRet.RULE_HOLIDAY_TIP:
                case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                case AntiAddictRet.RULE_NIGHT_NO_PLAY:
                case AntiAddictRet.RULE_GUEST:
                default:
                    YSDK.getInstance().executeInstruction(ret);
                    break;
            }
        }
    }

    @Override
    public void onTimeLimitNotify(AntiAddictRet ret) {

        Log.d("U8SDK", "ysdk onLoginLimitNotify called."+ret.ret+";family:"+ret.ruleFamily);

        if (AntiAddictRet.RET_SUCC == ret.ret) {
            // 防沉迷指令
            switch (ret.ruleFamily) {
                case AntiAddictRet.RULE_WORK_TIP:
                case AntiAddictRet.RULE_WORK_NO_PLAY:
                case AntiAddictRet.RULE_HOLIDAY_TIP:
                case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                case AntiAddictRet.RULE_NIGHT_NO_PLAY:
                case AntiAddictRet.RULE_GUEST:
                default:
                    YSDK.getInstance().executeInstruction(ret);
                    break;
            }

        }
    }
}
