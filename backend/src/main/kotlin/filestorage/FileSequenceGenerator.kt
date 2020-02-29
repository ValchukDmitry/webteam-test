package filestorage


/**
 * Interface to avoid vendor lock-in with AWS S3
 */
interface FileSequenceGenerator {
    fun getFiles(directoryName: String = ""): Sequence<File>
}