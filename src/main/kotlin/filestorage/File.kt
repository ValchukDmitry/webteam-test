package filestorage

import java.net.URL
import java.util.*

data class File(
    val name: String,
    val byteSize: Long,
    val lastModifiedDate: Date,
    val isFolder: Boolean,
    val downloadUrl: URL
)