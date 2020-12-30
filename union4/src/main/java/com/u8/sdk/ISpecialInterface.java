package com.u8.sdk;

import android.app.Activity;

//处理部分渠道特殊需求或者接口
public interface ISpecialInterface {

	//是否从渠道的游戏中心启动/Oppo
	public boolean isFromGameCenter(Activity context);
	
	//显示游戏中心或者社区/Oppo
	public void showGameCenter(Activity context);

	//跳转到论坛帖子
	public void showPostDetail(Activity context, String postId, String extra);
	
	//显示特权接口/YSDK for V+, bbs
	public void performFeature(Activity context, String type);

	//调用部分渠道特殊业务接口， 具体参数传啥， 根据不同接口的文档说明来进行传递
	public void callSpecailFunc(Activity context, String funcName, SDKParams params);
}
