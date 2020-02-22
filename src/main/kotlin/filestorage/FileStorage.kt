package filestorage

/**
 * Interface of file storage for vendor lock-in avoiding.
 */
interface FileStorage {
    fun getFiles(directoryName: String): List<File>
}