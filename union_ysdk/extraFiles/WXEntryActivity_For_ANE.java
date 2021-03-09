package com.u8.sdk.ysdk.activity;


import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.ePlatform;

public class WXEntryActivity extends com.tencent.ysdk.module.user.impl.wx.YSDKWXEntryActivity {

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        YSDKApi.handleIntent(intent);
    }

    public void onResp(BaseResp paramBaseResp) {
        try {
            Log.d("U8SDK", "onResp called");
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("wx_callback", "onResp");
            intent.putExtra("wx_errCode", paramBaseResp.errCode);
            intent.putExtra("wx_errStr", paramBaseResp.errStr);
            intent.putExtra("wx_transaction", paramBaseResp.transaction);
            intent.putExtra("wx_openId", paramBaseResp.openId);
            intent.putExtra("platformId", ePlatform.WX);
            if (paramBaseResp instanceof SendAuth.Resp) {
                SendAuth.Resp resp = (SendAuth.Resp)paramBaseResp;
                Log.d("U8SDK", "wx code: " + resp.code);
                intent.putExtra("wx_code", resp.code);
                intent.putExtra("country", resp.country);
                intent.putExtra("lang", resp.lang);
            }
            if (paramBaseResp instanceof com.tencent.mm.opensdk.modelmsg.SendMessageToWX.Resp && !TextUtils.isEmpty(paramBaseResp.transaction) && paramBaseResp.transaction.startsWith("WXShare")) {
                Log.d("U8SDK", "wx share code: " + paramBaseResp.errCode);
                intent.putExtra("wx_share_err_code", paramBaseResp.errCode);
            }

            YSDKApi.handleIntent(intent);

            if ("wechatAddCardToWXCardPackage".equals(paramBaseResp.transaction))
                startActivity(intent);
            finish();
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

}
