package ear.life.ui.music

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.support.v7.graphics.Palette
import android.widget.ImageView
import android.widget.TextView
import com.hm.library.base.BaseActivity
import com.hm.library.resource.view.TipsToast
import com.hm.library.resource.view.TipsToast.TipType.Smile
import com.hm.library.umeng.share.IShareCallback
import com.hm.library.umeng.share.ShareUtils
import com.hm.library.util.ArgumentUtil
import com.umeng.socialize.media.UMImage
import ear.life.R
import ear.life.app.App
import ear.life.ui.music.MusicLoader.MusicInfo
import kotlinx.android.synthetic.main.activity_player.*
import org.jetbrains.anko.*
import java.util.*


class PlayerActivity : BaseActivity() {

    val service = App.AudioService
    val player = service.mMediaPlayer
    var musicInfo: MusicInfo? = null
    var musicInfoID: Long = -1
    var mTimer: Timer = Timer()
    var mTimerTask: TimerTask? = null

    var su: ShareUtils? = null

    override fun setUIParams() {
        layoutResID = R.layout.activity_player
        hideActionBar = true
        immersedStatusbar = true
    }

    override fun checkParams(): Boolean {
        musicInfoID = intent.getLongExtra(ArgumentUtil.ID, -1)
        if (musicInfoID != -1.toLong())
            musicInfo = MusicListData[musicInfoID]
        return super.checkParams()
    }

    override fun initUI() {
        layout_back.onClick { finish() }
        layout_share.onClick { showTips(Smile, "开发中") }

        mTimer = Timer()
        mTimerTask = object : TimerTask() {
            override fun run() {
                onUiThread {
                    if (player.isPlaying) {
                        iv_play.setImageResource(R.drawable.icon_player_pause)
                    } else {
                        iv_play.setImageResource(R.drawable.icon_player_play)
                    }

                    val currentMusic = service.currentMusicInfo
                    if (currentMusic != null && currentMusic != musicInfo) {
                        musicInfo = currentMusic
                        album_art.progress = 0
                        setUI()

//                        if (player.isPlaying) {
//                            service.play(musicInfo) {}
//                        }
                    }

                    if (player.isPlaying && !album_art.isRotating) {
                        album_art.start()
                    } else if (!player.isPlaying && album_art.isRotating) {
                        album_art.stop()
                    }
                }
            }
        }
        mTimer.schedule(mTimerTask, 0, 2000)

        iv_share.setColorFilter(0xffffff)
        iv_share.onClick {
            if (musicInfo == null)
                return@onClick
            su = ShareUtils(act)
            su?.share(musicInfo?.title, "http://fir.im/ear", "总有一些音乐，宠坏了我们的耳朵", UMImage(ctx, R.drawable.ic_launcher), object : IShareCallback {
                override fun onSuccess() {
                    showTips(TipsToast.TipType.Smile, "分享成功")
                }

                override fun onFaild() {
                }

                override fun onCancel() {
                }
            })
        }

        iv_play.onClick {


            if (album_art.isRotating) {
                service.pause()
                album_art.stop()
                iv_play.setImageResource(R.drawable.icon_player_play)
            } else {
                if (player.duration == player.currentPosition) {
                    service.playMusic {}
                } else {
                    service.start()
                }
                album_art.start()
                iv_play.setImageResource(R.drawable.icon_player_pause)
            }
        }

        iv_play_next.onClick {
            service.playNextMusic {
                album_art.progress = 0
                musicInfo = it
                setUI()
            }
        }
        iv_play_last.onClick {
            service.playLastMusic {
                album_art.progress = 0
                musicInfo = it
                setUI()
            }
        }

        layout_play_list.onClick { startActivityForResult<PlayerListActivity>(1) }



        layout_player_mode.onClick {
            var modeStr: String = when (service.changePlayMode()) {
                PlayMode.Order -> "顺序播放"
                PlayMode.Random -> "随机播放"
                PlayMode.List -> "列表循环"
                PlayMode.Single -> "单曲循环"
            }
            var resID = initPlayMode()
            showTips(resID, modeStr)

        }
        if (musicInfo == null) {
            musicInfo = service.currentMusicInfo

            if (player.duration == player.currentPosition) {
                service.prepare()
                album_art.progress = 0
            } else {
                album_art.progress = (player.currentPosition / 1000f).toInt()

            }
            setUI()
            if (player.isPlaying) {
                album_art.start()
            }
            return
        }

        service.play(musicInfo) {
            MusicListData.saveList()
            setUI()
            album_art.start()
        }

    }

    fun setUI() {
        initPlayMode()
        if (musicInfo == null)
            return

        album_art.setMax((player.duration / 1000f).toInt())// + if (musicInfo!!.duration % 1000 != 0) 1 else 0)

        tv_name.text = musicInfo?.title
        tv_artist.text = if (musicInfo?.artist!!.contains("unknow")) "" else musicInfo?.artist

        var bmp = BitmapFactory.decodeFile(musicInfo?.getAlbumArt(this))
        if (bmp == null) {
            bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
        }

        album_art.setCoverDrawable(BitmapDrawable(bmp))
        Palette.generateAsync(bmp) { palette ->
            val array_bodyTextColor = arrayOf(tv_name, tv_artist, iv_back, iv_share, iv_play, iv_play_last, iv_play_next, iv_player_mode, iv_play_list)

            val mutedSwatch = palette.mutedSwatch
            var rgb = -1
            if (mutedSwatch != null) {
                rgb = mutedSwatch.rgb

                layout.backgroundColor = rgb
                iv_circle.setColorFilter(rgb)

                array_bodyTextColor.forEach {
                    if (it is TextView)
                        it.textColor = mutedSwatch.bodyTextColor
                    else if (it is ImageView)
                        it.setColorFilter(mutedSwatch.bodyTextColor)
                }
            } else {
                layout.backgroundColor = 0xff5F9EA0.toInt()
                iv_circle.setColorFilter(0xff5F9EA0.toInt())

                array_bodyTextColor.forEach {
                    if (it is TextView)
                        it.textColor = Color.WHITE
                    else if (it is ImageView)
                        it.setColorFilter(Color.WHITE)
                }
            }
        }
    }

    fun initPlayMode(): Int {
        var resID: Int = when (service.playMode) {
            PlayMode.Order -> R.drawable.icon_play_order
            PlayMode.Random -> R.drawable.icon_play_random
            PlayMode.List -> R.drawable.icon_play_round_list
            PlayMode.Single -> R.drawable.icon_play_round_single
        }
        iv_player_mode.setImageResource(resID)

        return resID
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> album_art.progress = 0
            }
            su?.onActivityResult(requestCode, resultCode, data)
        }
    }

}
