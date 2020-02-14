package filestorage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class S3FileStorageTest {
    private val testRegion = "testRegion"
    private val testBucketName = "testBucketName"
    private val clientBuilderMock = mockk<AmazonS3ClientBuilder>()
    private val amazonS3ClientMock = mockk<AmazonS3>()
    private val objectListingMock = mockk<ObjectListing>()

    @Before
    fun configureMocks() {
        mockkStatic(AmazonS3ClientBuilder::class)
        every { AmazonS3ClientBuilder.standard() } returns clientBuilderMock
        every { clientBuilderMock.withRegion(any<String>()) } returns clientBuilderMock
        every { clientBuilderMock.build() } returns amazonS3ClientMock
        every { amazonS3ClientMock.listObjects(any<String>()) } returns objectListingMock
        every { objectListingMock.objectSummaries } returns emptyList()
    }

    @Test
    fun s3ClientBuiltWithRegionFromArgs() {
        S3FileStorage(testBucketName, testRegion).getFiles()
        verify { clientBuilderMock.withRegion(testRegion) }
    }

    @Test
    fun s3ClientFilesGetsFromBucketFromArgs() {
        S3FileStorage(testBucketName, testRegion).getFiles()
        verify { amazonS3ClientMock.listObjects(testBucketName) }
    }

    @Test
    fun s3ClientReturnsFilesFromS3Client() {
        fun ObjectSummaryMockk(name: String, size: Long, date: Date): S3ObjectSummary {
            val resultMock = mockk<S3ObjectSummary>(name)
            every { resultMock.key } returns name
            every { resultMock.size } returns size
            every { resultMock.lastModified } returns date
            return resultMock
        }
        val mockedFiles = listOf<S3ObjectSummary>(
            ObjectSummaryMockk("file1", 12345, Date()),
            ObjectSummaryMockk("file2", 54321, Date()),
            ObjectSummaryMockk("file3", 123, Date())
        )
        every { objectListingMock.objectSummaries } returns mockedFiles
        val resultFiles = S3FileStorage(testBucketName, testRegion).getFiles()
        assertEquals(mockedFiles.size, resultFiles.size)
        mockedFiles.zip(resultFiles).forEach {
            val (mock, result) = it
            assertEquals(mock.key, result.name)
            assertEquals(mock.size, result.byteSize)
            assertEquals(mock.lastModified, result.lastModifiedDate)
        }

    }
}