package ear.life.ui

import android.app.Activity
import android.content.Intent
import com.hm.library.base.BaseMainActivity
import com.hm.library.http.HMRequest
import com.hm.library.resource.draggridview.DragGridActivity
import ear.life.R
import ear.life.app.App
import ear.life.http.CookieValidModel
import ear.life.ui.article.ArticleFragment
import ear.life.ui.music.LinghtMusicFragment
import ear.life.ui.nature.NatureFragment
import kotlinx.android.synthetic.main.activity_main_tab.*

class MainTabActivity(override var layoutResID: Int = R.layout.activity_main_tab) : BaseMainActivity() {

    //妙笔
    lateinit var articleFragment: ArticleFragment
    //纯音
    lateinit var lightMusicFragment: LinghtMusicFragment
    //自然
    lateinit var natureFragment: NatureFragment

    override fun setUIParams() {

        App.ContentResolver = contentResolver

        articleFragment = ArticleFragment()
        lightMusicFragment = LinghtMusicFragment()
        natureFragment = NatureFragment()

        mTabs.add(articleFragment)
        mTabs.add(lightMusicFragment)
        mTabs.add(natureFragment)
        mTabs.add(BlankFragment())
    }

    override fun initComplete() {
        //三秒后执行
        runDelayed({
            //模拟红点
            main_tabpage.setIndicateDisplay(3, true)
        }, 3000)

        //是否可以滑动，滑动是否有渐变效果
        main_tabpage.setStyle(true, true)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                DragGridActivity.Selection -> {
                    articleFragment.onActivityResult(requestCode, resultCode, data)
                }
                LinghtMusicFragment.Action_Import -> lightMusicFragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}