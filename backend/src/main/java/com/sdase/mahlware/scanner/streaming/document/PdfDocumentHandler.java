package com.sdase.mahlware.scanner.streaming.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfDocumentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfDocumentHandler.class);
    private static final String IBAN_PATTERN = "[A-Z]{2}[0-9]{2}[\\s]?[0-9]{4}[\\s]?[0-9]{4}[\\s]?[0-9]{4}[\\s]?[0-9]{4}[\\s]?[0-9]{2}";

    private PDDocument document;

    public void open(InputStream inputStream) throws IOException {
        try {
            LOGGER.info("Opening PDF document...");
            this.document = PDDocument.load(inputStream);
            LOGGER.info("PDF document opened successfully.");
        } catch (IOException e) {
            LOGGER.error("Error opening PDF document", e);
            throw e;
        }
    }

    public void close() {
        try {
            if (document != null) {
                document.close();
                LOGGER.info("PDF document closed successfully.");
            }
        } catch (IOException e) {
            LOGGER.error("Error closing PDF document", e);
        }
    }

    public Set<String> extractIbans() throws IOException {
        if (document == null) {
            LOGGER.error("Document is not initialized. Cannot extract IBANs.");
            throw new IOException("Document not loaded.");
        }

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        String cleanedText = cleanText(text);
        return extractIbansFromText(cleanedText);
    }

    public Set<String> extractIbansFromText(String text) {
        Pattern pattern = Pattern.compile(IBAN_PATTERN);

        Matcher matcher = pattern.matcher(text);
        Set<String> ibans = new HashSet<>();
        while (matcher.find()) {
            String iban = matcher.group().replaceAll("\\s", "");
            ibans.add(iban);
        }
        return ibans;
    }

    public String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s{2,}", " ");
    }

}
