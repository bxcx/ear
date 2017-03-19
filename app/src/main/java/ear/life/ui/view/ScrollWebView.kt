package ear.life.ui.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView


/**
 * ScrollWebView
 *
 * himi on 2017-03-19 13:34
 * version V1.0
 */
class ScrollWebView : WebView {
    var listener: OnScrollChangeListener? = null

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context) : super(context) {}

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val webcontent = contentHeight * scale// webview的高度
        val webnow = (height + scrollY).toFloat()// 当前webview的高度
        if (Math.abs(webcontent - webnow) < 1) {
            // 已经处于底端
            // Log.i("TAG1", "已经处于底端");
            listener?.onPageEnd(l, t, oldl, oldt)
        } else if (scrollY == 0) {
            // Log.i("TAG1", "已经处于顶端");
            listener?.onPageTop(l, t, oldl, oldt)
        } else {
            listener?.onScrollChanged(l, t, oldl, oldt)
        }
    }

    fun setOnScrollChangeListener(listener: OnScrollChangeListener) {
        this.listener = listener
    }

    interface OnScrollChangeListener {
        fun onPageEnd(l: Int, t: Int, oldl: Int, oldt: Int)
        fun onPageTop(l: Int, t: Int, oldl: Int, oldt: Int)
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
    }
}