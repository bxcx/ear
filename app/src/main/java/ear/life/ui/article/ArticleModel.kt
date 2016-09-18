package com.hm.hmlibrary.ui.common.article

import android.text.TextUtils
import android.view.View
import com.hm.library.base.BaseViewHolder
import com.hm.library.expansion.show
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.HtmlUtil
import ear.life.http.AttachmentModel
import ear.life.http.BaseModel
import ear.life.http.CommentModel
import ear.life.http.UserModel
import ear.life.ui.article.ArticleDetailActivity
import kotlinx.android.synthetic.main.item_article.view.*
import org.jetbrains.anko.onClick
import java.io.Serializable
import java.util.*

class CategorieListModel : BaseModel() {

    var count: Int? = 0
    var categories: ArrayList<CategorieModel>? = null

    class CategorieModel(var id: Int,
                         var slug: String,
                         var title: String,
                         var description: String,
                         var parent: Int,
                         var post_count: Int
    ): Serializable
}

class ArticleListModel(var posts: ArrayList<ArticleModel>) : BaseModel() {

    class ArticleModel(
            var id: Int?,
            var type: String?,
            var slug: String?,
            var url: String?,
            var status: String?,
            var title: String?,
            var title_plain: String?,
            var content: String?,
            var excerpt: String?,
            var date: String?,
            var modified: String?,
            var categories: ArrayList<CategorieListModel.CategorieModel>?,
            var tags: ArrayList<CategorieListModel.CategorieModel>?,
            var author: UserModel?,
            var comments: ArrayList<CommentModel>?,
            var attachments: ArrayList<AttachmentModel>?,
            var comment_count: Int?,
            var comment_status: String?,
            var custom_fields: Custom_Article_Fields?,

            var _excerpt: String?,
            var _categories: ArrayList<CategorieListModel.CategorieModel>?,
            var _likeCount: String?
    ) : Serializable

    class Custom_Article_Fields(var views: ArrayList<String>,
                                var mp3_title: ArrayList<String>,
                                var mp3_author: ArrayList<String>,
                                var mp3_address: ArrayList<String>
    ): Serializable

}

class ArticleHolder(itemView: View) : BaseViewHolder<ArticleListModel.ArticleModel>(itemView) {

    override fun setContent(position: Int) {

        itemView.tv_title.text = data.title
        if (TextUtils.isEmpty(data._likeCount)) {
            data._excerpt = HtmlUtil.splitAndFilterString(data.content)
            data._excerpt = data._excerpt?.replace("Read more", "", true)
            val index = data._excerpt?.lastIndexOf("\n")
            data._likeCount = data._excerpt?.substring(index!! + 1)
            data._excerpt = data._excerpt?.substring(0, index!!)
            data._excerpt = data._excerpt?.trim()
        }

        itemView.tv_excerpt.text = data._excerpt

        if (data._categories == null) {
            data.categories?.sortBy { it.id }
            data._categories = data.categories
        }
        var tag = ""
        data._categories?.forEach { tag += it.title + " , " }
        if (TextUtils.isEmpty(tag))
            itemView.tv_tag.visibility = View.GONE
        else
            itemView.tv_tag.text = tag.substring(0, tag.length - 2)

        itemView.tv_name.text = data.author?.nickname
        itemView.tv_count.text = "${data.custom_fields?.views!![0]}次阅读"

        if (data.attachments != null && data.attachments!!.size > 0) {
            itemView.iv_img.show(data.attachments!![0].images.thumbnail.url)
        }

        itemView.layout.onClick {
            context.startActivity<ArticleDetailActivity>(ArgumentUtil.OBJ to data)
        }
    }
}