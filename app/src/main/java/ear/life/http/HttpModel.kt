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

/**
 * 用户
 *
 */
class UserModel(var id: Int,
                var slug: String,
                var name: String,
                var first_name: String,
                var last_name: String,
                var nickname: String,
                var url: String,
                var description: String
)


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
                   var author: UserModel
)

/**
 * 附加图片
 *
 */
class AttachmentModel(var id: Int, var url: String, var images: ImagesModel)

class ImagesModel(var full: ImageModel, var thumbnail: ImageModel, var medium: ImageModel, var medium_large: ImageModel, var large: ImageModel, var post_thumbnail: ImageModel)

class ImageModel(var url: String, var width: Int, var height: Int)
