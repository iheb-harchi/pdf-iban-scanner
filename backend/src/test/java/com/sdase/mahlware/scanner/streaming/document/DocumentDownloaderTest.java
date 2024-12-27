package com.sdase.mahlware.scanner.streaming.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

class DocumentDownloaderTest {

    @Mock
    private HttpURLConnection mockConnection;

    @InjectMocks
    private DocumentDownloader documentDownloader;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND); // 404
        when(mockConnection.getInputStream()).thenThrow(new IOException("Failed to download document. HTTP Status: 404"));

        when(mockConnection.getURL()).thenReturn(new java.net.URL("http://example.com/test.pdf"));
    }

    @Test
    void testDownloadDocument_404() {
        String url = "http://example.com/test.pdf";

        try {
            InputStream result = documentDownloader.downloadDocument(url);
            fail("Expected IOException due to HTTP 404 error, but none was thrown");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Failed to download document. HTTP Status: 404"));
        }
    }
}
