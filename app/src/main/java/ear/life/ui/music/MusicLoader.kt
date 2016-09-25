package ear.life.ui.music

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore.Audio.Media
import android.text.TextUtils
import java.io.File
import java.util.*

/**
 * MusicLoader
 *
 *
 * himi on 2016-08-10 16:44
 * version V1.0
 */
class MusicLoader private constructor() {
    //Uri，指向external的database
    private val contentUri = Media.EXTERNAL_CONTENT_URI
    //projection：选择的列; where：过滤条件; sortOrder：排序。
    private val projection = arrayOf(Media._ID, Media.TITLE, Media.DATA, Media.ALBUM, Media.ARTIST, Media.DURATION, Media.SIZE, Media.ALBUM_ID)
    private val where = "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0 "
    private val sortOrder = Media.DATA

    init {
        referesh {}
    }

    fun referesh(completionHandler: (List<MusicInfo>) -> Unit?) {
        musicList.clear()
        //利用ContentResolver的query函数来查询数据，然后将得到的结果放到MusicInfo对象中，最后放到数组中
        val cursor = contentResolver!!.query(contentUri, projection, where, null, sortOrder)
        if (cursor == null) {
            //            Log.v(TAG, "Line(37	)	Music Loader cursor == null.");
        } else if (!cursor.moveToFirst()) {
            //            Log.v(TAG, "Line(39	)	Music Loader cursor.moveToFirst() returns false.");
        } else {
            val displayNameCol = cursor.getColumnIndex(Media.TITLE)
            val albumCol = cursor.getColumnIndex(Media.ALBUM)
            val idCol = cursor.getColumnIndex(Media._ID)
            val durationCol = cursor.getColumnIndex(Media.DURATION)
            val sizeCol = cursor.getColumnIndex(Media.SIZE)
            val artistCol = cursor.getColumnIndex(Media.ARTIST)
            val urlCol = cursor.getColumnIndex(Media.DATA)
            val albumidCol = cursor.getColumnIndex(Media.ALBUM_ID)
            do {
                val title = cursor.getString(displayNameCol)
                val album = cursor.getString(albumCol)
                val id = cursor.getLong(idCol)
                val albumid = cursor.getLong(albumidCol)
                val duration = cursor.getInt(durationCol)
                val size = cursor.getLong(sizeCol)
                val artist = cursor.getString(artistCol)
                val url = cursor.getString(urlCol)

                if (duration / 1000 > 30 && File(url).exists()) {
                    val musicInfo = MusicInfo(id, title)
                    musicInfo.album = album
                    musicInfo.albumid = albumid
                    musicInfo.duration = duration
                    musicInfo.size = size
                    musicInfo.artist = artist
                    musicInfo.url = url
                    musicList.add(musicInfo)
                }

            } while (cursor.moveToNext())
        }

        completionHandler.invoke(musicList)
    }

    public fun getMusicList(): List<MusicInfo> {
        return musicList
    }

    fun getMusicUriById(id: Long): Uri {
        val uri = ContentUris.withAppendedId(contentUri, id)
        return uri
    }

    //下面是自定义的一个MusicInfo子类，实现了Parcelable，为的是可以将整个MusicInfo的ArrayList在Activity和Service中传送，=_=!!,但其实不用
    class MusicInfo : Parcelable {
        var id: Long = 0
        var albumid: Long = 0
        var title: String? = null
        var album: String? = null
        var duration: Int = 0
        var size: Long = 0
        var artist: String? = null
        var url: String? = null
        var abumArt: String? = null

        var albumArtist: String = ""

        var sortLetters: String? = null

        constructor() {

        }

        constructor(pId: Long, pTitle: String) {
            id = pId
            title = pTitle
        }

        override fun toString(): String {
            return title + ""
        }

        val albumAndArtist: String
            get() {
                if (TextUtils.isEmpty(albumArtist)) {
                    albumArtist = "${artist} - ${album}"
                    albumArtist = albumArtist.replace("null", "")
                    albumArtist = albumArtist.replace("<unknown>", "")
                    albumArtist = albumArtist.trim()
                    if (albumArtist.startsWith("-"))
                        albumArtist = albumArtist.substring(1, albumArtist.length)
                    if (albumArtist.endsWith("-"))
                        albumArtist = albumArtist.substring(0, albumArtist.length - 1)
                }
                return albumArtist
            }

        fun getAlbumArt(context: Context): String? {
            if (TextUtils.isEmpty(abumArt)) {
                val mUriAlbums = "content://media/external/audio/albums"
                val projection = arrayOf("album_art")
                var cur = context.contentResolver.query(Uri.parse(mUriAlbums + "/" + albumid), projection, null, null, null)
                if (cur.count > 0 && cur.columnCount > 0) {
                    cur.moveToNext()
                    abumArt = cur!!.getString(0)
                }
                cur.close()
                cur = null
            }
            return abumArt
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeLong(id)
            dest.writeString(title)
            dest.writeString(album)
            dest.writeString(artist)
            dest.writeString(url)
            dest.writeInt(duration)
            dest.writeLong(size)
        }

        companion object {

            val CREATOR: Parcelable.Creator<MusicInfo> = object : Parcelable.Creator<MusicLoader.MusicInfo> {

                override fun newArray(size: Int): Array<MusicInfo?> {
                    return arrayOfNulls(size)
                }

                override fun createFromParcel(source: Parcel): MusicInfo {
                    val musicInfo = MusicInfo()
                    musicInfo.id = source.readLong()
                    musicInfo.title = source.readString()
                    musicInfo.album = source.readString()
                    musicInfo.artist = source.readString()
                    musicInfo.url = source.readString()
                    musicInfo.duration = source.readInt()
                    musicInfo.size = source.readLong()
                    return musicInfo
                }
            }
        }
    }

    companion object {

        private val musicList = ArrayList<MusicInfo>()

        private var musicLoader: MusicLoader? = null

        private var contentResolver: ContentResolver? = null

        fun instance(pContentResolver: ContentResolver): MusicLoader {
            if (musicLoader == null) {
                contentResolver = pContentResolver
                musicLoader = MusicLoader()
            }
            return musicLoader as MusicLoader
        }
    }
}
