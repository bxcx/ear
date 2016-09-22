package ear.life.app

import android.app.Activity
import com.hm.library.app.Cacher
import com.hm.library.app.HMApp
import com.hm.library.http.HMRequest
import com.hm.library.http.Method
import ear.life.http.CookieModel
import ear.life.http.HttpServerPath
import ear.life.http.UserModel
import ear.life.ui.LoginActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

/**
 * App
 *
 * himi on 2016-09-14 12:22
 * version V1.0
 */
class App : HMApp() {




    companion object {
        val COOKIE: String = "COOKIE"
        var cookie: CookieModel? = null
        var user: UserModel? = null

        fun checkCookie(act: Activity): Boolean {
            if (cookie == null) {
                act.toast("请先登录")
                act.startActivity<LoginActivity>()
                return false
            }
            return true
        }

        fun updateCookie(cookie: CookieModel?) {
            if (cookie == null || !cookie.hm_valid) {
                this.cookie = null
                this.user = null
                Cacher[COOKIE] = null
            } else {
                this.cookie = cookie
                this.user = cookie.user
                Cacher[COOKIE] = cookie
            }
            HMRequest.params = createParams
        }

        val createParams: HashMap<String, Any>
            get() {
                val params = HashMap<String, Any>()
                if (cookie != null) {
                    params.put("cookie", cookie!!.cookie)
                }
                return params
            }

        val createNameAndEmailParams: HashMap<String, Any>
            get() {
                val params = HashMap<String, Any>()
                if (cookie != null) {
                    params.put("cookie", cookie!!.cookie)
                    params.put("name", cookie!!.user.nickname)
                    params.put("email", cookie!!.user.email)
                }
                return params
            }
    }

    override fun onCreate() {
        super.onCreate()
        updateCookie(Cacher[COOKIE])

        HMRequest.method = Method.POST
        HMRequest.server = HttpServerPath.MAIN
//        OkHttpUtils.getInstance().setCertificates(Buffer().writeUtf8(CER).inputStream())
    }
}