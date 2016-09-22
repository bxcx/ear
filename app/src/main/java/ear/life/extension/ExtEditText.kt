package ear.life.extension

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Ext
 *
 * himi on 2016-09-22 18:21
 * version V1.0
 */
fun EditText.showSoftInput() {
    postDelayed({
        try {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, 0)

}

fun EditText.hideSoftInput() {
    try {
        val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}