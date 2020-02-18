package filestorage

import java.net.URL
import java.util.Date

data class File(val name: String, val byteSize: Long, val lastModifiedDate: Date, val downloadUrl: URL)