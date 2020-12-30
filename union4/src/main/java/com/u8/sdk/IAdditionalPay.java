package com.u8.sdk;


/**
 * IPay接口扩展
 */
public interface IAdditionalPay extends IPay {

    /**
     * 检查丢单， 纯单机渠道在再次进入游戏的时候，利用该接口做丢单检查。
     */
    public void checkFailedOrder(PayParams data);

    /**
     * 是否需要从服务器端查询支付完成状态.
     */
    public boolean needQueryResult();

}
