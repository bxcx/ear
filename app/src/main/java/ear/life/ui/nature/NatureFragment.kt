package ear.life.ui.nature


import android.media.MediaPlayer
import android.view.View
import android.view.ViewGroup
import com.hm.library.base.BaseListFragment
import com.hm.library.http.HMRequest
import com.hm.library.resource.circularseekbar.CircularSeekBar
import com.hm.library.resource.view.TipsToast.TipType.Smile
import com.hm.library.resource.view.TipsToast.TipType.Success
import ear.life.R
import ear.life.http.HttpServerPath
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
        canRefesh = false
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

        layout_icon_save.onClick {
            showTips(Smile, "功能开发中")
        }

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
        HMRequest.go<NatureListModel>(HttpServerPath.Server_Nature, needCallBack = true) {
            loadCompleted(it?.list)
        }
    }

    override fun getView(parent: ViewGroup?, position: Int): NautreHolder = NautreHolder(getItemView(parent))

}
