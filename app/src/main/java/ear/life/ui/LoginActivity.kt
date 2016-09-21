package ear.life.ui

import android.text.TextUtils
import com.hm.library.base.BaseActivity
import com.hm.library.http.HMRequest
import com.hm.library.resource.view.TipsToast.TipType
import ear.life.R
import ear.life.http.CookieModel
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.onClick
import java.util.*

class LoginActivity : BaseActivity() {

    override fun setUIParams() {
        layoutResID = R.layout.activity_login
        hideActionBar = true
        //沉浸式状态栏
        immersedStatusbar = true
    }

    override fun initUI() {
        super.initUI()

        btn_login.onClick {
            val username = ed_id.text.trim().toString()
            val password = ed_pwd.text.toString()

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                showLoading()

                val params = HashMap<String, Any>()
                params.put("json", "user/generate_auth_cookie")
                params.put("username", username)
                params.put("password", password)

                //这里如果登录失败时需要提示用户, 所以将needCallBack设置为true
                HMRequest.go<CookieModel>(params = params, needCallBack = true) {
                    cancelLoading()

                    if (it == null) {
                        showTips(TipType.Error, "账号或密码错误")
                    } else {
                        //登录成功, 可在本地缓存cookie, 并更新HMRequest中默认的params
                        showToast(it.cookie)
                    }
                }
            } else {
                showTips(TipType.Warning, "账号密码呢?")
            }
        }
    }


}
