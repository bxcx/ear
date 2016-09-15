package ear.life.ui.article


import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.hm.library.base.BaseListFragment
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import ear.life.R
import ear.life.http.BaseModel
import ear.life.ui.article.ArticleListFragment.ArticleHolder
import ear.life.ui.article.ArticleListFragment.HttpPostListModel.PostModel
import kotlinx.android.synthetic.main.item_article.view.*
import org.jetbrains.anko.onClick
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ArticleListFragment(override var itemResID: Int = R.layout.item_article) : BaseListFragment<PostModel, ArticleHolder>() {

    override fun setUIParams() {
        default_params_pageSize = "count"
    }

    override fun loadData() {
        val params = listParams
        params.put("json", "get_posts")

        HMRequest.go<HttpPostListModel>(params = params, needCallBack = true) {
            //仅需要调用这一个方法完成上下拉功能
            loadCompleted(it?.posts)
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))

    inner class ArticleHolder(itemView: View) : BaseViewHolder<PostModel>(itemView) {
        override fun setContent(position: Int) {
            itemView.tv_title.text = data.title
            itemView.tv_excerpt.text = data.excerpt

            itemView.onClick { showToast(data.url) }
        }
    }

    class HttpPostListModel(var posts: ArrayList<PostModel>) : BaseModel() {
        class PostModel(var id: Int, var url: String, var title: String, var excerpt: String)
    }

}



