package com.u8.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.ePlatform;

/**
 * 选择QQ、微信登录方式
 */
public class ChooseLoginTypeActivity extends Activity {

    private View btnQQ;
    private View btnWX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setFinishOnTouchOutside(false);

        int layoutID = 0;
        if (YSDK.getInstance().isCustomLogin()) {
            layoutID = getResources().getIdentifier("u8_custom_login_choice", "layout", getPackageName());
            setContentView(layoutID);
        } else {
            layoutID = getResources().getIdentifier("u8_layout_login_choice", "layout", getPackageName());
            setContentView(layoutID);
        }

        btnQQ = findViewById(getResources().getIdentifier("btn_qq", "id", getPackageName()));
        btnWX = findViewById(getResources().getIdentifier("btn_wx", "id", getPackageName()));

        btnQQ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ChooseLoginTypeActivity.this.finish();
                if (YSDKApi.isPlatformInstalled(ePlatform.QQ)) {
                    YSDK.getInstance().login(YSDK.LOGIN_TYPE_QQ);
                } else {
                    YSDK.showTip("您还没有安装QQ，请先安装QQ");
                }
            }
        });

        btnWX.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ChooseLoginTypeActivity.this.finish();
                YSDK.getInstance().login(YSDK.LOGIN_TYPE_WX);
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
