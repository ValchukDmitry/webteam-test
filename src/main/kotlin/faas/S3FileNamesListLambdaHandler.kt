package faas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import filestorage.FileStorage
import filestorage.S3FileStorage

class S3FileNamesListLambdaHandler : RequestHandler<HandlerInput, HandlerOutput> {
    private companion object {
        val bucketName = System.getenv("bucketName")
        val region = System.getenv("region")
        val fileStorage: FileStorage = S3FileStorage(bucketName, region)
    }

    override fun handleRequest(input: HandlerInput?, context: Context?): HandlerOutput {
        val resultFiles = fileStorage.getFiles(input?.folder ?: "")

        return HandlerOutput(
            resultFiles.sortedByDescending { it.isFolder }
                .drop(input?.offset ?: 0)
                .take(input?.count ?: 0)
                .map {
                    FileOutput(
                        it.name,
                        it.lastModifiedDate.toString(),
                        it.byteSize,
                        it.isDownloadable,
                        it.downloadUrl?.toExternalForm(),
                        it.isFolder
                    )
                },
            resultFiles.size
        )
    }
}