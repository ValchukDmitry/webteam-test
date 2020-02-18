package faas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import filestorage.FileStorage
import filestorage.S3FileStorage

class S3FileNamesListLambdaHandler : RequestHandler<HandlerInput, HandlerOutput> {
    override fun handleRequest(input: HandlerInput?, context: Context?): HandlerOutput {
        val bucketName = System.getenv("bucketName")
        val region = System.getenv("region")
        val fileStorage: FileStorage = S3FileStorage(bucketName, region)
        val resultFiles = fileStorage.getFiles(input?.folder ?: "")
        return HandlerOutput(
            resultFiles
                .sortedByDescending { it.lastModifiedDate }
                .drop(input?.offset ?: 0)
                .take(input?.count ?: 0)
                .map {
                    OutputElement(it.name, it.lastModifiedDate.toString(), it.byteSize, it.downloadUrl.toExternalForm())
                },
            resultFiles.size
        )
    }
}