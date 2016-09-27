package ear.life.ui.music

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.hm.library.app.Cacher
import com.orhanobut.logger.Logger
import ear.life.ui.music.MusicLoader.MusicInfo
import java.io.File
import java.util.*

enum class PlayMode {
    Order, Random, Single, List
}

/**
 * MusicService
 *
 *
 * himi on 2016-08-13 17:01
 * version V1.0
 */
class AudioService : Service(), MediaPlayer.OnCompletionListener {

    internal var playMode = PlayMode.Order
    internal var mMediaPlayer: MediaPlayer = MediaPlayer()
    internal var mPlayList: ArrayList<MusicInfo> = ArrayList()
    internal var lastIndex: Int = -1
    internal var currentIndex: Int = 0

    internal var init = false

    val currentMusicInfo: MusicInfo?
        get() {
            if (mPlayList.size > currentIndex)
                return mPlayList[currentIndex]
            return null
        }

    fun changePlayMode(): PlayMode {
        playMode = when (playMode) {
            PlayMode.Order -> PlayMode.Random
            PlayMode.Random -> PlayMode.Single
            PlayMode.Single -> PlayMode.List
            PlayMode.List -> PlayMode.Order
        }
        Cacher[MusicListData.PlayMode] = playMode
        return playMode
    }

    private val binder = AudioBinder()


    override fun onBind(arg0: Intent): IBinder? {
        return binder
    }


    fun addList(musicInfoList: List<MusicInfo>) {
        musicInfoList.forEach { add(it) }
    }

    fun setList(musicInfoList: List<MusicInfo>) {
        mPlayList.clear()
        mPlayList = musicInfoList as ArrayList<MusicInfo>
    }

    fun add(musicInfo: MusicInfo?, index: Int = -1): String {
        if (musicInfo == null)
            return "歌曲不存在"
        if (mPlayList.contains(musicInfo)) {
            return "已在播放列表中"
        } else {
            if (index == -1)
                mPlayList.add(musicInfo)
            else
                mPlayList.add(index, musicInfo)
            return "添加成功"
        }
    }

    fun del(musicInfo: MusicInfo?): String {
        if (musicInfo == null)
            return "歌曲不存在"
        if (mPlayList.contains(musicInfo)) {
            mPlayList.remove(musicInfo)
            return "已删除"
        } else {
            return "不在播放列表中"
        }
    }

    fun move(fromPosition: Int, toPosition: Int) {
        var musicInfo = mPlayList.removeAt(fromPosition)
        mPlayList.add(if (toPosition > fromPosition) toPosition - 1 else toPosition, musicInfo)
    }

    fun play(musicInfo: MusicInfo?, completionHandler: (MusicInfo) -> Unit) {
        if (musicInfo == null)
            return

        if (mPlayList.contains(musicInfo)) {
            move(mPlayList.indexOf(musicInfo), 0)
        } else {
            add(musicInfo, 0)
        }
        currentIndex = 0
        playMusic(completionHandler)
    }

    fun playNextMusic(completionHandler: (MusicInfo) -> Unit) {
        lastIndex = currentIndex
        currentIndex = getNextMusic()
        playMusic(completionHandler)
    }

    fun playLastMusic(completionHandler: (MusicInfo) -> Unit) {
        currentIndex = getLastMusic()
        playMusic(completionHandler)
    }

    fun getNextMusic(): Int {
        var index = -1
        when (playMode) {
            PlayMode.Order -> index = currentIndex + 1
            PlayMode.Random -> {
                index = Random().nextInt(mPlayList.size)
                if (index == currentIndex)
                    index = currentIndex + 1
            }
            PlayMode.Single -> index = currentIndex
            PlayMode.List -> index = currentIndex + 1
        }

        if (index > mPlayList.size - 1)
            index = 0

        return index

    }

    fun getLastMusic(): Int {
        var index = lastIndex
        when (playMode) {
            PlayMode.Order -> index = currentIndex - 1
            PlayMode.Random -> index = lastIndex
            PlayMode.Single -> index = currentIndex
            PlayMode.List -> index = currentIndex - 1
        }
        if (index < 0)
            index = 0
        return index
    }

    fun prepare(): MusicInfo {
        val musicInfo = mPlayList[currentIndex]

        mMediaPlayer.reset()
        mMediaPlayer.setDataSource(musicInfo.url)
        mMediaPlayer.prepare()
        return musicInfo
    }

    fun playMusic(completionHandler: (MusicInfo) -> Unit) {
        if (mPlayList.size == 0)
            return
        if (!File(mPlayList[currentIndex].url).exists()) {
            playNextMusic {}
            return
        }

        val musicInfo = prepare()
        Logger.w(musicInfo.duration.toString())
        mMediaPlayer.start()
        completionHandler(musicInfo)

        if (!init)
            init = true
    }

    fun start() {
        mMediaPlayer.start()
        if (!init)
            init = true
    }

    fun pause() {
        mMediaPlayer.pause()
    }

    /**
     * 当Audio播放完的时候触发该动作
     */
    override fun onCompletion(player: MediaPlayer) {
        if (!init) {
            init = true
            mMediaPlayer.seekTo(0)
            return
        }
        playNextMusic { }
    }

    //在这里我们需要实例化MediaPlayer对象
    override fun onCreate() {
        super.onCreate()
        mMediaPlayer.setOnCompletionListener(this)
        mPlayList = MusicListData.playList
        if (MusicListData.playMode == null) {
            playMode = PlayMode.Order
        } else {
            playMode = MusicListData.playMode!!
        }
    }

    /**
     * 该方法在SDK2.0才开始有的，替代原来的onStart方法
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
        }
        mMediaPlayer.release()
    }

    //为了和Activity交互，我们需要定义一个Binder对象
    inner class AudioBinder : Binder() {

        //返回Service对象
        val service: AudioService
            get() = this@AudioService
    }

}