package ear.life.ui.article

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import com.hm.hmlibrary.ui.article.Article
import com.hm.hmlibrary.ui.article.ArticleHolder
import com.hm.hmlibrary.ui.article.ArticleListModel
import com.hm.library.base.BaseListActivity
import com.hm.library.http.HMRequest
import ear.life.R
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.onClick

class SearchActivity(override var layoutResID: Int = R.layout.activity_search,
                     override var itemResID: Int = R.layout.item_article,
                     override var hideActionBar: Boolean = true) : BaseListActivity<Article, ArticleHolder>() {
    var key: String = ""
    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))

    override fun initComplete() {
        super.initComplete()
        iv_back.onClick { finish() }
        ed_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                fragment!!.loadRefresh()
            }
        })
    }

    override fun loadData() {
        key = ed_search.text.toString()
        if (TextUtils.isEmpty(key)) {
            layout_empty.visibility = View.VISIBLE
            loadCompleted(arrayListOf())
            return
        }

        val params = fragment!!.listParams
        params.put("json", "get_search_results")
        params.put("search", key)

        HMRequest.go<ArticleListModel>(params = params, needCallBack = true) {
            if (it == null || it.posts.size == 0)
                layout_empty.visibility = View.VISIBLE
            else
                layout_empty.visibility = View.GONE
            loadCompleted(it?.posts)
        }
    }
}
