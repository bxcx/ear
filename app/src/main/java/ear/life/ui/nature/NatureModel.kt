package ear.life.ui.nature

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.view.View
import com.hm.library.base.BaseViewHolder
import com.hm.library.http.HMRequest
import com.hm.library.resource.discreteseekbar.DiscreteSeekBar
import com.hm.library.resource.view.ActionSheetDialog
import com.hm.library.resource.view.CustomToast
import ear.life.R
import ear.life.app.App
import ear.life.http.BaseModel
import kotlinx.android.synthetic.main.item_nature.view.*
import org.jetbrains.anko.*
import java.io.File
import java.util.*

class NatureListModel(var list: ArrayList<NautreModel>? = null) : BaseModel() {

    class NautreModel(var id: String?, var name: String?, var icon: String?, var src: String?, var isDownloading: Boolean)

}

class NautreHolder(itemView: View) : BaseViewHolder<NatureListModel.NautreModel>(itemView) {

    override fun setContent(position: Int) {
        if (data.isDownloading) {
            startDownload()
            return
        }


        val path = App.NatureSoundPath
        val fileName = data.name + ".m4a"
        val file = File("$path/$fileName")

        itemView.setOnLongClickListener {
            if (file.exists()) {
                ActionSheetDialog(context).builder().setTitle("确认删除本地文件吗?")
                        .addSheetItem("删除", Color.RED, {
                            if (file.delete()) {
                                CustomToast.makeText(context, "删除成功", 0).show()
                            } else {
                                CustomToast.makeText(context, "删除失败", 0).show()
                            }
                            setContent(position)
                        })
                        .show()
            }
            true
        }

        var name: String = ""
        var disable: String = ""
        if (file.exists()) {
            itemView.v_cover.visibility = View.GONE
            itemView.tv_name.textColor = 0xff333333.toInt()
            itemView.bar_vol.setScrubberColor(0xff009b8f.toInt())
        } else {
            itemView.v_cover.visibility = View.VISIBLE
            itemView.v_cover.onTouch { view, motionEvent -> true }
            name = "/需要下载"
            disable = "_disabled"
            itemView.tv_name.textColor = 0x96353535.toInt()
            itemView.bar_vol.setScrubberColor(Color.TRANSPARENT)
        }

        var mMediaPlayer: MediaPlayer? = null
        if (NatureManager.PlayList.containsKey("$path/$fileName")) {
            mMediaPlayer = NatureManager.PlayList["$path/$fileName"]
        }

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying)
                itemView.iv_playing.visibility = View.VISIBLE
            else
                itemView.iv_playing.visibility = View.VISIBLE
            itemView.iv_playing.setImageResource(if (mMediaPlayer.isPlaying) R.drawable.icon_sound else R.drawable.icon_pause_simple)
        } else {
            itemView.iv_playing.visibility = View.GONE
        }

        itemView.tv_name.text = data.name + name
        itemView.iv_img.imageResource = context.resources.getIdentifier(data.icon!! + disable, "drawable", context.packageName)
        itemView.iv_img.onClick {

            var mMediaPlayer: MediaPlayer? = null
            if (NatureManager.PlayList.containsKey("$path/$fileName")) {
                mMediaPlayer = NatureManager.PlayList["$path/$fileName"]
            }

            if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
                mMediaPlayer?.stop()
                mMediaPlayer?.reset()
                NatureManager.PlayList.remove("$path/$fileName")
                NatureManager.VolumeList.remove("$path/$fileName")
                itemView.iv_playing.visibility = View.GONE
            } else {
                if (file.exists()) {
                    itemView.iv_playing.visibility = View.VISIBLE
                    itemView.iv_playing.setImageResource(R.drawable.icon_sound)
                    itemView.iv_playing.tag = "$path/$fileName"
                    playMusic("$path/$fileName")
                } else {
                    data.isDownloading = true
                    startDownload()
                    HMRequest.download(data.src!!, path, fileName, false, false, context as Activity, { progress, file ->
                        if (progress!! < 1) {
                            itemView.bar_vol.progress = (progress * 100).toInt()
                        } else {
                            context.toast("下载成功，请点击播放")
                            data.isDownloading = false
                            itemView.bar_vol.progress = 50
                            setContent(position)
                        }
                    })
                }
            }
        }

        if (NatureManager.VolumeList.containsKey("$path/$fileName")) {
            itemView.bar_vol.progress = NatureManager.VolumeList["$path/$fileName"]!!
        } else {
            itemView.bar_vol.progress = 50
        }
        itemView.bar_vol.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                val vol = value / 100f
                var mMediaPlayer: MediaPlayer? = null
                if (NatureManager.PlayList.containsKey("$path/$fileName")) {
                    mMediaPlayer = NatureManager.PlayList["$path/$fileName"]
                }
                mMediaPlayer?.setVolume(vol, vol)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                NatureManager.VolumeList.put("$path/$fileName", itemView.bar_vol.progress)
            }

        })
    }

    fun startDownload() {
        itemView.tv_name.text = data.name + "/下载中..."
    }

    fun playMusic(fileName: String) {
        var mMediaPlayer: MediaPlayer? = null
        if (NatureManager.PlayList.containsKey(fileName)) {
            mMediaPlayer = NatureManager.PlayList[fileName]
        }
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        }
        mMediaPlayer.reset()
        try {
            mMediaPlayer.setDataSource(fileName)
            mMediaPlayer.prepare()
            mMediaPlayer.start()
            mMediaPlayer.isLooping = true
            var volume = itemView.bar_vol.progress / 100f
            mMediaPlayer.setVolume(volume, volume)
            NatureManager.PlayList.put(fileName, mMediaPlayer)
            NatureManager.VolumeList.put(fileName, itemView.bar_vol.progress)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}