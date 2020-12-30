package com.u8.sdk.impl.activities;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.u8.sdk.impl.DefaultSDKPlatform;
import com.u8.sdk.impl.listeners.ISDKLoginListener;
import com.u8.sdk.impl.listeners.ISDKRegisterOnekeyListener;
import com.u8.sdk.impl.services.SdkManager;
import com.u8.sdk.impl.widgets.ExtendEditText;
import com.u8.sdk.impl.widgets.ExtendTextView;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.utils.StoreUtils;


public class LoginActivity extends Activity {

    private final String KEY_NAME = "u8_key_login_name";
    private final String KEY_PASSWORD = "u8_key_password";

    private boolean canTouch = true;

    private static class MyHandler extends Handler {

        private final WeakReference<LoginActivity> mActivity;
        public MyHandler(LoginActivity activity) {
            mActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mActivity.get();
            if (activity != null) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLER_KEY.INIT_LOGIN:
                        activity.startLogin();
                        break;
                    case HANDLER_KEY.BACK_LGOIN_VIEW:
                        activity.viewBack(LAYOUT_KEY.x_login_view);
                        break;
                    case HANDLER_KEY.SET_REGISTER_USER_VIEW:
                        activity.viewSet(LAYOUT_KEY.x_register_user_view);
                        break;
                }
            }
        }
    }


    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        public void onAnimationEnd(Animation arg0) {
            canTouch = true;
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationStart(Animation arg0) {
            canTouch = false;
        }
    };

    private MyHandler handler = new MyHandler(this);
    private Animation left_in;
    private Animation left_out;
    private Animation right_in;
    private Animation right_out;
    private String view_resource;


    private ExtendEditText x_login_username;
    private ExtendEditText x_login_userpwd;
    private ExtendEditText x_register_name;

    private ExtendEditText x_register_pwd;

    private boolean showLoginPwd;
    private boolean showRegUserPwd = true;

    private ImageView x_login_showpwdImg;

    @SuppressLint("WrongConstant")
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!canTouch) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService("input_method");
                this.x_login_userpwd.setClearDrawableVisible(false);
                this.x_login_username.setClearDrawableVisible(false);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v != null) && ((v instanceof ExtendEditText))) {
            int[] leftTop = new int[2];
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if ((event.getRawX() > left) && (event.getRawX() < right) && (event.getRawY() > top)
                    && (event.getRawY() < bottom)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void onBackPressed() {
        if (!canTouch)
            return;

        if (view_resource.equals(LAYOUT_KEY.x_login_view)) {
            LoginActivity.this.finish();
            overridePendingTransition(ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_appear_to_right),
                    ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_disappear_to_right));
        } else {
            handler.sendEmptyMessage(HANDLER_KEY.BACK_LGOIN_VIEW);
        }
    }

    public void startLogin() {
        view_resource = LAYOUT_KEY.x_login_view;
        findViewById(ResourceHelper.getIdentifier(this, this.view_resource)).setVisibility(View.VISIBLE);

        String name = StoreUtils.getString(this, KEY_NAME);
        String password = StoreUtils.getString(this, KEY_PASSWORD);

        if (!TextUtils.isEmpty(name)) {
            x_login_username.setText(name);
        }

        if (!TextUtils.isEmpty(password)) {
            x_login_userpwd.setText(password);
        }

        overridePendingTransition(ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_appear_to_left),ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_disappear_to_left));
    }


    private void doLogin() {
        final String username = x_login_username.getText().toString();
        final String password = x_login_userpwd.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ResourceHelper.showTip(this, "R.string.x_register_invalid_name_tip");
            return;
        }

        SdkManager.getInstance().login(username, password, new ISDKLoginListener() {
            @Override
            public void onFailed(final int code) {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ResourceHelper.showTip(LoginActivity.this, "R.string.x_login_fail");
                        DefaultSDKPlatform.getInstance().loginFailCallback();
                    }
                });
            }

            @Override
            public void onSuccess(final String id, final String name) {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        StoreUtils.putString(LoginActivity.this, KEY_NAME, username);
                        StoreUtils.putString(LoginActivity.this, KEY_PASSWORD, password);

                        ResourceHelper.showTip(LoginActivity.this, "R.string.x_login_suc");
                        DefaultSDKPlatform.getInstance().loginSucCallback(id, name);
                        LoginActivity.this.finish();
                        overridePendingTransition(ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_appear_to_right),
                                ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_disappear_to_right));

                    }
                });
            }
        });
    }


    private void doRegister(final String username, final String password, boolean isfast) {
        if (isfast) {
            SdkManager.getInstance().registerOnekey("", new ISDKRegisterOnekeyListener() {
                @Override
                public void onFailed(int code) {

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ResourceHelper.showTip(LoginActivity.this, "R.string.x_fastlogin_accout_fail");
                            x_register_name.setText("");
                            x_register_pwd.setText("");
                            DefaultSDKPlatform.getInstance().loginFailCallback();
                        }
                    });
                }

                @Override
                public void onSuccess(final String id, final String name, final String password) {

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            StoreUtils.putString(LoginActivity.this, KEY_NAME, name);
                            StoreUtils.putString(LoginActivity.this, KEY_PASSWORD, password);

                            StringBuffer sb = new StringBuffer();
                            sb.append(ResourceHelper.getString(LoginActivity.this, "R.string.x_fastlogin_accout"))
                                    .append(name).append("\n")
                                    .append(ResourceHelper.getString(LoginActivity.this, "R.string.x_fastlogin_pwd"))
                                    .append(password);
                            Toast.makeText(LoginActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();

                            DefaultSDKPlatform.getInstance().loginSucCallback(id, name);

                            LoginActivity.this.finish();
                            overridePendingTransition(
                                    ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_appear_to_right),
                                    ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_disappear_to_right));
                        }
                    });
                }
            });
        } else {
            if ((TextUtils.isEmpty(username) || TextUtils.isEmpty(password))) {
                Toast.makeText(this, ResourceHelper.getString(this, "R.string.x_register_invalid_name_tip"),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            SdkManager.getInstance().register(username, password, new ISDKLoginListener() {
                @Override
                public void onFailed(int code) {

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ResourceHelper.showTip(LoginActivity.this, "R.string.x_register_fail");
                            x_register_name.setText("");
                            x_register_pwd.setText("");
                            DefaultSDKPlatform.getInstance().loginFailCallback();
                        }
                    });
                }

                @Override
                public void onSuccess(final String id, final String name) {

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            StoreUtils.putString(LoginActivity.this, KEY_NAME, username);
                            StoreUtils.putString(LoginActivity.this, KEY_PASSWORD, password);

                            ResourceHelper.showTip(LoginActivity.this, "R.string.x_register_suc");

                            DefaultSDKPlatform.getInstance().loginSucCallback(id, name);

                            LoginActivity.this.finish();
                            overridePendingTransition(ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_appear_to_right),
                                    ResourceHelper.getIdentifier(LoginActivity.this, LAYOUT_KEY.anim_disappear_to_right));
                        }
                    });
                }
            });
        }

    }

    private void initAni() {
        this.left_in = AnimationUtils.loadAnimation(this,
                ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_appear_to_left));
        this.left_out = AnimationUtils.loadAnimation(this,
                ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_disappear_to_left));
        this.right_in = AnimationUtils.loadAnimation(this,
                ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_appear_to_right));
        this.right_out = AnimationUtils.loadAnimation(this,
                ResourceHelper.getIdentifier(this, LAYOUT_KEY.anim_disappear_to_right));
        this.left_in.setAnimationListener(this.animationListener);
        this.left_out.setAnimationListener(this.animationListener);
        this.right_in.setAnimationListener(this.animationListener);
        this.right_out.setAnimationListener(this.animationListener);
    }

    private void initLogin() {
        x_login_username = (ExtendEditText) ResourceHelper.getView(this, "R.id.x_login_username");
        x_login_userpwd = (ExtendEditText) ResourceHelper.getView(this, "R.id.x_login_userpwd");
        x_login_username.setnextedit(x_login_userpwd);
        Button x_login_go = (Button) ResourceHelper.getView(this, "R.id.x_login_go");
        x_login_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doLogin();
            }
        });
        x_login_showpwdImg = (ImageView) ResourceHelper.getView(this, "R.id.x_login_showpwdImg");
        LinearLayout x_login_showpwd = (LinearLayout) ResourceHelper.getView(this, "R.id.x_login_showpwd");
        x_login_showpwd.setEnabled(true);
        x_login_showpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginPwd = !showLoginPwd;
                if (showLoginPwd) {
                    x_login_userpwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    x_login_showpwdImg.setBackgroundResource(ResourceHelper.getIdentifier(LoginActivity.this,
                            "R.drawable.x_login_hidepwd"));
                } else {
                    x_login_userpwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    x_login_showpwdImg.setBackgroundResource(ResourceHelper.getIdentifier(LoginActivity.this,
                            "R.drawable.x_login_showpwd"));
                }
            }
        });

        TextView x_login_faststart = (TextView) ResourceHelper.getView(this, "R.id.x_login_faststart");
        x_login_faststart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LoginActivity.this.doRegister("", "", true);

            }
        });
        ExtendTextView x_login_register = (ExtendTextView) ResourceHelper.getView(this, "R.id.x_login_register");
        if (x_login_register != null) {
            x_login_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    handler.sendEmptyMessage(HANDLER_KEY.SET_REGISTER_USER_VIEW);
                }
            });
        }
        ExtendTextView x_login_findpwd = (ExtendTextView) findViewById(ResourceHelper.getIdentifier(this, "R.id.x_login_findpwd"));
        x_login_findpwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //handler.sendEmptyMessage(HANDLER_KEY.SET_FINDPWD_SELECT_VIEW);
                ResourceHelper.showTip(LoginActivity.this, "R.string.x_function_not_impl");
            }
        });
    }

    private void initRegister() {
        TextView x_register_user_jump = (TextView) findViewById(ResourceHelper.getIdentifier(this,
                "R.id.x_register_user_jump"));
        x_register_user_jump.setEnabled(true);
        x_register_user_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //handler.sendEmptyMessage(HANDLER_KEY.SET_REGISTER_PHONE_VIEW);
                ResourceHelper.showTip(LoginActivity.this, "R.string.x_function_not_impl");
            }
        });


        x_register_name = (ExtendEditText) ResourceHelper.getView(this, "R.id.x_register_name");
        x_register_pwd = (ExtendEditText) ResourceHelper.getView(this, "R.id.x_register_pwd");
        x_register_name.setnextedit(x_register_pwd);
        Button x_register_user_go = (Button) ResourceHelper.getView(this, "R.id.x_register_user_go");
        x_register_user_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegister(x_register_name.getText().toString(), x_register_pwd.getText().toString(), false);
            }
        });
        LinearLayout x_register_user_back = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this,
                "R.id.x_register_user_back"));
        x_register_user_back.setEnabled(true);
        x_register_user_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(HANDLER_KEY.BACK_LGOIN_VIEW);
            }
        });

        final ImageView x_login_showpwdImg = (ImageView) ResourceHelper.getView(this, "R.id.x_regu_showpwdImg");
        LinearLayout x_login_showpwd = (LinearLayout) ResourceHelper.getView(this, "R.id.x_regu_showpwd");
        x_login_showpwd.setEnabled(true);
        x_register_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        x_login_showpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegUserPwd = !showRegUserPwd;
                if (showRegUserPwd) {
                    x_register_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    x_login_showpwdImg.setBackgroundResource(ResourceHelper.getIdentifier(LoginActivity.this,
                            "R.drawable.x_login_hidepwd"));
                } else {
                    x_register_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    x_login_showpwdImg.setBackgroundResource(ResourceHelper.getIdentifier(LoginActivity.this,
                            "R.drawable.x_login_showpwd"));
                }
            }
        });

//		TextView x_registerphone_jump = (TextView) findViewById(ResourceHelper.getIdentifier(this,
//				"R.id.x_registerphone_jump"));
//		x_registerphone_jump.setEnabled(true);
//		x_registerphone_jump.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				handler.sendEmptyMessage(HANDLER_KEY.SET_REGISTER_USER_VIEW);
//			}
//		});
//		LinearLayout x_registerphone_back = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this,
//				"R.id.x_registerphone_back"));
//		x_registerphone_back.setEnabled(true);
//		x_registerphone_back.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				handler.sendEmptyMessage(HANDLER_KEY.BACK_LGOIN_VIEW);
//			}
//		});


    }

    private void initView() {
        initAni();
        initLogin();
        initRegister();
    }


    private void viewBack(String back_resource) {
        LinearLayout oldview = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this, this.view_resource));
        LinearLayout newview = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this, back_resource));
        oldview.setVisibility(View.GONE);
        oldview.startAnimation(this.right_out);
        newview.setVisibility(View.VISIBLE);
        newview.startAnimation(this.right_in);
        this.view_resource = back_resource;
    }

    private void viewSet(String new_resource) {
        LinearLayout oldview = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this, this.view_resource));
        LinearLayout newview = (LinearLayout) findViewById(ResourceHelper.getIdentifier(this, new_resource));
        oldview.setVisibility(View.GONE);
        oldview.startAnimation(this.left_out);
        newview.setVisibility(View.VISIBLE);
        newview.startAnimation(this.left_in);
        this.view_resource = new_resource;
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(ResourceHelper.getIdentifier(this, "R.layout.x_user_login"));
        initView();
        handler = new MyHandler(this);
        handler.sendEmptyMessage(HANDLER_KEY.INIT_LOGIN);
    }

}

class HANDLER_KEY {
    final static int BACK_FINDPWD_SELECT_VIEW = 11;
    final static int BACK_FINDPWD_ONE_VIEW = 12;
    final static int BACK_LGOIN_VIEW = 2;
    final static int INIT_LOGIN = 1;
    final static int SET_REGISTER_PHONE_VIEW = 3;
    final static int SET_REGISTER_USER_VIEW = 4;
}

class LAYOUT_KEY {
    static String anim_appear_to_left = "R.anim.x_appear_to_left";
    static String anim_appear_to_right = "R.anim.x_appear_to_right";
    static String anim_disappear_to_left = "R.anim.x_disappear_to_left";
    static String anim_disappear_to_right = "R.anim.x_disappear_to_right";
    static String x_login_view = "R.id.x_login_view";
    static String x_register_user_view = "R.id.x_register_user_view";
}
