package filestorage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectListing

class S3FileStorage(val bucketName: String, val region: String) : FileStorage {
    override fun getFiles(): List<File> {
        val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()
        val objectListing: ObjectListing = s3Client.listObjects(bucketName)
        return objectListing.objectSummaries.map { File(it.key, it.size, it.lastModified) }
    }
}