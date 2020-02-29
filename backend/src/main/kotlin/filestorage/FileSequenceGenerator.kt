package filestorage

interface FileSequenceGenerator {
    fun getFiles(directoryName: String = ""): Sequence<File>
}