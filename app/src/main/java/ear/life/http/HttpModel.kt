package ear.life.http

import com.hm.library.http.HMModel

/**
 * HttpBase
 *
 * himi on 2016-09-15 09:21
 * version V1.0
 */
open class BaseModel : HMModel() {

    var status: String? = null
    var error: String = ""

    //数据是否正确
    override var valid: Boolean = false
        get() = "ok".equals(status)
    //相应提示
    override var message: String = ""
        get() = error
}