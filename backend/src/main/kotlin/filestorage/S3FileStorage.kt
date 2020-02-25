package filestorage

import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.S3ObjectSummary
import org.apache.logging.log4j.LogManager
import java.net.URL
import java.util.*


/**
 * @see FileStorage implementation for AWS S3
 */
class S3FileStorage(
    private val bucketName: String,
    region: String,
    private val linkExpirationTime: Long = 1000 * 60 * 60
) : FileStorage {
    private val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

    companion object {
        private const val delimiter = '/'
        private val logger = LogManager.getLogger(S3FileStorage::class)
        private val DOWNLOADABLE_STORAGES = listOf("STANDARD")
    }

    override fun getFiles(directoryName: String): List<File> {
        // make directory name unified
        val directoryNameWithDelimiter = if (directoryName.endsWith('/') || directoryName.isBlank()) {
            directoryName
        } else {
            "$directoryName$delimiter"
        }

        val objectListing = try {
            s3Client.listObjects(bucketName)
        } catch (e: SdkClientException) {
            logger.error("S3 client couldn`t get any response", e)
            throw e
        } catch (e: AmazonServiceException) {
            logger.error("S3 was not able to process client`s request", e)
            throw e
        }

        val allFiles = objectListing.objectSummaries
        val filesAtDirectory = allFiles
            .asSequence()
            .filter { fileInDirectory(directoryNameWithDelimiter, it) }
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
            .filter(::filterFileInInternalDirectories)
            .groupBy { it.name }
            .map { directoryFile(it) }
            .toList()

        logger.info("Found ${filesAtDirectory.size} files at directory $directoryName")

        return filesAtDirectory
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

    private fun fileInDirectory(directoryNameWithDelimiter: String, it: S3ObjectSummary): Boolean {
        return it.key.startsWith(directoryNameWithDelimiter)
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
        try {
            return s3Client.generatePresignedUrl(generatePresignedUrlRequest)
        } catch (e: SdkClientException) {
            logger.error("Failed to generate presigned URL for file ${fileName}", e)
            throw e
        }

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