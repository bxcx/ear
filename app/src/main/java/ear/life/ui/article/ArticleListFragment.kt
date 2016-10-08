package ear.life.ui.article


import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.hm.hmlibrary.ui.article.Article
import com.hm.hmlibrary.ui.article.ArticleHolder
import com.hm.hmlibrary.ui.article.ArticleListModel
import com.hm.hmlibrary.ui.article.CategorieListModel
import com.hm.library.app.Cacher
import com.hm.library.base.BaseListFragment
import com.hm.library.http.HMRequest
import com.hm.library.resource.sweetsheet.entity.MenuEntity
import com.hm.library.resource.sweetsheet.sweetpick.BlurEffect
import com.hm.library.resource.sweetsheet.sweetpick.RecyclerViewDelegate
import com.hm.library.resource.sweetsheet.sweetpick.SweetSheet
import com.hm.library.util.ArgumentUtil

import ear.life.R
import kotlinx.android.synthetic.main.fragment_article_list.*
import org.jetbrains.anko.onClick
import java.util.*

class ArticleListFragment(override var layoutResID: Int = R.layout.fragment_article_list, override var itemResID: Int = R.layout.item_article) : BaseListFragment<Article, ArticleHolder>() {

    companion object {
        fun newInstance(id: Int): ArticleListFragment {
            val fragment = ArticleListFragment()
            val args = Bundle()
            args.putInt(ArgumentUtil.ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    var parentID: Int = -1
    var categoryID: Int = -1

    override fun checkParams(): Boolean {
        categoryID = arguments.getInt(ArgumentUtil.ID, -1)
        parentID = categoryID
        return id != -1
    }

    override fun setUIParams() {
        default_params_pageSize = "count"
    }

    override fun initUI() {
        super.initUI()
        if (parentID == -1) {
            iv_filter.visibility = View.GONE
        } else {
            iv_filter.visibility = View.VISIBLE
            iv_filter.onClick {
                showSheet()
            }
        }

    }

    var mSweetSheet: SweetSheet? = null
    fun showSheet() {
        if (mSweetSheet != null)
            return

        mSweetSheet = SweetSheet(layout_content)

        val categories: ArrayList<CategorieListModel.CategorieModel>? = Cacher[ArticleFragment.ChannelAll]

        if (categories != null && categories.size > 0) {
            val children = categories.filter { it.parent == parentID } as ArrayList<CategorieListModel.CategorieModel>
            val all = CategorieListModel.CategorieModel(parentID, "", "全部", "", 0, -1)
            children.add(0, all)
            val list: ArrayList<MenuEntity> = ArrayList()
            for (channel in children) {
                val m: MenuEntity = MenuEntity()
                m.title = channel.title
                list.add(m)
            }

            mSweetSheet?.setMenuList(list)
            mSweetSheet?.delegate = object : RecyclerViewDelegate(true) {
                override fun dismiss() {
                    super.dismiss()
                    runDelayed({
                        mSweetSheet = null
                    }, 1000)
                }
            }
            mSweetSheet?.setBackgroundEffect(BlurEffect(8f))
            mSweetSheet?.setOnMenuItemClickListener { position, menuEntity1 ->
                categoryID = children[position].id
                showLoading()
                loadRefresh()
                true
            }

            mSweetSheet?.show()
        }
    }

    override fun loadData() {
        val params = listParams
        if (categoryID == -1) {
            params.put("json", "get_posts")
        } else {
            params.put("json", "get_category_posts")
            params.put("id", categoryID)
        }

        HMRequest.go<ArticleListModel>(params = params, needCallBack = true) {
            cancelLoading()
            loadCompleted(it?.posts)
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))

}



