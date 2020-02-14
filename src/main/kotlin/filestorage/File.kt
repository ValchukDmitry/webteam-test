package filestorage

import java.util.Date

data class File(val name: String, val byteSize: Long, val lastModifiedDate: Date)