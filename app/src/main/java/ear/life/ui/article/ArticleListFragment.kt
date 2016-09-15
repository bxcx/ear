package ear.life.ui.article


import android.view.ViewGroup
import com.hm.hmlibrary.ui.common.article.ArticleHolder
import com.hm.hmlibrary.ui.common.article.ArticleListModel
import com.hm.hmlibrary.ui.common.article.ArticleListModel.ArticleModel
import com.hm.library.base.BaseListFragment
import com.hm.library.http.HMRequest
import ear.life.R

class ArticleListFragment(override var itemResID: Int = R.layout.item_article) : BaseListFragment<ArticleModel, ArticleHolder>() {

    override fun setUIParams() {
        default_params_pageSize = "count"
    }

    override fun loadData() {
        val params = listParams
        params.put("json", "get_posts")

        HMRequest.go<ArticleListModel>(params = params, needCallBack = true) {
            //仅需要调用这一个方法完成上下拉功能
            loadCompleted(it?.posts)
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))
 
}



