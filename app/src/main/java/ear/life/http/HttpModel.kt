package ear.life.http

import com.hm.library.http.HMModel
import java.io.Serializable

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
    override var hm_valid: Boolean = false
        get() = "ok" == status
    //相应提示
    override var hm_message: String = ""
        get() = error
}

class HttpFirVersion(var installUrl: String, var changelog: String, var versionShort: String, var version: Int)

class CookieModel(var cookie: String, var cookie_name: String, var user: UserModel) : BaseModel()
class CookieValidModel(var valid: Boolean) : BaseModel()
class NoncedModel(var controller: String, var method: String, var nonce: String) : BaseModel()

/**
 * 用户
 *
 */
class UserModel(var id: Int,
                var slug: String,
                var name: String,
                var first_name: String,
                var last_name: String,
                var username: String,
                var nickname: String,
                var url: String,
                var avatar: String,
                var email: String,
                var description: String
) : Serializable


/**
 * 评论
 *
 */
class CommentModel(var id: Int,
                   var slug: String,
                   var name: String,
                   var url: String,
                   var date: String,
                   var content: String,
                   var parent: Int,
                   var author: UserModel?
) : Serializable {

}

class FavoriteModel(var before: Boolean, var after: Boolean) : BaseModel()

/**
 * 附加图片
 *
 */
class AttachmentModel(var id: Int, var url: String, var images: ImagesModel) : Serializable

class ImagesModel(var full: ImageModel, var thumbnail: ImageModel, var medium: ImageModel, var medium_large: ImageModel, var large: ImageModel, var post_thumbnail: ImageModel) : Serializable

class ImageModel(var url: String, var width: Int, var height: Int) : Serializable
