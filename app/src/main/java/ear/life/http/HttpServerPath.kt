package ear.life.http

import android.text.TextUtils
import com.hm.library.http.HMExceptionInfo
import ear.life.app.App
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * HttpServerPath
 *
 * himi on 2016-09-14 22:01
 * version V1.0
 */
object HttpServerPath : HMExceptionInfo {

    val MAIN = "https://ear.life"
    val Server_Nature = MAIN + "/api/getNature.php"

    override fun parseError(e: Exception): String {
        var domain = when (e) {
            is ConnectException,
            is UnknownHostException
            -> "网络不佳"
            else -> "未知错误"
        }
        return domain
    }

    override fun onError(message: String) {
        if (!TextUtils.isEmpty(message) && message.contains("登录")) {
            App.updateCookie(null)
        }
    }

}