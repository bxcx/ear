package ear.life.ui.music

import android.app.Activity
import android.view.MenuItem
import android.view.ViewGroup
import com.hm.library.app.Cacher
import com.hm.library.base.BaseListActivity
import com.hm.library.resource.FileUtil
import com.hm.library.resource.MediaScanner
import com.hm.library.resource.sortlistview.CharacterParser
import com.hm.library.resource.view.TipsToast
import ear.life.R
import ear.life.app.App
import ear.life.ui.music.LinghtMusicFragment.MusicFileHolder
import ear.life.ui.music.MusicLoader.MusicInfo
import org.jetbrains.anko.async
import org.jetbrains.anko.ctx
import java.io.File
import java.util.*


object MusicListData {

    val PlayIDS: String = "PlayIDS"
    val PlayMode: String = "PlayMode"

    var characterParser: CharacterParser? = null
    var pinyinComparator: PinyinComparator? = null

    var musicList: ArrayList<MusicInfo>? = null
    var playList: ArrayList<MusicInfo> = ArrayList()
    var playIDs: ArrayList<Long>? = null
    var playMode: PlayMode? = null

    var selectedMusicList: ArrayList<MusicInfo> = ArrayList()

    init {
        characterParser = CharacterParser.getInstance()
        pinyinComparator = PinyinComparator()

        playIDs = Cacher[PlayIDS]
        playMode = Cacher[PlayMode]

        refresh {
            if (playMode == null)
                playMode = ear.life.ui.music.PlayMode.Order
            if (playIDs == null) {
                playList = ArrayList()
            } else {
                playIDs!!.forEach { pid ->
                    val music = musicList!!.singleOrNull { it.id == pid }
                    if (music != null && File(music.url).exists())
                        playList.add(music)
                }
            }
        }
    }

    fun refresh(completionHandler: () -> Unit?) {
        if (App.ContentResolver == null)
            return
        MusicLoader.instance(App.ContentResolver!!).referesh {
            musicList = it as ArrayList<MusicInfo>
            musicList!!.forEach {
                val pinyin = characterParser!!.getSelling(it.title)
                val sortString = pinyin.substring(0, 1).toUpperCase()
                if (sortString.matches("[A-Z]".toRegex())) {
                    it.sortLetters = sortString.toUpperCase()
                } else {
                    it.sortLetters = "#"
                }
            }

            Collections.sort(musicList, pinyinComparator)
            completionHandler.invoke()
        }
    }

    fun saveList() {
        val ids: ArrayList<Long> = ArrayList()
        playList.forEach { ids.add(it.id) }
        Cacher[MusicListData.PlayIDS] = ids
    }

    operator fun get(id: Long) = musicList?.first { it.id == id }
}

/**
 * MusicListFragment
 *
 * himi on 2016-08-10 16:50
 * version V1.0
 */
class MusicListActivity(override var menuResID: Int = R.menu.menu_music_import) : BaseListActivity<MusicInfo, MusicFileHolder>() {


    override fun setUIParams() {
        fragment = LinghtMusicFragment.newInstance(false)
    }

    override fun onViewCreated() {
        super.onViewCreated()
        title = "选择音乐并导入"
        showTips(TipsToast.TipType.Smile, "请选择您最喜欢的5首歌曲")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_import) {
            doImport()
        }
        return super.onOptionsItemSelected(item)
    }

    fun doImport() {
        showLoading("请稍候")
        if (MusicListData.selectedMusicList.size == 0) {
            scan()
            return
        }
        async() {
            MusicListData.selectedMusicList.forEach {
                var newFile = File(it.url)
                newFile = File(App.LightMusicPath, newFile.name)
                FileUtil.copyFile(it.url, newFile.absolutePath)
            }

            scan()
        }
    }

    fun scan() {
        MediaScanner.scanFile(ctx, arrayOf(App.LightMusicPath), null) { path, uri ->
            runDelayed(
                    {
                        MusicListData.refresh({
                            cancelLoading()
                            if (MusicListData.selectedMusicList.size > 0) {
                                showTips(TipsToast.TipType.Success, "成功导入${MusicListData.selectedMusicList.size}首歌曲")
                                MusicListData.selectedMusicList.clear()
                            }
                            setResult(Activity.RESULT_OK)
                            finish()
                        })
                    }, 2000)
        }

    }

    override fun getView(parent: ViewGroup?, position: Int): MusicFileHolder = MusicFileHolder(getItemView(parent))

}


