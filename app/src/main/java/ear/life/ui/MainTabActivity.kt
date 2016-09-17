package ear.life.ui

import android.app.Activity
import android.content.Intent
import com.hm.library.base.BaseMainActivity
import com.hm.library.resource.draggridview.DragGridActivity
import ear.life.R
import ear.life.ui.article.ArticleFragment
import kotlinx.android.synthetic.main.activity_main_tab.*

class MainTabActivity(override var layoutResID: Int = R.layout.activity_main_tab) : BaseMainActivity() {

    //妙笔
    lateinit var articleFragment: ArticleFragment

    override fun setUIParams() {
        articleFragment = ArticleFragment()

        mTabs.add(articleFragment)
        mTabs.add(BlankFragment())
        mTabs.add(BlankFragment())
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                DragGridActivity.Selection -> {
                    articleFragment.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }
}