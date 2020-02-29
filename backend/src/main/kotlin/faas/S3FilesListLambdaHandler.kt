package faas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import filestorage.S3FileSequenceGenerator
import filestorage.FileStorage

/**
 * AWS Lambda Handler which returns list of S3 files
 * It takes bucketName and region of S3 storage and linkExpirationTime
 */
class S3FilesListLambdaHandler : RequestHandler<HandlerInput, HandlerOutput> {
    private companion object {
        val bucketName = System.getenv("bucketName")
        val region = System.getenv("region")
        val linkExpirationTime = System.getenv("linkExpirationTime")?.toLong() ?: 60 * 60 * 1000
        val fileStorage: FileStorage = FileStorage(
            S3FileSequenceGenerator(bucketName, region, linkExpirationTime)
        )
    }

    override fun handleRequest(input: HandlerInput?, context: Context?): HandlerOutput {
        val resultFiles = fileStorage.getFiles(input?.folder ?: "",
            input?.offset ?: 0,
            input?.count ?: 1)

        return HandlerOutput(
            resultFiles.files.sortedByDescending { it.isDirectory }
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
            resultFiles.totalSize
        )
    }
}