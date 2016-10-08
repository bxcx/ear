package ear.life.ui.music

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.hm.library.base.BaseActivity
import com.hm.library.base.BaseListFragment
import com.hm.library.base.BaseViewHolder
import com.hm.library.expansion.show
import com.hm.library.resource.view.ActionSheetDialog
import com.hm.library.resource.view.TipsToast
import com.hm.library.util.ArgumentUtil
import com.hm.library.util.ImageUtil
import ear.life.R
import ear.life.app.App
import ear.life.ui.article.ArticleDetailActivity
import ear.life.ui.music.MusicLoader.MusicInfo
import kotlinx.android.synthetic.main.fragment_music_list.*
import kotlinx.android.synthetic.main.item_music.view.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onLongClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import java.io.File

/**
 * LinghtMusicFragment
 *
 * himi on 2016-08-11 20:32
 * version V1.0
 */
open class LinghtMusicFragment : BaseListFragment<MusicInfo, LinghtMusicFragment.MusicFileHolder>() {

    var isLight = true

    companion object {
        val Action_Import = 1

        fun newInstance(isLight: Boolean): LinghtMusicFragment {
            val fragment = LinghtMusicFragment()
            val args = Bundle()
            args.putBoolean(ArgumentUtil.ID, isLight)
            fragment.arguments = args
            return fragment
        }
    }

    override fun checkParams(): Boolean {
        if (arguments != null)
            isLight = arguments.getBoolean(ArgumentUtil.ID, true)
        return true
    }

    override fun setUIParams() {
        layoutResID = R.layout.fragment_music_list
        itemResID = R.layout.item_music
        canRefesh = isLight
        canLoadmore = false
    }

    override fun initUI() {
        sidebar.setTextView(dialog)

        adapter = SortAdapter(this, context)

        sidebar.setOnTouchingLetterChangedListener { s ->
            val position = (adapter as SortAdapter).getPositionForSection(s[0].toInt())
            if (position != -1) {
                recyclerView!!.recyclerView.scrollToPosition(position)
            }
        }

        super.initUI()

        if (isLight) {
            layout_icon_import.onClick { startActivityForResult<MusicListActivity>(Action_Import) }
            layout_icon_playlist.onClick {
                if (MusicListData.playList.size > 0)
                    startActivity<PlayerActivity>()
                else
                    showTips(TipsToast.TipType.Error, "播放列表中还没有歌曲")
            }
        } else {
            layout_title.visibility = View.GONE
        }

//        val audioWidget = AudioWidget.Builder(context).build()
//        audioWidget.show(100, 100)
    }

    override fun onResume() {
        super.onResume()
        if (ArticleDetailActivity.needRefeshMusicList) {
            ArticleDetailActivity.needRefeshMusicList = false
            loadRefresh()
        }
    }

    override fun loadRefresh() {
        MusicListData.refresh { super.loadRefresh() }
    }

    override fun loadData() {
        if (MusicListData.musicList == null) {
            App.ContentResolver = act.contentResolver
            loadRefresh()
            return
        }
        if (MusicListData.musicList == null) {
            showToast("加载本地音乐出错,请反馈")
            return
        }

        var musicList = if (isLight) {
            MusicListData.musicList!!.filter { it.url!!.contains(App.LightMusicPath) }
        } else {
            MusicListData.musicList!!.filter { !it.url!!.contains(App.LightMusicPath) }
        }
        loadCompleted(musicList)
    }

    override fun getView(parent: ViewGroup?, position: Int): MusicFileHolder {
        val m = MusicFileHolder(getItemView(parent))
        m.isLight = isLight
        return m
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Action_Import -> loadRefresh()
            }
    }

    class MusicFileHolder(itemView: View) : BaseViewHolder<MusicInfo>(itemView) {

        var isLight = true

        override fun setContent(position: Int) {
            itemView.tv_title.text = data.title
            itemView.tv_artist.text = data.albumAndArtist

            itemView.iv_img.show("file:/" + data.getAlbumArt(context), ImageUtil.SimpleDisplayImageOptions(R.drawable.ic_launcher))

            if (isLight) {
                itemView.iv_seleced.visibility = View.GONE
            } else {
                itemView.iv_seleced.visibility = View.VISIBLE
                itemView.iv_seleced.setImageResource(if (MusicListData.selectedMusicList.contains(data)) R.drawable.icon_music_select else R.drawable.icon_music_unselect)
            }

            itemView.layout.onClick({
                if (isLight) {
                    context.startActivity<PlayerActivity>(ArgumentUtil.ID to data.id)
                } else {
                    if (MusicListData.selectedMusicList.size > 4) {
                        val act = context as BaseActivity
                        act.showTips(TipsToast.TipType.Warning, "每次最多可以导入5首歌曲")
                        return@onClick
                    }

                    if (MusicListData.selectedMusicList.contains(data)) {
                        MusicListData.selectedMusicList.remove(data)
                    } else {
                        MusicListData.selectedMusicList.add(data)
                    }
                    itemView.iv_seleced.setImageResource(if (MusicListData.selectedMusicList.contains(data)) R.drawable.icon_music_select else R.drawable.icon_music_unselect)
                }
            })

            itemView.layout.onLongClick {
                ActionSheetDialog(context).builder().setTitle("确认删除这首音乐吗?")
                        .addSheetItem("删除", Color.RED, {
                            if (File(data.url).delete()) {
                                MusicListData.musicList!!.remove(data)
                                adapter.data.remove(data)
                                adapter.notifyDataSetChanged()
                            }
                        })
                        .show()

                true
            }
        }

    }

    class SortAdapter(baseListFragment: BaseListFragment<MusicInfo, MusicFileHolder>, context: Context) : BaseListAdapter<MusicInfo, LinghtMusicFragment.MusicFileHolder>(baseListFragment, context) {

        fun getPositionForSection(section: Int): Int {
            for (i in 0..data.size - 1) {
                val sortStr = data[i].sortLetters!!
                val firstChar = sortStr.toUpperCase()[0]
                if (firstChar.toInt() == section) {
                    return i
                }
            }

            return -1
        }
    }

}