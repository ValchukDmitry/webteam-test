package filestorage

interface FileStorage {
    fun getFiles(folderName: String): List<File>
}