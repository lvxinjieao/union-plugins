package com.u8.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.ePlatform;
import com.u8.sdk.log.Log;

public class ChooseLoginTypeActivity extends Activity {

    private RelativeLayout btnQQ;
    private RelativeLayout btnWX;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setFinishOnTouchOutside(false);

        int layoutID = getResources().getIdentifier("u8_layout_login_choice", "layout", getPackageName());
        setContentView(layoutID);

        this.btnQQ = ((RelativeLayout) findViewById(getResources().getIdentifier("btn_qq", "id", getPackageName())));
        this.btnWX = ((RelativeLayout) findViewById(getResources().getIdentifier("btn_wx", "id", getPackageName())));

        this.btnQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseLoginTypeActivity.this.finish();
                if (YSDKApi.isPlatformInstalled(ePlatform.QQ)) {
                    YSDK.getInstance().login(YSDK.LOGIN_TYPE_QQ);
                } else {
                    U8SDK.getInstance().onLoginResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                    YSDK.showTip("您还没有安装QQ，请先安装QQ");
                }
            }
        });

        this.btnWX.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ChooseLoginTypeActivity.this.finish();
                YSDK.getInstance().login(YSDK.LOGIN_TYPE_WX);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("U8SDK", "OnKeyDown:" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
