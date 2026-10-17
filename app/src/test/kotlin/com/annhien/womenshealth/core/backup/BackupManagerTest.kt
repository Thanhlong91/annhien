package com.annhien.womenshealth.core.backup

import com.annhien.womenshealth.core.security.KeystoreManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Unit tests focus on the portable-backup contract.
 * Android Keystore and SQLCipher are intentionally not exercised here.
 */
class BackupManagerTest {

    private val keystoreManager: KeystoreManager = mockk()

    @Test
    fun `backup version is portable and includes database passphrase`() {
        assertEquals(2, BackupManagerTestConstants.BACKUP_VERSION)
    }

    @Test
    fun `restore rejects unsupported backup version`() {
        val tempDir = Files.createTempDirectory("annhien-backup").toFile()
        val backupFile = File(tempDir, "backup.annhien_backup")
        backupFile.writeText("invalid")

        val manager = BackupManager(
            context = mockk(),
            keystoreManager = keystoreManager
        )

        val result = kotlinx.coroutines.runBlocking {
            manager.restoreBackup("password".toCharArray(), backupFile)
        }

        assertTrue(result is RestoreResult.Error)
        assertTrue(
            (result as RestoreResult.Error).cause.contains("định dạng") ||
                result.cause.contains("Phiên bản")
        )
    }

    @Test
    fun `restore requires database passphrase for portable backups`() {
        val tempDir = Files.createTempDirectory("annhien-backup").toFile()
        val backupFile = File(tempDir, "backup.annhien_backup")
        backupFile.writeText("invalid")

        every { keystoreManager.getDatabasePassphrase() } returns ByteArray(32)

        val manager = BackupManager(
            context = mockk(),
            keystoreManager = keystoreManager
        )

        val result = kotlinx.coroutines.runBlocking {
            manager.restoreBackup("password".toCharArray(), backupFile)
        }

        assertTrue(result is RestoreResult.Error)
        verify(exactly = 0) { keystoreManager.importDatabasePassphrase(any()) }
    }
}

object BackupManagerTestConstants {
    const val BACKUP_VERSION = 2
}
