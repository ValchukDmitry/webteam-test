package filestorage

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class FileStorageTest {
    val fileSequence = sequenceOf(
        File(
            "file1",
            1,
            Date(),
            false,
            false,
            mockk()
        ),
        File(
            "file2",
            2,
            Date(),
            false,
            false,
            mockk()
        ),
        File(
            "file3",
            3,
            Date(),
            false,
            false,
            mockk()
        ),
        File(
            "file4",
            4,
            Date(),
            false,
            false,
            mockk()
        ),
        File(
            "folder1/file5",
            5,
            Date(),
            false,
            false,
            mockk()
        ),
        File(
            "folder1/folder2/file6",
            6,
            Date(),
            false,
            false,
            mockk()
        )

    )

    var fileSequenceGenerator: FileSequenceGenerator = mockk()

    @Before
    fun configureMocks() {
        fileSequenceGenerator = mockk()
        every { fileSequenceGenerator.getFiles(any()) } returns fileSequence
    }

    @Test
    fun fileStorageReturnsValidFilesFromRoot() {
        val fileStorage = FileStorage(fileSequenceGenerator)
        val files = fileStorage.getFiles("", limit=10)
        val expectedFileNames = listOf("folder1", "file1", "file2", "file3", "file4")
        assertEquals(expectedFileNames.size, files.totalSize)
        expectedFileNames.zip(files.files).forEach {
            assertEquals(it.first, it.second.name)
        }
    }

    @Test
    fun fileStorageReturnsValidFilesFromInnerFolder() {
        val fileStorage = FileStorage(fileSequenceGenerator)
        val directoryName = "folder1/"
        fileStorage.getFiles(directoryName, limit=10)
        verify { fileSequenceGenerator.getFiles(eq(directoryName)) }
    }

    @Test
    fun fileStorageMarksFoldersValid() {
        val fileStorage = FileStorage(fileSequenceGenerator)
        val directoryName = ""
        val files = fileStorage.getFiles(directoryName, limit=10)
        assertEquals(true, files.files.get(0).isDirectory)
    }
}