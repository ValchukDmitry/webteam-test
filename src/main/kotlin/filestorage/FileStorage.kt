package filestorage

interface FileStorage {
    fun getFiles(): List<File>
}