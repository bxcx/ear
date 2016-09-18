package ear.life.ui.article

import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hm.hmlibrary.ui.common.article.ArticleHolder
import com.hm.hmlibrary.ui.common.article.ArticleListModel
import com.hm.hmlibrary.ui.common.article.ArticleListModel.ArticleModel
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import com.hm.library.util.ArgumentUtil
import ear.life.R
import ear.life.http.CommentModel

class ArticleDetailActivity(override var itemResID: Int = R.layout.item_article) : BaseListActivity<ArticleModel, ArticleHolder>() {
    override fun getView(parent: ViewGroup?, position: Int): ArticleHolder = ArticleHolder(getItemView(parent))

    class CommentHolder(itemView: View) : BaseViewHolder<CommentModel>(itemView) {
        override fun setContent(position: Int) {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    var article: ArticleModel? = null
    lateinit var webView: WebView

    override fun checkParams(): Boolean {
        article = intent.getSerializableExtra(ArgumentUtil.OBJ) as ArticleModel
        title = article?.title
        return article != null
    }

    override fun setHeaderView() {
        webView = WebView(this)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val settings = webView.settings

        // User settings
        settings.javaScriptEnabled = true
        settings.loadsImagesAutomatically = true
        settings.loadWithOverviewMode = false
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.isHorizontalScrollBarEnabled = false
        webView.addJavascriptInterface(this, "App")

        webView.loadUrl(article?.url)

        showLoadProgerss()

        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelLoadProgerss()
                runDelayed({

                    //经过测试, 在android6.0+的系统中, 音乐没有自动播放,
                    //这里用js代码找到播放按钮, 调用其点击事件以播放
                    val js = "javascript:(" +
                            "function(){ " +
                            "var aEle=document.getElementsByTagName('div');" +
                            "for(var i=0;i<aEle.length;i++) { " +
                            "if(aEle[i].className=='wp-player-list'){" +
                            "aEle[i].focus(); " +
                            "var alist = aEle[i].getElementsByTagName('*');" +
                            "for(var j=0;j<3;j++) { " +
                            "alist[j].focus(); alist[j].click();" +
                            "}" +
                            "break;" +
                            "}" +
                            "}" +
                            "}()) "
                    webView.loadUrl(js)
                }, 3000)

                //webView嵌套有时会出现大面积空白, 所以在加载完成后, 重新设置一下它的高度
                webView.loadUrl("javascript:App.resize(document.body.getBoundingClientRect().height);")

            }
        })
        fragment!!.recyclerView!!.addHeaderView(webView)
    }

    @JavascriptInterface
    fun resize(height: Float) {
        runOnUIThread({
            webView.layoutParams.width = resources.displayMetrics.widthPixels
            webView.layoutParams.height = (height * resources.displayMetrics.density).toInt()
        })
    }

    override fun loadData() {
        val params = fragment!!.listParams
        params.put("json", "get_posts")

        HMRequest.go<ArticleListModel>(params = params, needCallBack = true) {
            //仅需要调用这一个方法完成上下拉功能
            loadCompleted(it?.posts)
        }
    }

}
