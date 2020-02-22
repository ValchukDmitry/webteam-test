package faas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import filestorage.FileStorage
import filestorage.S3FileStorage

/**
 * AWS Lambda Handler which returns list of S3 files
 * It takes bucketName and region of S3 storage and linkExpirationTime
 */
class S3FilesListLambdaHandler : RequestHandler<HandlerInput, HandlerOutput> {
    private companion object {
        val bucketName = System.getenv("bucketName")
        val region = System.getenv("region")
        val linkExpirationTime = System.getenv("linkExpirationTime")?.toLong() ?: 60 * 60 * 1000
        val fileStorage: FileStorage = S3FileStorage(bucketName, region, linkExpirationTime)
    }

    override fun handleRequest(input: HandlerInput?, context: Context?): HandlerOutput {
        val resultFiles = fileStorage.getFiles(input?.folder ?: "")

        return HandlerOutput(
            resultFiles.sortedByDescending { it.isDirectory }
                .drop(input?.offset ?: 0)
                .take(input?.count ?: 0)
                .map {
                    FileOutput(
                        it.name,
                        it.lastModifiedDate.toString(),
                        it.byteSize,
                        it.isDownloadable,
                        it.downloadUrl?.toExternalForm(),
                        it.isDirectory
                    )
                },
            resultFiles.size
        )
    }
}