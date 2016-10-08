package com.hm.hmlibrary.ui.article

import android.text.TextUtils
import android.view.View
import com.hm.library.base.BaseViewHolder
import com.hm.library.expansion.show
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.HtmlUtil
import com.hm.library.util.ImageUtil
import com.orhanobut.logger.Logger
import ear.life.R
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
    ) : Serializable
}

class ArticleModel(var post: Article) : BaseModel()

class ArticleListModel(var posts: ArrayList<Article>) : BaseModel() {


    class Custom_Article_Fields(var views: ArrayList<String>,
                                var mp3_title: ArrayList<String>,
                                var mp3_author: ArrayList<String>,
                                var mp3_address: ArrayList<String>
    ) : Serializable {
        val valid: Boolean
            get() {
                if (mp3_address == null || mp3_author == null || mp3_title == null)
                    return false
                if (mp3_address.size != mp3_author.size || mp3_address.size != mp3_title.size)
                    return false
                mp3_address.forEach {
                    if (!(it.startsWith("http://") && it.endsWith(".mp3"))) {
                        return false
                    }
                }

                return true
            }
    }

}

class Article(
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
        var custom_fields: ArticleListModel.Custom_Article_Fields?,

        var _categories: ArrayList<CategorieListModel.CategorieModel>?,
        var _likeCount: String?
) : Serializable {

    var _share: String = ""
        get() {
            try {
                var str = "耳朵纯音乐 | "
                val array = _content.split("\n")
                for (i in 0..2) {
                    str += array[i] + "\n"
                }
                return str
            } catch(e: Exception) {
                Logger.e(e.message)
            }
            return title!!
        }

    private var t_content: String = ""
    var _content: String = ""
        get() {
            if (TextUtils.isEmpty(t_content)) {
                try {
                    t_content = HtmlUtil.splitAndFilterString(content)
                    t_content = t_content.replace("Read more", "", true)

                    val index = t_content.lastIndexOf("\n")
                    _likeCount = t_content.substring(index + 1)
                    t_content = t_content.substring(0, index)
                    t_content = t_content.trim()
                } catch(e: Exception) {
                }
            }
            return t_content
        }

    private var t_excerpt: String = ""
    var _excerpt: String = ""
        get() {
            if (!TextUtils.isEmpty(excerpt) && TextUtils.isEmpty(t_excerpt)) {
                try {
                    t_excerpt = excerpt!!.substring(3, excerpt!!.indexOf("</p>"))
//                t_excerpt = t_excerpt.replace("Read more", "", true)
//
//                val index = t_excerpt.lastIndexOf("\n")
//                _likeCount = t_excerpt.substring(index + 1)
//                t_excerpt = t_excerpt.substring(0, index)
                    t_excerpt = t_excerpt.trim()
                } catch(e: Exception) {
                }
            }
            return t_excerpt
        }

    var _url: String? = ""
        get() = url?.replace("https://", "http://")
}

class ArticleHolder(itemView: View) : BaseViewHolder<Article>(itemView) {

    override fun setContent(position: Int) {

        itemView.tv_title.text = data.title
        itemView.tv_excerpt.text = data._content
        itemView.iv_head.show(data.author?.avatar, ImageUtil.CircleDisplayImageOptions(R.drawable.ic_launcher))

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
//            context.startActivity<ArticleDetailActivity>(ArgumentUtil.OBJ to data)
            context.startActivity<ArticleDetailActivity>(ArgumentUtil.TITLE to data.title!!, ArgumentUtil.ID to data.id!!)
        }
    }
}