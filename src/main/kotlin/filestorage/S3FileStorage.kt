package filestorage

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectListing
import org.apache.logging.log4j.LogManager


class S3FileStorage(val bucketName: String, region: String) : FileStorage {
    private val s3Client: AmazonS3

    companion object {
        val logger = LogManager.getLogger(S3FileStorage::class)
        private val DOWNLOADABLE_STORAGES = listOf("STANDARD")
    }

    init {
        s3Client = AmazonS3ClientBuilder.standard().withRegion(region).build()
    }

    private val delimiter = '/'
    override fun getFiles(directoryName: String): List<File> {
        val directoryNameWithDelimiter = if (directoryName.endsWith('/') || directoryName.isBlank()) {
            directoryName
        } else {
            "$directoryName$delimiter"
        }
        try {
            val objectListing: ObjectListing = s3Client.listObjects(bucketName)
            val allFiles = objectListing.objectSummaries
            logger.info("Found ${allFiles.size} total files")
            val filesAtDirectory = allFiles
                .filter { it.key.startsWith(directoryNameWithDelimiter) }
            logger.info("Found ${filesAtDirectory.size} files at directory $directoryName")
            return filesAtDirectory
                .map {
                    File(
                        it.key.removePrefix(directoryNameWithDelimiter),
                        it.size,
                        it.lastModified,
                        false,
                        DOWNLOADABLE_STORAGES.contains(it.storageClass),
                        s3Client.getUrl(bucketName, it.key)
                    )
                }
                .map(::getFolder)
                .filter(::filterFileAtAnotherFolders)
                .sortedByDescending { it.name }
                .groupBy { it.name }
                .map {
                    File(
                        it.key,
                        it.value.map { it.byteSize }.sum(),
                        it.value.map { it.lastModifiedDate }.max()!!,
                        it.value.first().isFolder,
                        !it.value.first().isFolder && it.value.first().isDownloadable,
                        it.value.first().downloadUrl
                    )
                }
        } catch (e: SdkClientException) {
            logger.error("S3 client couldn`t get any response", e)
            throw e
        } catch (e: AmazonServiceException) {
            logger.error("S3 was not able to process client request", e)
            throw e
        }
    }

    private fun getFolder(file: File): File {
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

    private fun filterFileAtAnotherFolders(file: File): Boolean {
        return !file.name.contains(delimiter)
    }
}