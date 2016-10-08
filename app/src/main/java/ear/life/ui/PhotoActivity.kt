package ear.life.ui

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bm.library.PhotoView
import com.hm.library.base.BaseActivity
import com.hm.library.expansion.show
import com.hm.library.http.HMRequest
import ear.life.R
import kotlinx.android.synthetic.main.activity_photo.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.onClick
import java.util.*


class PhotoActivity : BaseActivity() {

    companion object {
        val PhotoArray = "PhotoArray"
        val SavePath = "SavePath"
        val Index = "Index"
    }

    var photoArray: ArrayList<String> = ArrayList()
    var savePath: String = ""
    var index: Int = 0

    override fun setUIParams() {
        layoutResID = R.layout.activity_photo
        hideActionBar = true
        swipeBack = false
    }

    override fun checkParams(): Boolean {
        if (intent.hasExtra(PhotoArray))
            photoArray = extras.getStringArrayList(PhotoArray)
        if (intent.hasExtra(SavePath))
            savePath = extras.getString(SavePath)
        index = extras.getInt(Index, 0)

        return photoArray.size != 0
    }

    override fun initUI() {
        super.initUI()

        iv_photo_back.onClick { finish() }
        if (TextUtils.isEmpty(savePath)) {
            iv_photo_down.visibility = View.GONE
        } else {
            iv_photo_down.visibility = View.VISIBLE
            iv_photo_down.onClick {
                val url = photoArray[pager_photo.currentItem]
                val fileName = url.substring(url.lastIndexOf("/") + 1)
                HMRequest.download(url, savePath, fileName, activity = this) { progress, file ->
                    if (file != null) {
                        try {
                            // 其次把文件插入到系统图库
                            MediaStore.Images.Media.insertImage(ctx.contentResolver,
                                    file.absolutePath, fileName, null)
                            // 最后通知图库更新
                            ctx.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        if (photoArray.size == 1) {
            tv_photo_count.visibility = View.GONE
        } else {
            tv_photo_count.visibility = View.VISIBLE
            tv_photo_count.text = "${index + 1}/${photoArray.size}"
        }

        pager_photo.pageMargin = (resources.displayMetrics.density * 15).toInt()
        pager_photo.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (photoArray.size > 1)
                    tv_photo_count.text = "${position + 1}/${photoArray.size}"
            }

        })
        pager_photo.adapter =
                object : PagerAdapter() {
                    override fun getCount(): Int {
                        return photoArray.size
                    }

                    override fun isViewFromObject(view: View, `object`: Any): Boolean {
                        return view === `object`
                    }

                    override fun instantiateItem(container: ViewGroup, position: Int): Any {
                        val view = PhotoView(ctx)
                        view.enable()
                        view.scaleType = ImageView.ScaleType.FIT_CENTER
                        view.show(photoArray[position])
                        view.onClick { finish() }
                        container.addView(view)
                        return view
                    }

                    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                        container.removeView(`object` as View)
                    }
                }
        pager_photo.currentItem = index
    }

}
