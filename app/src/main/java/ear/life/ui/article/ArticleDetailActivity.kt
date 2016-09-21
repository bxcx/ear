package ear.life.ui.article

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.http.SslError
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.hm.hmlibrary.ui.common.article.ArticleListModel.ArticleModel
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.HtmlUtil
import com.orhanobut.logger.Logger
import ear.life.R
import ear.life.http.BaseModel
import ear.life.http.CommentModel
import ear.life.ui.article.ArticleDetailActivity.CommentHolder
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.item_comment.view.*
import org.jetbrains.anko.onClick
import java.util.*

class ArticleDetailActivity : BaseListActivity<CommentModel, CommentHolder>() {

    var article: ArticleModel? = null
    var webView: WebView? = null

    override fun setUIParams() {
        layoutResID = R.layout.activity_article_detail
        itemResID = R.layout.item_comment
        canRefesh = false
        canLoadmore = false
    }

    override fun checkParams(): Boolean {
        if (extras.containsKey(ArgumentUtil.OBJ))
            article = intent.getSerializableExtra(ArgumentUtil.OBJ) as ArticleModel
        title = article?.title
        return article != null
    }

    override fun setHeaderView() {
        webView = WebView(this)
        webView!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val settings = webView!!.settings

        // User settings
        settings.javaScriptEnabled = true
        settings.loadsImagesAutomatically = true
        settings.loadWithOverviewMode = false
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView!!.isHorizontalScrollBarEnabled = false
        webView!!.addJavascriptInterface(this, "App")

        webView!!.loadUrl(article?.url)

        showLoadProgerss()

        webView!!.setWebChromeClient(MyWebChromeClient())
        webView!!.setWebViewClient(MyWebViewClient())
        fragment!!.recyclerView!!.addHeaderView(webView)
    }

    private inner class MyWebViewClient : WebViewClient(), MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        override fun onSeekComplete(p0: MediaPlayer?) {
            Logger.e("onSeekComplete")
        }

        override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
            Logger.e("onError")
            return false
        }

        override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {
        }

        override fun onCompletion(p0: MediaPlayer?) {
            Logger.e("onCompletion")
        }

        override fun onPrepared(p0: MediaPlayer?) {
            Logger.e("onPrepared")
        }

        override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
            Logger.e("onBufferingUpdate")
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                super.onReceivedError(view, request, error)
            Logger.e("onReceivedError :" + error.toString())
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
//                super.onReceivedHttpError(view, request, errorResponse)
            Logger.e("onReceivedError :" + errorResponse.toString())
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)
            Logger.e("onReceivedSslError :" + error.toString())
        }

        override fun onReceivedError(view: WebView, errorCode: Int,
                                     description: String, failingUrl: String) {
            // Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl)
            Logger.e("onReceivedError2 :" + description)
        }

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
                webView!!.loadUrl(js)
            }, 3000)

            //webView嵌套有时会出现大面积空白, 所以在加载完成后, 重新设置一下它的高度
            webView!!.loadUrl("javascript:App.resize(document.body.getBoundingClientRect().height);")

        }
    }

    private inner class MyWebChromeClient : WebChromeClient(), MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        override fun onSeekComplete(p0: MediaPlayer?) {
            Logger.e("onSeekComplete")
        }

        override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
            Logger.e("onError")
            return false
        }

        override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {
        }

        override fun onCompletion(p0: MediaPlayer?) {
            Logger.e("onCompletion")
        }

        override fun onPrepared(p0: MediaPlayer?) {
            Logger.e("onPrepared")
        }

        override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
            Logger.e("onBufferingUpdate")
        }

        override fun onProgressChanged(view: WebView, progress: Int) {
            Logger.e("$progress")
        }

        override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
            // Spit out lots of console messages here
            return true
        }
    }

    @JavascriptInterface
    fun resize(height: Float) {
        runOnUIThread({
            webView!!.layoutParams.width = resources.displayMetrics.widthPixels
            webView!!.layoutParams.height = (height * resources.displayMetrics.density).toInt()
        })
    }

    override fun loadData() {

        if (adapter.data.size == 0) {
            var comments = article!!.comments
            if (comments == null || comments.size == 0) {
                comments = arrayListOf(CommentModel(-1, "", "", "", "", "暂无评论", 0, null))
            }
            loadCompleted(comments)
        } else {
            loadCompleted(ArrayList())
        }

    }

    var b: Boolean = true
    override fun initUI() {
        super.initUI()
        iv_collect.onClick {
            val params = HashMap<String, Any>()
            params.put("json", "user/generate_auth_cookie")
            params.put("username", "admin")
            params.put("password", "asas4444")

            HMRequest.go<BaseModel>("https://ear.life", params) { }
        }


//        iv_share.onClick {
//            LogcatHelper.getInstance(this).start()

//        async() {
//            Logger.e("!!!")
//            val process = Runtime.getRuntime().exec("logcat ")
//            val bufferedReader = BufferedReader(
//                    InputStreamReader(process.inputStream))
//
//            val log = StringBuilder()
//            var line: String = ""
//
//            while (b || line.contains("url")) {
//                line = bufferedReader.readLine()
//                log.append(line)
//
//                runOnUiThread {
//                    tv_test.text = log.toString() + "\n"
//                }
//            }
//
//
//        }

        val a = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        a.requestAudioFocus({ Logger.e("$it") }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//        }
    }

    override fun getView(parent: ViewGroup?, position: Int): CommentHolder = CommentHolder(getItemView(parent))

    class CommentHolder(itemView: View) : BaseViewHolder<CommentModel>(itemView) {
        override fun setContent(position: Int) {
            itemView.tv_name.text = data.name
            itemView.tv_date.text = data.date
            itemView.tv_content.text = HtmlUtil.splitAndFilterString(data.content)
        }

    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }


}
