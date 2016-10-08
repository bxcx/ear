package ear.life.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hm.hmlibrary.ui.article.CategorieListModel
import com.hm.library.app.Cacher
import com.hm.library.base.BaseMainActivity
import com.hm.library.http.HMRequest
import com.hm.library.resource.draggridview.DragGridActivity
import com.hm.library.resource.view.TipsToast
import ear.life.R
import ear.life.app.App
import ear.life.app.DeviceInfoTool
import ear.life.http.HttpFirVersion
import ear.life.ui.article.ArticleFragment
import ear.life.ui.mine.MineFragment
import ear.life.ui.music.LinghtMusicFragment
import ear.life.ui.nature.NatureFragment
import ear.life.ui.view.AlertDialog
import im.fir.sdk.FIR
import im.fir.sdk.VersionCheckCallback
import kotlinx.android.synthetic.main.activity_main_tab.*
import org.jetbrains.anko.ctx

class MainTabActivity(override var layoutResID: Int = R.layout.activity_main_tab) : BaseMainActivity() {

    companion object {
        lateinit var instance: MainTabActivity
    }

    //妙笔
    lateinit var articleFragment: ArticleFragment
    //纯音
    lateinit var lightMusicFragment: LinghtMusicFragment
    //自然
    lateinit var natureFragment: NatureFragment
    //爱听
    lateinit var mineFragment: MineFragment


    override fun setUIParams() {
        App.ContentResolver = contentResolver
        articleFragment = ArticleFragment()
        lightMusicFragment = LinghtMusicFragment()
        natureFragment = NatureFragment()
        mineFragment = MineFragment()

        mTabs.add(articleFragment)
        mTabs.add(lightMusicFragment)
        mTabs.add(natureFragment)
        mTabs.add(mineFragment)
    }

    override fun initComplete() {
        instance = this

        //三秒后执行
        runDelayed({
            //模拟红点
            main_tabpage.setIndicateDisplay(3, true)
        }, 3000)

        //是否可以滑动，滑动是否有渐变效果
        main_tabpage.setStyle(false, false)

        val params = App.createParams
        params.put("json", "get_category_index")

        HMRequest.go<CategorieListModel>(params = params) {
            Cacher[ArticleFragment.ChannelAll] = it?.categories
        }

        FIR.checkForUpdateInFIR(App.api_token, object : VersionCheckCallback() {

            override fun onSuccess(versionJson: String) {
                val version: HttpFirVersion = Gson().fromJson(versionJson, object : TypeToken<HttpFirVersion>() {}.type)
                if (DeviceInfoTool.getVersion(ctx) < version.version)
                    AlertDialog(ctx).builder().setCancelable(true).setTitle("发现新版本：" + version.versionShort)
                            .setMsg(version.changelog).setNegativeButton("下次再说") {}.setPositiveButton("立即更新") {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.installUrl))
                            startActivity(intent)
                        } catch(e: Exception) {
                            showTips(TipsToast.TipType.Error, "下载失败,请自行到官网下载")
                        }
                    }.show()

            }

        })

//        val request = Request.Builder().url("https://api.github.com/repos/square/okhttp/issues").header("User-Agent", "OkHttp Headers.java").addHeader("Accept", "application/json; q=0.5").addHeader("Accept", "application/vnd.github.v3+json").build()
//
//        val response = OkHttpClient().newCall(request).execute()
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

    override fun onTabSelected(index: Int) {
        if (index == 3) {
            main_tabpage.setIndicateDisplay(3, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                DragGridActivity.Selection -> {
                    articleFragment.onActivityResult(requestCode, resultCode, data)
                }
                LinghtMusicFragment.Action_Import -> lightMusicFragment.onActivityResult(requestCode, resultCode, data)
                else -> mineFragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}