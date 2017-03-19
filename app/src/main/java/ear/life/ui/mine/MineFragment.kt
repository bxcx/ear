package ear.life.ui.mine


import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.hm.library.base.BaseFragment
import com.hm.library.expansion.show
import com.hm.library.http.HMRequest
import com.hm.library.resource.view.ActionSheetDialog
import com.hm.library.resource.view.TipsToast
import com.hm.library.umeng.share.IShareCallback
import com.hm.library.umeng.share.ShareUtils
import com.umeng.socialize.media.UMImage
import ear.life.R
import ear.life.app.App
import ear.life.http.CookieModel
import ear.life.ui.LoginActivity
import kotlinx.android.synthetic.main.fragment_mine.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult

class MineFragment(override var layoutResID: Int = R.layout.fragment_mine) : BaseFragment(), View.OnClickListener {


    companion object {
        val Action_Login = 20
    }

    var su: ShareUtils? = null

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
        if (layout_feedback == null)
            return
        layout_feedback.onClick {
            try {
                FeedbackAPI.openFeedbackActivity(ctx)
            } catch(e: Exception) {
            }
        }
        layout_share.onClick {
            su = ShareUtils(act)
            su?.share("耳朵纯音乐", "http://fir.im/ear", "总有一些音乐，宠坏了我们的耳朵", UMImage(ctx, R.drawable.ic_launcher), object : IShareCallback {
                override fun onSuccess() {
                    showTips(TipsToast.TipType.Smile, "分享成功")
                }

                override fun onFaild() {
                }

                override fun onCancel() {
                }
            })
        }

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
            } else {
                showTips(TipsToast.TipType.Smile, "请前往网站修改资料")
            }
        }

        layout_collection.setOnClickListener {
            if (App.checkCookie(act)) {
                startActivity<MyCollectionActivity>()
            }
        }
        layout_yejian.setOnClickListener(this)
        layout_setting.setOnClickListener(this)
        layout_message.setOnClickListener(this)
        layout_activity.setOnClickListener(this)
        layout_join.onClick {
            if (!joinQQGroup("RT20txYU4wfHtN8g-gb68ChnNu0kYWls")) {
                showTips(TipsToast.TipType.Smile, "QQ群号：3318916")
            }
        }

        if (App.user == null) {
            iv_head.setImageResource(R.drawable.ic_launcher)
            tv_name.text = "请登录"
            tv_login.text = "登录"
            return
        }

        iv_head.show(App.user?.avatar)
        tv_name.text = App.user?.nickname
        tv_login.text = "注销"
    }

    override fun onClick(p0: View?) {
        showToast("即将开放")
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loadData()
        su?.onActivityResult(requestCode, resultCode, data)
    }

    /****************

     * 发起添加群流程。群号：耳朵纯音乐(3318916) 的 key 为： RT20txYU4wfHtN8g-gb68ChnNu0kYWls
     * 调用 joinQQGroup(RT20txYU4wfHtN8g-gb68ChnNu0kYWls) 即可发起手Q客户端申请加群 耳朵纯音乐(3318916)

     * @param key 由官网生成的key
     * *
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     */
    fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key)
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            return true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            return false
        }

    }

}
