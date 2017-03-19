package ear.life.ui.article


import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.google.gson.Gson
import com.hm.hmlibrary.ui.article.CategorieListModel
import com.hm.library.app.Cacher
import com.hm.library.base.BaseFragment
import com.hm.library.resource.draggridview.DragGridActivity
import com.hm.library.resource.view.TipsToast
import ear.life.R
import kotlinx.android.synthetic.main.fragment_article.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.startActivity
import java.util.*

/**
 * 妙笔
 */
class ArticleFragment(override var layoutResID: Int = R.layout.fragment_article) : BaseFragment() {

    companion object {
        val Channel1st = "Channel1st2"
        val ChannelAll = "ChannelAll"
        val MyChannel = "MyChannel"
    }

    //分类
    var channelList: ArrayList<CategorieListModel.CategorieModel>? = null

    override fun loadData() {
        //v1.0.5版本去掉用户自定义功能
        //先加载用户本地保存的分类信息,如果用户有自己保存则只显示用户自己的
//        channelList = Cacher[MyChannel]

        //如果没有用户评阅的分类, 那查看本地有没有缓存所有一级频道
        if (channelList == null || channelList!!.size == 0)
            channelList = Cacher[Channel1st]

        //好吧, 都没有的话, 我们查询API
        if (channelList == null || channelList!!.size == 0) {
            //v1.0.6版本去掉用户自定义功能
            val json = "{'status':'ok','categories':[{'id':-1,'title':'\u6700\u65b0'},{'id':22,'title':'\u4e50\u5668'},{'id':64,'title':'\u89c6\u9891'},{'id':59,'title':'\u6563\u6587'},{'id':2,'title':'\u7535\u53f0'},{'id':45,'title':'\u5fc3\u60c5'},{'id':10,'title':'\u66f2\u98ce'}]}"
            channelList = Gson().fromJson(json, CategorieListModel::class.java).categories
            initUI()

//            HMRequest.go<CategorieListModel>(HttpServerPath.Server_Category, activity = act) {
//                channelList = it?.categories
            //在最前面加一个"最新"的频道, 按发布时间来查看
//                val all = CategorieListModel.CategorieModel(-1, "", "最新", "", 0, -1)
//                channelList!!.add(0, all)
            //将频道保存到本地
            //Cacher[Channel1st] = channelList
//                initUI()
//            }

        } else {
            //上面的情况任意一种查询到频道信息后, 填充UI
            initUI()
        }
    }

    override fun onResume() {
        super.onResume()
        if (channelList == null || channelList!!.size == 0) {
            loadData()
        }
    }

    override fun initUI() {
        super.initUI()

        if (pager == null) {
            showToast("初始化错误,请重试")
            return
        }

        pager.adapter = MyPagerAdapter()
        tabs.setViewPager(pager)
        tabs.setCurrentPosition(0)
        iv_search.onClick {
            startActivity<SearchActivity>()
        }
//        iv_plus.onClick {
//            showLoading()
//
//            val params = HashMap<String, Any>()
//            params.put("json", "get_category_index")
//            HMRequest.go<CategorieListModel>(params = params, activity = act) {
//                cancelLoading()
//
//                var channelAll = it?.categories
//                if (channelAll != null && channelAll.size > 0) {
//
////                    channelAll.sortByDescending { it.post_count }
//                    val all = CategorieListModel.CategorieModel(-1, "", "最新", "", 0, -1)
//                    channelAll.add(0, all)
//                    Cacher[Channel1st] = channelAll.filter { it.parent == 0 }
//                    Cacher[ChannelAll] = channelAll
//
//                    val userList: ArrayList<CategorieListModel.CategorieModel>? = Cacher[MyChannel]
//
//                    val mUserList = ArrayList<String>()
//                    val mOtherList = ArrayList<String>()
//
//                    if (userList == null) {
//                        mUserList.add("最新")
//                        mUserList.add("乐器")
//                    } else {
//                        for (channel in userList) {
//                            mUserList.add(channel.title)
//                        }
//                    }
//                    for (channel in channelAll.filter { it.parent == 0 }) {
//                        val title = channel.title
//                        if (mUserList.contains(title))
//                            continue
//                        mOtherList.add(title)
//                    }
//
//                    startActivityForResult<DragGridActivity>(DragGridActivity.Selection, DragGridActivity.UserList to mUserList, DragGridActivity.OtherList to mOtherList)
//                }
//            }
//
//        }
    }

    inner class MyPagerAdapter : FragmentPagerAdapter(fragmentManager) {

        override fun getPageTitle(position: Int): CharSequence = channelList!![position].title

        override fun getCount(): Int = channelList!!.size

        override fun getItem(position: Int): Fragment = ArticleListFragment.newInstance(channelList!![position].id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showTips(TipsToast.TipType.Success, "保存成功")
        val list = data!!.getStringArrayListExtra(DragGridActivity.UserList)
        val channelAll: ArrayList<CategorieListModel.CategorieModel> = Cacher[ChannelAll]!!

        val userList: ArrayList<CategorieListModel.CategorieModel> = ArrayList()
        for (user in list) {
            for (channel in channelAll) {
                if (user == channel.title) {
                    userList.add(channel)
                    break
                }
            }
        }
        Cacher[MyChannel] = userList
        loadData()
    }
}
