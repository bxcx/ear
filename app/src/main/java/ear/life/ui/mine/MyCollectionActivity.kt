package ear.life.ui.mine

import android.view.View
import android.view.ViewGroup
import com.hm.hmlibrary.ui.article.Article
import com.hm.hmlibrary.ui.article.ArticleListModel
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import com.hm.library.util.ArgumentUtil
import ear.life.R
import ear.life.app.App
import ear.life.http.FavoriteModel
import ear.life.ui.article.ArticleDetailActivity
import kotlinx.android.synthetic.main.item_collection.view.*
import org.jetbrains.anko.act
import org.jetbrains.anko.onClick

class MyCollectionActivity : BaseListActivity<Article, MyCollectionActivity.CollectionHolder>() {
    override fun getView(parent: ViewGroup?, position: Int): CollectionHolder = CollectionHolder(getItemView(parent))

    override fun setUIParams() {
        title = "我的收藏"
        itemResID = R.layout.item_collection
    }


    override fun loadData() {

        val params = App.createParams
        params.put("json", "user/get_favorite")

        HMRequest.go<ArticleListModel>(params = params) {
            loadCompleted(it?.posts)
        }
    }

    inner class CollectionHolder(itemView: View) : BaseViewHolder<Article>(itemView) {
        override fun setContent(position: Int) {
            itemView.setBackgroundColor(if (position % 2 == 0) 0xffffffff.toInt() else 0xffF0F0F0.toInt())
            itemView.tv_title.text = data.title
            itemView.tv_date.text = data.date
            itemView.layout.setOnClickListener {
                startActivity<ArticleDetailActivity>(ArgumentUtil.TITLE to data.title!!, ArgumentUtil.ID to data.id!!)
            }

            itemView.iv_collect.onClick {
                if (!App.checkCookie(act)) {
                    return@onClick
                }

                val params = App.createParams
                params.put("json", "user/post_favorite")
                params.put("post_id", data.id!!)
                params.put("doAction", true)
                showLoading()

                HMRequest.go<FavoriteModel>(params = params, activity = act) {
                    cancelLoading()
                    itemView.iv_collect.setImageResource(if (it!!.after) R.drawable.icon_collected else R.drawable.icon_collect)
                }
            }
        }
    }
}
