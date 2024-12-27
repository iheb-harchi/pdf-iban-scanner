package com.sdase.mahlware.scanner.streaming.service;

import com.sdase.mahlware.scanner.streaming.document.DocumentDownloader;
import com.sdase.mahlware.scanner.streaming.document.PdfDocumentHandler;
import com.sdase.mahlware.scanner.streaming.model.CheckResultEvent;
import com.sdase.mahlware.scanner.streaming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCheckService.class);

    private final PdfDocumentHandler pdfDocumentHandler;
    private final DocumentDownloader documentDownloader;

    @Value("${iban.blacklist}")
    private String blacklistedIbans;

    public DocumentCheckService(PdfDocumentHandler pdfDocumentHandler, DocumentDownloader documentDownloader) {
        this.pdfDocumentHandler = pdfDocumentHandler;
        this.documentDownloader = documentDownloader;

    }

    public CheckResultEvent checkDocumentForBlacklistedIban(String documentUrl) {
        CheckResultEvent event = new CheckResultEvent();
        event.setName(Constants.IBAN_CHECKER_EVENT_NAME);
        try {
            LOGGER.info("Processing document from URL: {}", documentUrl);

            try (InputStream inputStream = this.documentDownloader.downloadDocument(documentUrl)) {
                System.err.println(inputStream);

                this.pdfDocumentHandler.open(inputStream);
                System.err.println(inputStream);
                Set<String> extractedIbans = this.pdfDocumentHandler.extractIbans();
                Set<String> foundBlacklistedIbans = extractedIbans.stream()
                        .filter(blacklistedIbans::contains)
                        .collect(Collectors.toSet());
                if (!foundBlacklistedIbans.isEmpty()) {
                    event.setState(CheckResultEvent.StateEnum.SUSPICIOUS);
                    event.setDetails(Constants.BLACKLISTED_IBAN_FOUND + String.join(", ", foundBlacklistedIbans));
                    LOGGER.warn("Blacklisted IBAN found in document.");
                } else {
                    event.setState(CheckResultEvent.StateEnum.OK);
                    event.setDetails(Constants.NO_BLACKLISTED_IBAN_FOUND);
                    event.setName(Constants.IBAN_CHECKER_EVENT_NAME);
                    LOGGER.info("No blacklisted IBAN found in document.");
                }
            }

        } catch (IOException e) {
            LOGGER.error("Error processing document", e);
            event.setState(CheckResultEvent.StateEnum.ERROR);
            event.setDetails("Error processing document: " + e.getMessage());
            return event;
        } catch (Exception e) {
            LOGGER.error("Unexpected error processing document", e);
            event.setState(CheckResultEvent.StateEnum.ERROR);
            event.setDetails("Unexpected error processing document: " + e.getMessage());
            return event;
        } finally {
            this.pdfDocumentHandler.close();
        }
        return event;
    }

    public void setBlacklistedIbans(String blacklistedIbans) {
        this.blacklistedIbans = blacklistedIbans;
    }
}
