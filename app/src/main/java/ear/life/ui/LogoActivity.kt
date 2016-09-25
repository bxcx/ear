package ear.life.ui

import com.hm.library.base.BaseActivity
import com.hm.library.http.HMRequest
import ear.life.R
import ear.life.app.App
import ear.life.http.CookieValidModel
import org.jetbrains.anko.startActivity

class LogoActivity : BaseActivity() {

    override fun setUIParams() {
        App.ContentResolver = contentResolver
        layoutResID = R.layout.activity_logo
        hideActionBar = true
        immersedStatusbar = true
    }

    override fun initUI() {
        super.initUI()

        runDelayed({
            startActivity<MainTabActivity>()
            finish()
        }, 3000)

        if (App.cookie != null) {
            val params = App.createParams
            params.put("json", "user/validate_auth_cookie")
            HMRequest.go<CookieValidModel>(params = params, needCallBack = true) {

                if (it == null || !it.valid) {
                    showToast("登录信息已过期")
                    App.updateCookie(null)
                }
            }
        }
    }

}
