package com.u8.sdk.plugin;

import android.app.Activity;

import com.u8.sdk.ISpecialInterface;
import com.u8.sdk.SDKParams;
import com.u8.sdk.U8SDK;

//渠道特殊接口调用， 进提供渠道接入工程使用， 插件不要用该接口
public class U8SpecialInterface implements ISpecialInterface {

    private static U8SpecialInterface instance;

    private ISpecialInterface plugin;

    private U8SpecialInterface() {

    }

    public static U8SpecialInterface getInstance() {
        if (instance == null) {
            instance = new U8SpecialInterface();
        }
        return instance;
    }

    public void setSpecialInterface(ISpecialInterface plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFromGameCenter(Activity activity) {
        if (plugin != null) {
            return plugin.isFromGameCenter(activity);
        }
        return false;
    }

    @Override
    public void showGameCenter(Activity context) {
        if (plugin != null) {
            plugin.showGameCenter(context);
        }
    }

    @Override
    public void showPostDetail(Activity context, String postId, String extra) {
        if (plugin != null) {
            plugin.showPostDetail(context, postId, extra);
        }
    }

    @Override
    public void performFeature(Activity context, String type) {
        if (plugin != null) {
            plugin.performFeature(context, type);
        }
    }


    //调用部分渠道特殊业务接口， 具体参数传啥， 根据不同接口的文档说明来进行传递
    @Override
    public void callSpecailFunc(final Activity context, final String funcName, final SDKParams params) {
        if (plugin != null) {
            U8SDK.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    plugin.callSpecailFunc(context, funcName, params);
                }
            });
        }
    }


}
