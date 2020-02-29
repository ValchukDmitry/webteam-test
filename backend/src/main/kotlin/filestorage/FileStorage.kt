package filestorage

import org.apache.logging.log4j.LogManager

data class FilesList(val files: List<File>, val totalSize: Int)

class FileStorage(private val fileSequence: FileSequenceGenerator, private val delimiter: String = "/") {
    companion object {
        private val logger = LogManager.getLogger(FileStorage::class)
    }

    fun getFiles(directoryName: String, offset: Int = 0, limit: Int = 1): FilesList {
        val directoryNameWithDelimiter = if (directoryName.endsWith(delimiter) || directoryName.isBlank()) {
            directoryName
        } else {
            "$directoryName$delimiter"
        }

        // TODO: remove it
        val bufFiles = fileSequence.getFiles(directoryNameWithDelimiter).toList()
        logger.info(bufFiles.size)

        val filesAtDirectory = fileSequence
            .getFiles(directoryNameWithDelimiter)
            .map(::formDirectoryFromFile)
            .filter(::filterFileInInternalDirectories)
            .groupBy { it.name }
            .map { directoryFile(it) }

        logger.info("Found ${filesAtDirectory.size} files at directory $directoryName")

        val totalSize = filesAtDirectory.size

        val resultFiles = filesAtDirectory
            .sortedByDescending { it.lastModifiedDate }
            .sortedByDescending { it.isDirectory }
            .drop(offset)
            .take(limit)


        return FilesList(resultFiles, totalSize)
    }

    private fun directoryFile(files: Map.Entry<String, List<File>>): File {
        return File(
            files.key,
            files.value.map { it.byteSize }.sum(),
            files.value.map { it.lastModifiedDate }.max()!!,
            files.value.first().isDirectory,
            !files.value.first().isDirectory && files.value.first().isDownloadable,
            files.value.first().downloadUrl
        )
    }

    private fun formDirectoryFromFile(file: File): File {
        if (file.name.contains(delimiter)) {
            return File(
                file.name.substringBefore(delimiter),
                file.byteSize,
                file.lastModifiedDate,
                true,
                false,
                file.downloadUrl
            )
        }
        return file
    }

    private fun filterFileInInternalDirectories(file: File): Boolean {
        return !file.name.contains(delimiter)
    }
}