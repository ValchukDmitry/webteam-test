package filestorage

import java.net.URL
import java.util.*

data class File(
    val name: String,
    val byteSize: Long,
    val lastModifiedDate: Date,
    val isDirectory: Boolean,
    val isDownloadable: Boolean,
    val downloadUrl: URL?
)