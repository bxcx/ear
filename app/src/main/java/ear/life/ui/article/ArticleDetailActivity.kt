package ear.life.ui.article

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.view.*
import android.webkit.*
import android.widget.EditText
import com.hm.hmlibrary.ui.article.Article
import com.hm.hmlibrary.ui.article.ArticleModel
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.expansion.show
import com.hm.library.http.HMRequest
import com.hm.library.resource.MediaScanner
import com.hm.library.resource.view.TipsToast
import com.hm.library.umeng.share.IShareCallback
import com.hm.library.umeng.share.ShareUtils
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.HtmlUtil
import com.hm.library.util.ImageUtil
import com.rey.material.widget.Button
import com.umeng.socialize.media.UMImage
import ear.life.R
import ear.life.app.App
import ear.life.extension.hideSoftInput
import ear.life.extension.showSoftInput
import ear.life.http.BaseModel
import ear.life.http.CommentModel
import ear.life.http.FavoriteModel
import ear.life.ui.article.ArticleDetailActivity.CommentHolder
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.item_comment.view.*
import org.jetbrains.anko.act
import org.jetbrains.anko.ctx
import org.jetbrains.anko.onClick

class ArticleDetailActivity : BaseListActivity<CommentModel, CommentHolder>() {

    var article: Article? = null
    lateinit var webView: WebView

    var su: ShareUtils? = null

    companion object {
        var needRefeshMusicLise = false
    }

    override fun setUIParams() {
        layoutResID = R.layout.activity_article_detail
        itemResID = R.layout.item_comment
        menuResID = R.menu.menu_article_detail
        canRefesh = true
        canLoadmore = false
    }

    override fun checkParams(): Boolean {
        if (extras.containsKey(ArgumentUtil.OBJ))
            article = intent.getSerializableExtra(ArgumentUtil.OBJ) as Article
        article = intent.getSerializableExtra(ArgumentUtil.OBJ) as Article
        title = article?.title
        if (article == null) {
            finish()
        }
        return article != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_download) {
            if (article == null)
                return false

            if (article!!.custom_fields == null || !article!!.custom_fields!!.valid) {
                showTips(TipsToast.TipType.Error, "这首音乐暂未提供下载")
                return false
            }

            val mp3 = article?.custom_fields ?: return false
            if (mp3.mp3_address.size > 0) {
                showLoading("下载中")
                mp3.mp3_address.forEachIndexed { index, url ->
                    HMRequest.download(url, App.LightMusicPath, mp3.mp3_title[index] + ".mp3", false, true, this) { progress, file ->
                        if (file != null) {
                            needRefeshMusicLise = true
                            MediaScanner.scanFile(ctx, arrayOf(App.LightMusicPath), null) { path, uri ->
                                runOnUiThread {
                                    showTips(TipsToast.TipType.Success, "下载成功,请到纯音中试听")
                                }
                            }
                        }
                    }

                }
            }

            //
//        OkHttpUtils.get().addHeader("User-Agent", "test").url("http://itingw.b0.upaiyun.com/乌拉尔的花楸树.mp3").build()
//                .execute(object : FileCallBack(App.NatureSoundPath, "test.mp3") //
//                {
//                    override fun onResponse(response: File?) {
//                        Logger.i("下载成功,保存在" + response?.absolutePath)
//                        toast("下载成功,保存在" + response?.absolutePath)
//                    }
//
//                    override fun onError(call: Call?, e: Exception?) {
//                        toast(e?.message.toString())
//                    }
//
//                    override fun inProgress(progress: Float) {
//                        com.orhanobut.logger.Logger.e("$progress")
//                    }
//
//                })
        }

        return super.onOptionsItemSelected(item)
    }

    var displayUI: Boolean = false
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

        webView.loadUrl(article?._url)

        showLoadProgerss()
        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!displayUI && newProgress > 80) {
                    displayUI = true
                    runDelayed({
                        //webView嵌套有时会出现大面积空白, 所以在加载完成后, 重新设置一下它的高度
                        webView.loadUrl("javascript:App.resize(document.body.getBoundingClientRect().height);")
                    }, 3000)
                }
            }
        })
        webView.setWebViewClient(object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
////                view.loadUrl(url)
//                return false
//            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (!url.equals(article?._url)) {
//                    view?.goBack()
                    val uri = Uri.parse(url)
                    val iten = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(iten)
                    finish(500)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //延迟至loadData
                //cancelLoadProgerss()
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
            }
        })
        fragment!!.recyclerView!!.addHeaderView(webView)
    }

    var hasResize = false
    @JavascriptInterface
    fun resize(height: Float) {
        runOnUIThread({
            webView.layoutParams.width = resources.displayMetrics.widthPixels
            webView.layoutParams.height = (height * resources.displayMetrics.density).toInt()
            hasResize = true
            fragment!!.loadRefresh()

        })
    }

    override fun loadData() {
        //在webView高度不确定前不设置评论数据
        if (!hasResize) {
            loadCompleted(arrayListOf(CommentModel(-1, "", "", "", "", "暂无评论", 0, null)))
            return
        }
        var params = App.createParams
        params.put("json", "get_post")
        params.put("id", article?.id!!)
        HMRequest.go<ArticleModel>(params = params) {
            var comments = it?.post?.comments

            if (comments?.size == 0) {
                tv_comment_count.visibility = View.GONE
            } else {
                tv_comment_count.visibility = View.VISIBLE
                tv_comment_count.text = "${comments?.size}"
            }

            comments?.sortByDescending { it.id }
            if (comments == null || comments.size == 0) {
                comments = arrayListOf(CommentModel(-1, "", "", "", "", "暂无评论", 0, null))
            }
            loadCompleted(comments)

            cancelLoadProgerss()
            if (gotoComment) {
                gotoComment = false
                fragment!!.recyclerView!!.recyclerView.scrollBy(0, webView.layoutParams.height)
            }
        }
    }

    var gotoComment: Boolean = false
    override fun initUI() {
        super.initUI()

        tv_comment.onClick {
            if (!App.checkCookie(this)) {
                return@onClick
            }
            showComment("", "请输入评论内容")
        }

        iv_comment.onClick {
            if (article == null)
                return@onClick
            fragment!!.recyclerView!!.recyclerView.scrollBy(0, webView.layoutParams.height)
        }

        iv_collect.onClick {
            if (article == null)
                return@onClick
            if (!App.checkCookie(this)) {
                return@onClick
            }

            val params = App.createParams
            params.put("json", "user/post_favorite")
            params.put("post_id", article!!.id!!)
            params.put("doAction", true)
            showLoading()

            HMRequest.go<FavoriteModel>(params = params, activity = this) {
                cancelLoading()
                iv_collect.setImageResource(if (it!!.after) R.drawable.icon_collected else R.drawable.icon_collect)
            }
        }

        iv_share.onClick {
            if (article == null)
                return@onClick
            val image: UMImage
            if (article!!.attachments != null && article!!.attachments!!.size > 0) {
                image = UMImage(ctx, article!!.attachments!![0].images.thumbnail.url)
            } else {
                image = UMImage(ctx, R.drawable.ic_launcher)
            }

            su = ShareUtils(act)
            su?.share(article!!.title, article!!._url, article!!._excerpt, image, object : IShareCallback {
                override fun onSuccess() {
                    showTips(TipsToast.TipType.Smile, "分享成功")
                }

                override fun onFaild() {
                }

                override fun onCancel() {
                }
            })
        }

    }

    fun showComment(replayId: String, hint: String) {
        val dialog = DialogComment(ctx, "提交", hint)
        dialog.setCommentClicker(View.OnClickListener {
            val content = dialog.ed_comment.text.toString().trim()
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
        val params = App.createParams
        params.put("json", "user/post_comment")
        params.put("post_id", article!!.id!!)
        params.put("content", content)
        params.put("comment_status", 1)
        showLoading()

        HMRequest.go<BaseModel>(params = params, activity = this) {
            cancelLoading()
            showToast("发表成功")
            gotoComment = true
            fragment!!.loadRefresh()
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): CommentHolder = CommentHolder(getItemView(parent))

    class CommentHolder(itemView: View) : BaseViewHolder<CommentModel>(itemView) {
        override fun setContent(position: Int) {
            itemView.tv_name.text = data.author?.nickname
            itemView.tv_date.text = data.date
            itemView.tv_content.text = HtmlUtil.splitAndFilterString(data.content)
            itemView.iv_head.show(data.author?.avatar, ImageUtil.CircleDisplayImageOptions(R.drawable.ic_launcher))

        }

    }

    override fun onResume() {
        super.onResume()
        webView.onResume()

        //先查查当前是否已经收藏过
        if (App.cookie != null && article != null) {
            val params = App.createParams
            params.put("json", "user/post_favorite")
            params.put("post_id", article!!.id!!)

            HMRequest.go<FavoriteModel>(params = params) {
                iv_collect.setImageResource(if (it!!.before) R.drawable.icon_collected else R.drawable.icon_collect)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    class DialogComment(internal var context: Context, btText: String, hint: String) {


        val dialog: Dialog
        var ed_comment: EditText
        var btn_comment: Button

        init {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            // 获取Dialog布局
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)
            ed_comment = view.findViewById(R.id.ed_comment) as EditText
            btn_comment = view.findViewById(R.id.btn_comment) as Button
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
            ed_comment.showSoftInput()
        }

        fun cancel() {
            ed_comment.hideSoftInput()
            dialog.cancel()
        }

        fun setCommentClicker(l: View.OnClickListener) {
            btn_comment.setOnClickListener(l)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        su?.onActivityResult(requestCode, resultCode, data)
    }
}
