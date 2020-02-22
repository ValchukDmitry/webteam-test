package filestorage

import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import org.apache.logging.log4j.LogManager
import java.net.URL
import java.util.*


/**
 * @see FileStorage implementation for AWS S3
 */
class S3FileStorage(
    val bucketName: String,
    region: String,
    val linkExpirationTime: Long = 1000 * 60 * 60
) : FileStorage {
    private val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

    companion object {
        private val delimiter = '/'
        private val logger = LogManager.getLogger(S3FileStorage::class)
        private val DOWNLOADABLE_STORAGES = listOf("STANDARD")
    }

    override fun getFiles(directoryName: String): List<File> {
        val directoryNameWithDelimiter = if (directoryName.endsWith('/') || directoryName.isBlank()) {
            directoryName
        } else {
            "$directoryName$delimiter"
        }
        try {
            val objectListing: ObjectListing = s3Client.listObjects(bucketName)
            val allFiles = objectListing.objectSummaries

            val filesAtDirectory = allFiles
                .filter { it.key.startsWith(directoryNameWithDelimiter) }
                .map {
                    File(
                        it.key.removePrefix(directoryNameWithDelimiter),
                        it.size,
                        it.lastModified,
                        false,
                        isDownloadable(it),
                        if (isDownloadable(it)) {
                            getDownloadUrl(it.key)
                        } else {
                            null
                        }
                    )
                }
                .filter { !it.name.isBlank() }
                .map(::formDirectoryFromFile)
                .filter(::filterFileAtAnotherDirectories)
            logger.info("Found ${filesAtDirectory.size} files at directory $directoryName")

            return filesAtDirectory
                .groupBy { it.name }
                .map {
                    File(
                        it.key,
                        it.value.map { it.byteSize }.sum(),
                        it.value.map { it.lastModifiedDate }.max()!!,
                        it.value.first().isDirectory,
                        !it.value.first().isDirectory && it.value.first().isDownloadable,
                        it.value.first().downloadUrl
                    )
                }
        } catch (e: SdkClientException) {
            logger.error("S3 client couldn`t get any response", e)
            throw e
        } catch (e: AmazonServiceException) {
            logger.error("S3 was not able to process client`s request", e)
            throw e
        }
    }

    private fun isDownloadable(s3Object: S3ObjectSummary): Boolean {
        return DOWNLOADABLE_STORAGES.contains(s3Object.storageClass)
    }

    private fun getDownloadUrl(fileName: String): URL {
        val expiration = Date()
        expiration.time += linkExpirationTime
        val generatePresignedUrlRequest =
            GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration)

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest)
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

    private fun filterFileAtAnotherDirectories(file: File): Boolean {
        return !file.name.contains(delimiter)
    }
}