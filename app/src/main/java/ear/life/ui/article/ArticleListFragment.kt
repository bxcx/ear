package ear.life.ui.article


import android.os.Bundle
import android.view.ViewGroup
import com.hm.hmlibrary.ui.common.article.ArticleHolder
import com.hm.hmlibrary.ui.common.article.ArticleListModel
import com.hm.hmlibrary.ui.common.article.ArticleListModel.ArticleModel
import com.hm.library.base.BaseListFragment
import com.hm.library.http.HMRequest
import com.hm.library.util.ArgumentUtil
import ear.life.R

class ArticleListFragment(override var itemResID: Int = R.layout.item_article) : BaseListFragment<ArticleModel, ArticleHolder>() {

    companion object {
        fun newInstance(id: Int): ArticleListFragment {
            val fragment = ArticleListFragment()
            val args = Bundle()
            args.putInt(ArgumentUtil.ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    var channelID: Int = -1

    override fun checkParams(): Boolean {
        channelID = arguments.getInt(ArgumentUtil.ID)
        return id != -1
    }

    override fun setUIParams() {
        default_params_pageSize = "count"
    }

    override fun loadData() {
        val params = listParams
        if (channelID == -1) {
            params.put("json", "get_posts")
        } else {
            params.put("json", "get_category_posts")
            params.put("id", channelID)
        }

        HMRequest.go<ArticleListModel>(params = params, needCallBack = true) {
            //仅需要调用这一个方法完成上下拉功能
            loadCompleted(it?.posts)
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))

}



