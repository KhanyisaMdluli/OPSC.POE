package com.opsc.solowork_1

import com.opsc.solowork_1.model.Document
import org.junit.Test
import org.junit.Assert.*

class DocumentFormatTest {

    @Test
    fun testFormatFileSizeBytes() {
        // Test bytes
        assertEquals("500 B", Document.formatFileSize(500))
        assertEquals("0 B", Document.formatFileSize(0))
        assertEquals("1023 B", Document.formatFileSize(1023))
    }

    @Test
    fun testFormatFileSizeKilobytes() {
        // Test kilobytes
        assertEquals("1.0 KB", Document.formatFileSize(1024))
        assertEquals("1.5 KB", Document.formatFileSize(1536))
        assertEquals("1023.0 KB", Document.formatFileSize(1024 * 1023))
    }

    @Test
    fun testFormatFileSizeMegabytes() {
        // Test megabytes
        assertEquals("1.0 MB", Document.formatFileSize(1024 * 1024))
        assertEquals("2.5 MB", Document.formatFileSize(2621440)) // 2.5 * 1024 * 1024
        assertEquals("999.0 MB", Document.formatFileSize(1024 * 1024 * 999))
    }

    @Test
    fun testFormatFileSizeGigabytes() {
        // Test gigabytes
        assertEquals("1.0 GB", Document.formatFileSize(1024 * 1024 * 1024))
        assertEquals("1.5 GB", Document.formatFileSize(1610612736)) // 1.5 * 1024 * 1024 * 1024
    }

    @Test
    fun testFormatFileSizeEdgeCases() {
        // Test edge cases
        assertEquals("1.0 KB", Document.formatFileSize(1024)) // Exactly 1 KB
        assertEquals("1.0 MB", Document.formatFileSize(1048576)) // Exactly 1 MB
        assertEquals("1.0 GB", Document.formatFileSize(1073741824)) // Exactly 1 GB
    }
}