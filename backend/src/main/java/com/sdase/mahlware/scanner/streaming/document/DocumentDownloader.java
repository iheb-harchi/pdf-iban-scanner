package com.sdase.mahlware.scanner.streaming.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class DocumentDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDownloader.class);

    public InputStream downloadDocument(String documentUrl) throws IOException {
        HttpURLConnection connection = null;
        try {
            LOGGER.info("Downloading document from URL: {}", documentUrl);

            URL url = new URL(documentUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Failed to download document. HTTP Status: {}", responseCode);
                throw new IOException("Failed to download document. HTTP Status: " + responseCode);
            }

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
        } catch (IOException e) {
            LOGGER.error("Error downloading document from URL: {}", documentUrl, e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
