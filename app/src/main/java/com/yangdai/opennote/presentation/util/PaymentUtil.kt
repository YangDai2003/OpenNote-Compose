package com.yangdai.opennote.presentation.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.net.URISyntaxException

/**
 * @author 30415
 */
object PaymentUtil {
    /**
     * 旧版支付宝二维码通用 Intent Scheme Url 格式
     */
    private const val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

    /**
     * 打开转账窗口
     * 旧版支付宝二维码方法，需要使用 [...](https://fama.alipay.com/qrcode/index.htm) 网站生成的二维码
     * 这个方法最好，但在 2016 年 8 月发现新用户可能无法使用
     *
     * @param activity Parent Activity
     * @param urlCode  手动解析二维码获得地址中的参数，例如 [...](https://qr.alipay.com/aehvyvf4taxxxxxxx) 最后那段
     */
    fun startAlipayClient(activity: Activity?, urlCode: String) {
        if (activity != null)
            startIntentUrl(activity, INTENT_URL_FORMAT.replace("{urlCode}", urlCode))
    }

    /**
     * 打开 Intent Scheme Url
     *
     * @param activity      Parent Activity
     * @param intentFullUrl Intent 跳转地址
     */
    fun startIntentUrl(activity: Activity, intentFullUrl: String?) {
        try {
            val intent = Intent.parseUri(
                intentFullUrl,
                Intent.URI_INTENT_SCHEME
            )
            activity.startActivity(intent)
        } catch (e: URISyntaxException) {
            Log.e("PaymentUtil", "startIntentUrl: ", e)
        } catch (e: ActivityNotFoundException) {
            Log.e("PaymentUtil", "startIntentUrl: ", e)
        }
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     *
     * @param context Context
     * @return 支付宝客户端是否已安装
     */
    fun isInstalledPackage(context: Context): Boolean {
        val uri = Uri.parse("alipays://platformapi/startApp")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val componentName = intent.resolveActivity(context.packageManager)
        return componentName != null
    }
}