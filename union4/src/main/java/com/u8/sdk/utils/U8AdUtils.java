package com.u8.sdk.utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class U8AdUtils {

    private static int currAdId = 0;

    /**
     * 生成banner广告的容器，在页面底部
     * @param activity
     * @param pos  banner所在位置， 1：顶部；2：底部
     * @return
     */
    public static ViewGroup generateBannerViewContainer(Activity activity, int pos){

        View decorView = activity.getWindow().getDecorView();
        FrameLayout contentParent =
                (FrameLayout) decorView.findViewById(android.R.id.content);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.FILL_HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentParent.addView(layout, layoutParams);
        //layout_gravity should be set after addView called.
        if(pos == 1){
            ((FrameLayout.LayoutParams)layout.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        }else{
            ((FrameLayout.LayoutParams)layout.getLayoutParams()).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
        layout.requestLayout();

        return layout;
    }

    public static void destroySelf(View child){

        if (child != null){
            ViewGroup parent = (ViewGroup)child.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                parent.removeView(child);
            }
        }
    }

    /**
     * 生成当前广告唯一ID， 仅仅当前内存中使用
     * @return
     */
    public static int nextAdId(){

        return ++currAdId;
    }

}
