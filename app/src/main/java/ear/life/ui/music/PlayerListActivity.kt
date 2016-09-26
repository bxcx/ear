package ear.life.ui.music

import android.app.Activity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.hm.library.base.BaseListActivity
import com.hm.library.base.BaseViewHolder
import com.hm.library.expansion.show
import com.hm.library.resource.recyclerview.PullRefreshLoadRecyclerView
import com.hm.library.resource.view.CustomToast
import com.hm.library.resource.view.TipsToast
import com.hm.library.util.ImageUtil
import ear.life.R
import ear.life.app.App
import kotlinx.android.synthetic.main.item_music_playlist.view.*
import org.jetbrains.anko.onClick

//swipeBack 右滑返回 默认为true
class PlayerListActivity : BaseListActivity<MusicLoader.MusicInfo, PlayerListActivity.PlayerListHolder>() {


    override fun setUIParams() {
        swipeBack = false
        menuResID = R.menu.menu_save

        //是否能刷新
        canRefesh = false
        //是否能加载更多
        canLoadmore = false
        //是否能拖拽排序
        canDrag = true
        //是否能侧滑
        canSwipe = true
        //侧滑方式 支持Layout(滑动菜单) Delete(滑动删除)
        swipeType = PullRefreshLoadRecyclerView.SwipeType.Layout

        itemResID = R.layout.item_music_playlist
    }

    override fun initUI() {
        super.initUI()
        title = "播放列表"

//        fragment?.onItemMove = { fromPosition, toPosition ->
//            showToast("onItemMove")
//            MusicListData.playList = adapter.data
//            ITingApp.AudioService.setList(MusicListData.playList!!)
//            Cacher[MusicListData.PlayList] = MusicListData.playList
//        }
//        fragment?.onItemDismiss = { position ->
//            showToast("onItemDismiss")
//            ITingApp.AudioService.setList(MusicListData.playList!!)
//            MusicListData.playList = adapter.data
//            Cacher[MusicListData.PlayList] = MusicListData.playList
//        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            MusicListData.playList = adapter.data
            App.AudioService.setList(MusicListData.playList)
            App.AudioService.currentIndex = 0

            val b = App.AudioService.mMediaPlayer.isPlaying
            App.AudioService.prepare()
            if (b) {
                App.AudioService.start()
            }

            MusicListData.saveList()

            showTips(TipsToast.TipType.Success, "保存成功")
            setResult(Activity.RESULT_OK)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun loadData() {
        loadCompleted(MusicListData.playList)
    }


    override fun getView(parent: ViewGroup?, position: Int): PlayerListHolder = PlayerListHolder(getItemView(parent))

    class PlayerListHolder(itemView: View) : BaseViewHolder<MusicLoader.MusicInfo>(itemView) {

        override fun setContent(position: Int) {
            itemView.tv_title.text = data.title
            itemView.tv_artist.text = data.albumAndArtist

            itemView.iv_img.show("file:/" + data.getAlbumArt(context), ImageUtil.SimpleDisplayImageOptions(R.drawable.ic_launcher))

            itemView.layout_top.onClick { CustomToast.makeText(context, "Top clicked", 0).show() }
            itemView.layout_delete.onClick {
                adapter.onItemDismiss(position)
                CustomToast.makeText(context, "Delete clicked", 0).show()
            }

        }

    }
}
