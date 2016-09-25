package ear.life.ui

import android.app.Activity
import android.view.View
import com.hm.library.base.BaseActivity
import com.hm.library.expansion.isEmail
import com.hm.library.expansion.length
import com.hm.library.expansion.matches
import com.hm.library.http.HMRequest
import com.hm.library.resource.ViewTool
import com.hm.library.resource.view.TipsToast.TipType
import ear.life.R
import ear.life.app.App
import ear.life.extension.showSoftInput
import ear.life.http.BaseModel
import ear.life.http.CookieModel
import ear.life.http.NoncedModel
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.onClick
import java.util.*

class LoginActivity : BaseActivity() {

    private enum class Type {
        Login, Register, Forget
    }

    private var currentType: Type = Type.Login

    override fun setUIParams() {
        layoutResID = R.layout.activity_login
        hideActionBar = true
        //沉浸式状态栏
        immersedStatusbar = true
    }

    override fun initUI() {
        super.initUI()

        //登录
        btn_login.onClick {
            when (currentType) {
                Type.Login -> doLogin()
                Type.Register -> doRegister()
                Type.Forget -> doForget()
            }
        }

        //注册
        btn_reg.onClick {
            if (currentType != Type.Register) {
                showRegister()
            } else {
                showRegister(false)
            }
        }
        //忘记密码
        btn_forget.onClick {
            if (currentType != Type.Forget) {
                showForget()
            } else {
                showForget(false)
            }
        }
    }

    //登录
    fun doLogin() {
        ed_id.setText(ed_id.text.toString().replace(" ", ""))
        if (!ed_id.text.length(3, 18)) {
            showTips(TipType.Error, "账号长度为3~18位")
            ed_id.text.clear()
            ed_id.showSoftInput()
            return
        } else if (!ed_pwd.text.length(6, 18)) {
            showTips(TipType.Error, "密码长度为6~18位")
            ed_pwd.text.clear()
            ed_repwd.text.clear()
            ed_pwd.showSoftInput()
            return
        }

        showLoading()

        val params = HashMap<String, Any>()
        params.put("json", "user/generate_auth_cookie")
        params.put("username", ed_id.text.trim())
        params.put("password", ed_pwd.text)

        //这里如果登录失败时需要提示用户, 所以将needCallBack设置为true
        HMRequest.go<CookieModel>(params = params, needCallBack = true) {
            cancelLoading()

            if (it == null) {
                showTips(TipType.Error, "账号或密码错误")
            } else {
                //登录成功, 可在本地缓存cookie, 并更新HMRequest中默认的params
                App.updateCookie(it)
                showTips(TipType.Success, "登录成功")
                setResult(Activity.RESULT_OK)
                finish(500)
            }
        }
    }

    //注册
    fun showRegister(show: Boolean = true) {
        if (show) {
            currentType = Type.Register
            v_shade.visibility = View.VISIBLE
            ed_pwd.visibility = View.VISIBLE
            ed_repwd.visibility = View.VISIBLE
            ed_email.visibility = View.VISIBLE
            ed_id.hint = "请输入账号"
            ed_email.hint = "接收密码的邮箱"

            btn_forget.text = "忘记密码"
            btn_reg.text = "返回"
            btn_login.text = "注册"
        } else {
            currentType = Type.Login
            v_shade.visibility = View.GONE
            ed_pwd.visibility = View.VISIBLE
            ed_repwd.visibility = View.GONE
            ed_email.visibility = View.GONE
            ed_id.hint = "请输入耳朵账号"
            btn_reg.text = "注册新用户"
            btn_login.text = "登录"

            ed_repwd.text.clear()
            ed_email.text.clear()
        }
    }

    fun checkRegister(): Boolean {
        ed_id.setText(ed_id.text.toString().replace(" ", ""))
        if (!ed_id.text.length(3, 18)) {
            showTips(TipType.Error, "账号长度为3~18位")
            ed_id.text.clear()
            ed_id.showSoftInput()
            return false
        } else if (!ed_pwd.text.length(6, 18)) {
            showTips(TipType.Error, "密码长度为6~18位")
            ed_pwd.text.clear()
            ed_repwd.text.clear()
            ed_pwd.showSoftInput()
            return false
        } else if (!ed_pwd.text.matches(ed_repwd.text)) {
            showTips(TipType.Error, "两次密码不一致")
            ed_pwd.text.clear()
            ed_repwd.text.clear()
            ed_pwd.showSoftInput()
            return false
        } else if (!ed_email.text.isEmail) {
            showTips(TipType.Error, "邮箱格式不正确")
            ed_email.text.clear()
            ed_email.showSoftInput()
            return false
        }
        return true
    }

    fun doRegister() {
        ViewTool.hideSoftInputFromWindow(this)
        if (!checkRegister()) return

        showLoading()
        val params = HashMap<String, Any>()
        params.put("json", "get_nonce")
        params.put("controller", "User")
        params.put("method", "register")
        HMRequest.go<NoncedModel>(params = params, activity = this) {
            params.clear()
            params.put("json", "user/register")
            params.put("username", ed_id.text)
            params.put("display_name", ed_id.text)
            params.put("user_pass", ed_pwd.text)
            params.put("email", ed_email.text)
            params.put("nonce", it!!.nonce)

            HMRequest.go<BaseModel>(params = params, activity = this) {
                showTips(TipType.Smile, "注册成功")

                showRegister(false)
                doLogin()
            }
        }
    }

    //忘记密码
    fun showForget(show: Boolean = true) {
        if (show) {
            currentType = Type.Forget
            v_shade.visibility = View.VISIBLE
            ed_email.visibility = View.VISIBLE

            ed_pwd.visibility = View.GONE
            ed_repwd.visibility = View.GONE

            ed_email.hint = "注册时填写的邮箱"

            btn_forget.text = "返回"
            btn_reg.text = "注册新用户"
            btn_login.text = "发送邮件"
        } else {
            currentType = Type.Login
            v_shade.visibility = View.GONE
            ed_pwd.visibility = View.VISIBLE
            ed_repwd.visibility = View.GONE
            ed_email.visibility = View.GONE
            ed_id.hint = "请输入耳朵账号"
            btn_forget.text = "忘记密码"
            btn_login.text = "登录"

            ed_repwd.text.clear()
            ed_email.text.clear()
        }
    }

    fun checkForget(): Boolean {
        ed_id.setText(ed_id.text.toString().replace(" ", ""))
        if (!ed_id.text.length(3, 18)) {
            showTips(TipType.Error, "账号长度为3~18位")
            ed_id.text.clear()
            ed_id.showSoftInput()
            return false
        } else if (!ed_email.text.isEmail) {
            showTips(TipType.Error, "邮箱格式不正确")
            ed_email.text.clear()
            ed_email.showSoftInput()
            return false
        }
        return true
    }

    fun doForget() {
        ViewTool.hideSoftInputFromWindow(this)
        if (!checkForget()) return

        showLoading()
        val params = HashMap<String, Any>()
        params.put("json", "user/retrieve_password")
        params.put("user_login", ed_id.text)
        params.put("email", ed_email.text)

        HMRequest.go<BaseModel>(params = params, activity = this) {
            cancelLoading()
            showTips(TipType.Success, "重置邮件已发送")

            showForget(false)
        }
    }
}
