package ear.life.ui.mine


import android.content.Intent
import android.graphics.Color
import com.hm.library.base.BaseFragment
import com.hm.library.expansion.show
import com.hm.library.http.HMRequest
import com.hm.library.resource.view.ActionSheetDialog
import ear.life.R
import ear.life.app.App
import ear.life.http.CookieModel
import ear.life.ui.LoginActivity
import kotlinx.android.synthetic.main.fragment_mine.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.startActivityForResult

class MineFragment(override var layoutResID: Int = R.layout.fragment_mine) : BaseFragment() {

    companion object{
        val Action_Login = 20
    }

    override fun loadData() {
        if (App.user == null) {
            initUI()
            return
        }
        val params = App.createParams
        params.put("json", "user/get_currentuserinfo")
        HMRequest.go<CookieModel>(params = params) {
            App.user = it?.user
            initUI()
        }
    }

    override fun initUI() {
        super.initUI()

        layout_loginout.onClick {
            if (App.user != null) {
                ActionSheetDialog(context).builder().setTitle("确认注销当前用户吗?")
                        .addSheetItem("注销", Color.RED, {
                            App.updateCookie(null)
                            initUI()
                        })
                        .show()
            } else {
                startActivityForResult<LoginActivity>(Action_Login)
            }
        }

        iv_head.onClick {
            if (App.user == null) {
                startActivityForResult<LoginActivity>(Action_Login)
            }
        }

        if (App.user == null) {
            iv_head.setImageResource(R.drawable.ic_launcher)
            tv_name.text = "请登录"
            tv_login.text = "登录"
            return
        }

        iv_head.show(App.user!!.avatar)
        tv_name.text = App.user!!.nickname
        tv_login.text = "注销"
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        loadData()
    }

}
