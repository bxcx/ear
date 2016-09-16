package ear.life.ui

import com.hm.library.base.BaseMainActivity
import ear.life.R
import ear.life.ui.article.ArticleFragment
import kotlinx.android.synthetic.main.activity_main_tab.*

class MainTabActivity(override var layoutResID: Int = R.layout.activity_main_tab) : BaseMainActivity() {

    lateinit var titles: Array<String>

    override fun setUIParams() {
        titles = resources.getStringArray(R.array.bottom_bar_labels)
        titles.forEachIndexed { index, title ->
            if (index == 0)
                mTabs.add(ArticleFragment())
            mTabs.add(BlankFragment())
        }
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

    override fun onTabSelected(index: Int) {
        tv_title.text = titles[index]
    }
}