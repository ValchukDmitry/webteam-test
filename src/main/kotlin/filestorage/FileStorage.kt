package filestorage

interface FileStorage {
    fun getFiles(directoryName: String): List<File>
}