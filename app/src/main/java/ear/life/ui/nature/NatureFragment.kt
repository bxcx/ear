package ear.life.ui.nature


import android.media.MediaPlayer
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.hm.library.base.BaseListFragment
import com.hm.library.resource.circularseekbar.CircularSeekBar
import com.hm.library.resource.view.TipsToast.TipType.Smile
import com.hm.library.resource.view.TipsToast.TipType.Success
import ear.life.R
import kotlinx.android.synthetic.main.fragment_nature_list.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onTouch
import java.text.SimpleDateFormat
import java.util.*

object NatureManager {
    val PlayList: HashMap<String, MediaPlayer> = HashMap<String, MediaPlayer>()
    val VolumeList: HashMap<String, Int> = HashMap<String, Int>()

    fun stop(key: String) {
        if (PlayList.containsKey(key)) {
            val value = PlayList[key]
            value?.stop()
            value?.reset()

            PlayList.remove(key)
            VolumeList.remove(key)
        }
    }

    fun pause(key: String) {
        if (PlayList.containsKey(key)) {
            val value = PlayList[key]
            value?.pause()
        }
    }

    fun start(key: String) {
        if (PlayList.containsKey(key)) {
            val value = PlayList[key]
            value?.start()
        }
    }

    fun stopAll() {
        PlayList.keys.toList().forEach { stop(it) }
    }

    fun pause() {
        PlayList.keys.forEach { pause(it) }
    }

    fun start() {
        PlayList.keys.forEach { start(it) }
    }

    fun isPlaying(): Boolean {
        return PlayList.values.count() > 0 && PlayList.values.first().isPlaying
    }
}

class NatureFragment : BaseListFragment<NatureListModel.NautreModel, NautreHolder>() {

    var time: Int = 0
    var timer = Timer()

    var showClock: Boolean = false
    var startClock: Boolean = false

    override fun setUIParams() {
        layoutResID = R.layout.fragment_nature_list
        itemResID = R.layout.item_nature
        canRefesh = true
        canLoadmore = false
    }

    override fun onResume() {
        super.onResume()
        if (time <= 0) {
            stopUI()
        }
    }

    override fun initUI() {
        super.initUI()

        layout_icon_play.onClick {
            if (NatureManager.isPlaying()) {
                iv_play.setImageResource(R.drawable.icon_pause)
                NatureManager.pause()
            } else {
                iv_play.setImageResource(R.drawable.icon_play)
                NatureManager.start()
            }

            adapter?.notifyDataSetChanged()
        }

        val sdf = SimpleDateFormat("HH:mm")
        layout_clock.onTouch { view, motionEvent -> true }
        layout_icon_clock.onClick {
            if (startClock) {
                if (layout_clock.visibility == View.GONE)
                    layout_clock.visibility = View.VISIBLE
                else
                    layout_clock.visibility = View.GONE
            } else if (!showClock) {
                showClock = true
                iv_clock.setImageResource(R.drawable.icon_clock_black)
                layout_clock.visibility = View.VISIBLE
                val progress = 20
                bar_clock.progress = progress
                val afterDate = Date(Date().time + 1000 * 60 * progress)
                val str = sdf.format(afterDate)
                tv_time.text = "至 $str"
            } else {
                showClock = false
                iv_clock.setImageResource(R.drawable.icon_clock)
                layout_clock.visibility = View.GONE
            }
        }
        bar_clock.setOnSeekBarChangeListener(object : CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar?, progress: Int, fromUser: Boolean) {
                if (startClock)
                    return

                time = progress
                tv_count.text = "$progress 分钟"
                val afterDate = Date(Date().time + 1000 * 60 * progress)
                val str = sdf.format(afterDate)
                tv_time.text = "至 $str"
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }

        })


        btn_start.onClick {
            if (startClock) {
                stopTimer("已停止")
            } else {
                time = bar_clock.progress
                if (time > 0) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            runOnUIThread({
                                try {
                                    time--
                                    if (time <= 0) {
                                        stopTimer()
                                    } else {
                                        bar_clock.progress = time
                                        tv_count.text = "还剩下 $time 分钟"
                                    }
                                } catch(e: Exception) {
                                }
                            })
                        }
                    }, 60 * 1000, 60 * 1000)

                    startClock = true

                    showTips(Success, "设定成功")
                    bar_clock.isTouchEnabled = false

                    showClock = false
                    iv_clock.setImageResource(R.drawable.icon_clock_red)
                    layout_clock.visibility = View.GONE
                }
            }
        }
    }

    fun stopTimer(msg: String = "时间到") {
        try {
            timer.cancel()
            timer.purge()
        } catch(e: Exception) {
        }

        startClock = false
        NatureManager.stopAll()
        showTips(Smile, msg)

        stopUI()
    }

    fun stopUI() {
        layout_clock.visibility = View.GONE
        tv_count.text = "已停止"
        iv_clock.setImageResource(R.drawable.icon_clock)
        adapter?.notifyDataSetChanged()
        bar_clock.isTouchEnabled = true
    }


    override fun loadData() {
        val json = "{'status':'ok','msg':null,'list':[{'id':'1','sort_index':'1','name':'\u96e8','icon':'rain_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/b19336964062bd29803c413486f8d68b.m4a'},{'id':'3','sort_index':'2','name':'\u96f7\u9635\u96e8','icon':'thunderstorm_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/fe35a13940712c9080dc516d0ecb47e6.m4a'},{'id':'5','sort_index':'3','name':'\u98ce','icon':'wind_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/4e82de674022ba0e80ea5a60b7fce1eb.m4a'},{'id':'4','sort_index':'4','name':'\u7bdd\u706b','icon':'fire_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/bf719ad24022c01880aed5854c65ef7e.m4a'},{'id':'6','sort_index':'5','name':'\u6d77\u6d6a','icon':'water_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/4558cd53400909c180f7fc911189480d.m4a'},{'id':'7','sort_index':'6','name':'\u6cb3\u6d41','icon':'river_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/990521114098e5138022153df2d817fe.m4a'},{'id':'14','sort_index':'7','name':'\u98ce\u94c3','icon':'chimes_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/e5dfef0f40c68acb805d947430a69963.m4a'},{'id':'15','sort_index':'8','name':'\u84dd\u9cb8','icon':'whale_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/fda3b94d40f2f90f80be9e37254a8836.m4a'},{'id':'8','sort_index':'9','name':'\u590f\u5929\u7684\u591c\u665a','icon':'night_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/2f47cbeb402c47a3808fa7df411b8710.m4a'},{'id':'9','sort_index':'10','name':'\u6797\u95f4\u7684\u6e05\u6668','icon':'forest_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/e3f5d4db4008295180d58e97be8d1629.m4a'},{'id':'2','sort_index':'11','name':'\u5496\u5561\u5385','icon':'cafe_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/9d4fb81f407a30d68027f64d3bf9df04.m4a'},{'id':'13','sort_index':'12','name':'\u519c\u573a','icon':'cow_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/f9f2515f408eddc88009710f7c5427f9.m4a'},{'id':'10','sort_index':'13','name':'\u592a\u7a7a','icon':'space_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/b72e05f8407c8a54803d02d0427341dc.m4a'},{'id':'11','sort_index':'14','name':'\u626c\u5e06','icon':'steering_wheel_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/d7eb4698408cd2e980be1ad10f44adb1.m4a'},{'id':'12','sort_index':'15','name':'\u8fdc\u65b9','icon':'rails_icon','src':'http://bmob-cdn-5137.b0.upaiyun.com/2016/07/30/665030d64094bf94809a999e7cac0446.m4a'}]}"
        val model: NatureListModel = Gson().fromJson(json, NatureListModel::class.java)
        loadCompleted(model.list)

//        HMRequest.go<NatureListModel>(HttpServerPath.Server_Nature, needCallBack = true) {
//            loadCompleted(it?.list)
//        }
    }

    override fun getView(parent: ViewGroup?, position: Int): NautreHolder = NautreHolder(getItemView(parent))

}
