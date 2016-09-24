package ear.life.app

import android.app.Activity
import com.hm.library.app.Cacher
import com.hm.library.app.HMApp
import com.hm.library.http.HMRequest
import com.hm.library.http.Method
import ear.life.http.CookieModel
import ear.life.http.HttpServerPath
import ear.life.http.UserModel
import ear.life.ui.LoginActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

/**
 * App
 *
 * himi on 2016-09-14 12:22
 * version V1.0
 */
class App : HMApp() {

    private val CER = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGAjCCBOqgAwIBAgISA1pyhye246sNmLPw3nJ+iDC8MA0GCSqGSIb3DQEBCwUA\n" +
            "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
            "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xNjA5MjAxMTUwMDBaFw0x\n" +
            "NjEyMTkxMTUwMDBaMBMxETAPBgNVBAMTCGVhci5saWZlMIICIjANBgkqhkiG9w0B\n" +
            "AQEFAAOCAg8AMIICCgKCAgEAvV6sNXr6B2eVynj1O+GEAnfcwKrCMkgkHimyqi9w\n" +
            "7QdYjn4B8zVYKSdMuZ35Rm/XwR3ScohdlyA2VklRAW0hbL6V8bHguY1JFb5O00uF\n" +
            "9LmnBJFafGvM+HbF3WcF9X1mUxtiGXJBvyksfwGvJ+sEJ/vgHFC2a6ybkrkIUier\n" +
            "U0jmj90kIuKf4p4K5lz/klHju2wgqfwk1lO50qjrbMFS3YLgT9u08y5rH9SwD39o\n" +
            "EQIoIXXgLlnKGgQimJ4UgJNYOpPcIKTZohneYER8lNqeR6h8Osc/V3Kg3ljAtgko\n" +
            "ewR4Um6E2P5bOaGq3TWgfo25HZ99QOtG9yBElsMyyBbbJIaUJQE73tk3Ps7EblDB\n" +
            "1M+uTCVWYN8dpxwJ+C9i+O/LZ+neamYoIRoH6W30e6T+DKOP5rYsBVGId/gBwcDQ\n" +
            "cYIm1zySJa2OmDIBB/9ygjWcwE2gQlGMgX9h6KxDpc7OTvd8BcguXHRzksViKi+P\n" +
            "5SIQLAzeBjwNQcBFd4w4lr6Q4slOAPFPAeP7mPxNAvS5hxDtqlZnP5OtuiKVykVP\n" +
            "5hhqg/F61f76O3YefCKX9T17Oo0MG4m11+tePP1g18LtdnpHeloJAC/STlAcsgUF\n" +
            "XgIOh3/ATkNIT4mjkapeNy6WpQevVMW04jfq4jJpWpfnrEVe+8TB76Z9s7w55lFB\n" +
            "QYECAwEAAaOCAhcwggITMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEF\n" +
            "BQcDAQYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQUspvbpTu1KDaw\n" +
            "SHjSCCJ2xIIB6M4wHwYDVR0jBBgwFoAUqEpqYwR93brm0Tm3pkVl7/Oo7KEwcAYI\n" +
            "KwYBBQUHAQEEZDBiMC8GCCsGAQUFBzABhiNodHRwOi8vb2NzcC5pbnQteDMubGV0\n" +
            "c2VuY3J5cHQub3JnLzAvBggrBgEFBQcwAoYjaHR0cDovL2NlcnQuaW50LXgzLmxl\n" +
            "dHNlbmNyeXB0Lm9yZy8wIQYDVR0RBBowGIIIZWFyLmxpZmWCDHd3dy5lYXIubGlm\n" +
            "ZTCB/gYDVR0gBIH2MIHzMAgGBmeBDAECATCB5gYLKwYBBAGC3xMBAQEwgdYwJgYI\n" +
            "KwYBBQUHAgEWGmh0dHA6Ly9jcHMubGV0c2VuY3J5cHQub3JnMIGrBggrBgEFBQcC\n" +
            "AjCBngyBm1RoaXMgQ2VydGlmaWNhdGUgbWF5IG9ubHkgYmUgcmVsaWVkIHVwb24g\n" +
            "YnkgUmVseWluZyBQYXJ0aWVzIGFuZCBvbmx5IGluIGFjY29yZGFuY2Ugd2l0aCB0\n" +
            "aGUgQ2VydGlmaWNhdGUgUG9saWN5IGZvdW5kIGF0IGh0dHBzOi8vbGV0c2VuY3J5\n" +
            "cHQub3JnL3JlcG9zaXRvcnkvMA0GCSqGSIb3DQEBCwUAA4IBAQBfPwwQTEfv/oZ/\n" +
            "elLm/vvGjN9v+Ktr+GBErt0cKkhrJIdRaqqYasSjEl3AqA/IHBFZ0GieKlDgRBKb\n" +
            "jZaOXBXh/aHyENOcA8Kg9f84p8BIeLq2CZuSi+93sFsB9iEqm1zqvm/9CTJQBC+p\n" +
            "JSgGtybt+t+8EFw8ZVHO2KKva+K2gTu2LhLjsdDJiREWsaW1vl7YeHzymhz8RSIw\n" +
            "dASmXMo+CCwkx08fJBMCj6jvst0c2m+0QYUXz7jN/ydIFA5yS6BCVE/V0Zpy1U2G\n" +
            "fvOMndKoC+VXuCDdgHCRRz0tEKSlsQnO/v0xEqLwDNY72wr+GIvgpG5enWI0Iylm\n" +
            "0CofANXm\n" +
            "-----END CERTIFICATE-----"


    companion object {
        val COOKIE: String = "COOKIE"
        var cookie: CookieModel? = null
        var user: UserModel? = null

        fun checkCookie(act: Activity): Boolean {
            if (cookie == null) {
                act.toast("请先登录")
                act.startActivity<LoginActivity>()
                return false
            }
            return true
        }

        fun updateCookie(cookie: CookieModel?) {
            if (cookie == null || !cookie.hm_valid) {
                this.cookie = null
                this.user = null
                Cacher[COOKIE] = null
            } else {
                this.cookie = cookie
                this.user = cookie.user
                Cacher[COOKIE] = cookie
            }
            HMRequest.params = createParams
        }

        val createParams: HashMap<String, Any>
            get() {
                val params = HashMap<String, Any>()
                if (cookie != null) {
                    params.put("cookie", cookie!!.cookie)
                }
                return params
            }

        val createNameAndEmailParams: HashMap<String, Any>
            get() {
                val params = HashMap<String, Any>()
                if (cookie != null) {
                    params.put("cookie", cookie!!.cookie)
                    params.put("name", cookie!!.user.nickname)
                    params.put("email", cookie!!.user.email)
                }
                return params
            }
    }

    override fun onCreate() {
        super.onCreate()
        updateCookie(Cacher[COOKIE])

        HMRequest.method = Method.POST
        HMRequest.server = HttpServerPath.MAIN
        HMRequest.parse = HttpServerPath
//        OkHttpUtils.getInstance().setCertificates(Buffer().writeUtf8(CER).inputStream())
    }
}