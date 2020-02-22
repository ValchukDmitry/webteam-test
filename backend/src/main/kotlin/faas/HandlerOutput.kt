package faas

data class FileOutput(
    val name: String,
    val modifiedDate: String,
    val size: Long,
    val isDownloadable: Boolean,
    val downloadLink: String? = null,
    val isDirectory: Boolean = false
)

data class HandlerOutput(val files: List<FileOutput>, val count: Int)