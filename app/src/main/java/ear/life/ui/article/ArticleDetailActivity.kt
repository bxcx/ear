package ear.life.ui.article

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.*
import android.webkit.*
import android.widget.EditText
import com.gc.materialdesign.views.ButtonFlat
import com.hm.hmlibrary.ui.common.article.ArticleListModel.ArticleModel
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import com.hm.library.resource.view.TipsToast.TipType
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.HtmlUtil
import ear.life.R
import ear.life.app.App
import ear.life.http.BaseModel
import ear.life.http.CommentModel
import ear.life.ui.LoginActivity
import ear.life.ui.article.ArticleDetailActivity.CommentHolder
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.item_comment.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity
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
        if (article == null) {
            finish()
        }
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

        webView!!.setWebChromeClient(object : WebChromeClient() {

        })

        webView!!.setWebViewClient(object : WebViewClient() {
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
        })
        fragment!!.recyclerView!!.addHeaderView(webView)
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
            if (App.cookie == null) {
                showTips(TipType.Warning, "请先登录")
                startActivity<LoginActivity>()
                return@onClick
            }

            showComment("", "请输入评论内容")
        }

    }

    fun showComment(replayId: String, hint: String) {
        val dialog = DialogComment(ctx, "提交", hint)
        dialog.setCommentClicker(View.OnClickListener {
            val content = dialog.ed_comment.getText().toString().trim()
            if (TextUtils.isEmpty(content)) {
                showTips(R.drawable.tips_warning, "请输入评论")
                return@OnClickListener
            }
            dialog.cancel()
            doComment(content, -1)
        })

        dialog.show()
    }

    fun doComment(content: String, replayId: Int) {
        val params = App.createNameAndEmailParams
        params.put("json", "respond/submit_comment")
        params.put("post_id", article!!.id!!)
        params.put("content", content)

        HMRequest.go<BaseModel>("https://ear.life", params) { }
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

    class DialogComment(internal var context: Context, btText: String, hint: String) {


        private val dialog: Dialog
        lateinit var ed_comment: EditText
        lateinit var btn_comment: ButtonFlat

        init {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            // 获取Dialog布局
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)
            ed_comment = view.findViewById(R.id.ed_comment) as EditText
            btn_comment = view.findViewById(R.id.btn_comment) as ButtonFlat
            ed_comment.hint = hint
            btn_comment.text = btText
            // 设置Dialog最小宽度为屏幕宽度
            view.minimumWidth = display.width

            // 定义Dialog布局和参数
            dialog = Dialog(context, R.style.ActionSheetDialogStyle)
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)

            val win = dialog.window
            win.decorView.setPadding(0, 0, 0, 0)
            win.setGravity(Gravity.BOTTOM)
            val lp = win.attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            win.attributes = lp

        }

        fun show() {
            dialog.show()
//            btn_comment.postDelayed(Runnable { ViewTool.showSoftInput(ed_comment) }, 100)
        }

        fun cancel() {
            dialog.cancel()
        }

        fun setCommentClicker(l: View.OnClickListener) {
            btn_comment.setOnClickListener(l)
        }


    }

}
