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

class S3FileSequenceGenerator(
    private val bucketName: String,
    region: String,
    private val linkExpirationTime: Long = 1000 * 60 * 60
) : FileSequenceGenerator{

    companion object {
        private val logger = LogManager.getLogger(FileStorage::class)
        private val DOWNLOADABLE_STORAGES = listOf("STANDARD")
    }

    private val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .enablePathStyleAccess()
        .build()

    override fun getFiles(directoryName: String): Sequence<File> {
        return generateSequence(
            try {
                s3Client.listObjects(bucketName, directoryName)
            } catch (e: SdkClientException) {
                logger.error("S3 client couldn`t get any response", e)
                throw e
            } catch (e: AmazonServiceException) {
                logger.error("S3 was not able to process client`s request", e)
                throw e
            }
        ) {
            if(!it.isTruncated) {
                null
            } else {
                try {
                    s3Client.listNextBatchOfObjects(it)
                } catch (e: SdkClientException) {
                    logger.error("S3 client couldn`t get any response", e)
                    throw e
                } catch (e: AmazonServiceException) {
                    logger.error("S3 was not able to process client`s request", e)
                    throw e
                }
            }
        }.flatMap {
            it.objectSummaries.asSequence().map {
                File(
                    it.key.removePrefix(directoryName),
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
        }.filter { !it.name.isBlank() }
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

    private fun isDownloadable(s3Object: S3ObjectSummary): Boolean {
        return DOWNLOADABLE_STORAGES.contains(s3Object.storageClass)
    }

}