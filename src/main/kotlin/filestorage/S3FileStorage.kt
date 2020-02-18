package filestorage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectListing

class S3FileStorage(val bucketName: String, val region: String) : FileStorage {
    override fun getFiles(folderName: String): List<File> {
        val folderNameWithDelimiter = if (folderName.endsWith('/') || folderName.isBlank()) {
            folderName
        } else {
            "$folderName/"
        }
        val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()
        val objectListing: ObjectListing = s3Client.listObjects(bucketName)
        return objectListing.objectSummaries
            .filter { it.key.startsWith(folderNameWithDelimiter) }
            .map {
                File(
                    it.key.removePrefix(folderNameWithDelimiter),
                    it.size,
                    it.lastModified,
                    s3Client.getUrl(bucketName, it.key)
                )
            }
            .filter(::filterFileAtAnotherFolders)
    }

    private fun filterFileAtAnotherFolders(file: File): Boolean {
        val delimiter = '/'
        return !file.name.contains(delimiter)
    }
}