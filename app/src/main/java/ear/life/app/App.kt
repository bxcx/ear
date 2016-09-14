package ear.life.app

import com.hm.library.app.HMApp
import com.hm.library.http.HMRequest
import com.hm.library.http.Method

/**
 * App
 *
 * himi on 2016-09-14 12:22
 * version V1.0
 */
class App : HMApp() {

    override fun onCreate() {
        super.onCreate()
        HMRequest.method = Method.POST
    }
}