package filestorage

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*

class S3FileStorageTest {
    private val testRegion = "testRegion"
    private val testBucketName = "testBucketName"
    private val testUrl = mockk<URL>()
    private val clientBuilderMock = mockk<AmazonS3ClientBuilder>()
    private val amazonS3ClientMock = mockk<AmazonS3>()
    private val objectListingMock = mockk<ObjectListing>()
    private val urlRequest = mockk<GeneratePresignedUrlRequest>()

    @Before
    fun configureMocks() {
        mockkStatic(AmazonS3ClientBuilder::class)
        every { AmazonS3ClientBuilder.standard() } returns clientBuilderMock
        every { clientBuilderMock.withRegion(any<String>()) } returns clientBuilderMock
        every { clientBuilderMock.enablePathStyleAccess() } returns clientBuilderMock
        every { clientBuilderMock.build() } returns amazonS3ClientMock
        every { amazonS3ClientMock.listObjects(any<String>()) } returns objectListingMock
        every { amazonS3ClientMock.getUrl(any<String>(), any<String>()) } returns testUrl
        every { amazonS3ClientMock.getUrl(any<String>(), any<String>()) } returns testUrl
        every { amazonS3ClientMock.generatePresignedUrl(urlRequest) } returns testUrl
        every { objectListingMock.objectSummaries } returns emptyList()
        mockkConstructor(GeneratePresignedUrlRequest::class)
        every { anyConstructed<GeneratePresignedUrlRequest>().withMethod(HttpMethod.GET) } returns urlRequest
        every { urlRequest.withExpiration(any()) } returns urlRequest
    }

    @Test
    fun s3ClientBuiltWithRegionFromArgs() {
        S3FileStorage(testBucketName, testRegion).getFiles("")
        verify { clientBuilderMock.withRegion(testRegion) }
    }

    @Test
    fun s3ClientFilesGetsFromBucketFromArgs() {
        S3FileStorage(testBucketName, testRegion).getFiles("")
        verify { amazonS3ClientMock.listObjects(testBucketName) }
    }

    @Test
    fun s3ClientReturnsFilesFromS3Client() {
        fun ObjectSummaryMockk(name: String, size: Long, date: Date): S3ObjectSummary {
            val resultMock = mockk<S3ObjectSummary>(name)
            every { resultMock.key } returns name
            every { resultMock.size } returns size
            every { resultMock.lastModified } returns date
            every { resultMock.storageClass } returns "STANDARD"
            return resultMock
        }

        val mockedFiles = listOf(
            ObjectSummaryMockk("file1", 12345, Date()),
            ObjectSummaryMockk("file2", 54321, Date()),
            ObjectSummaryMockk("file3", 123, Date()),
            ObjectSummaryMockk("folder/fileAtFolder", 123, Date())
        )

        val resultNames = listOf("file1", "file2", "file3", "folder")

        every { objectListingMock.objectSummaries } returns mockedFiles
        val resultFiles = S3FileStorage(testBucketName, testRegion).getFiles("")
        assertEquals(mockedFiles.size, resultFiles.size)
        mockedFiles.zip(resultFiles).forEach {
            val (mock, result) = it
            assertEquals(mock.size, result.byteSize)
            assertEquals(mock.lastModified, result.lastModifiedDate)
        }
        resultNames.zip(resultFiles).forEach {
            val (mockFileName, result) = it
            assertEquals(mockFileName, result.name)
        }

    }

    @Test
    fun s3ClientReturnsFilesFromS3ClientAtDirectory() {
        fun ObjectSummaryMockk(name: String, size: Long, date: Date): S3ObjectSummary {
            val resultMock = mockk<S3ObjectSummary>(name)
            every { resultMock.key } returns name
            every { resultMock.size } returns size
            every { resultMock.lastModified } returns date
            every { resultMock.storageClass } returns "STANDARD"
            return resultMock
        }

        val mockedFiles = listOf<S3ObjectSummary>(
            ObjectSummaryMockk("file1", 12345, Date()),
            ObjectSummaryMockk("file2", 54321, Date()),
            ObjectSummaryMockk("file3", 123, Date()),
            ObjectSummaryMockk("folder/fileAtFolder", 123, Date())
        )
        every { objectListingMock.objectSummaries } returns mockedFiles
        val resultFiles = S3FileStorage(testBucketName, testRegion).getFiles("folder")
        assertEquals(1, resultFiles.size)
        assertEquals("fileAtFolder", resultFiles[0].name)
    }
}