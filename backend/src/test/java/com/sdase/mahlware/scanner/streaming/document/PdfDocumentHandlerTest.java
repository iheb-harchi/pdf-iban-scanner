package com.sdase.mahlware.scanner.streaming.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PdfDocumentHandlerTest {

    @Mock
    private PDDocument mockDocument;

    private PdfDocumentHandler pdfDocumentHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pdfDocumentHandler = new PdfDocumentHandler();
    }

    @Test
    void testOpenAndClose() throws IOException, URISyntaxException {

        byte[] pdfBytes = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("pdfs/Testdaten.pdf").toURI()));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);


        PdfDocumentHandler pdfDocumentHandler = new PdfDocumentHandler();
        pdfDocumentHandler.open(inputStream);
        pdfDocumentHandler.close();
    }

    @Test
    void testExtractIbans_noIbans() throws IOException, URISyntaxException {

        byte[] pdfBytes = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("pdfs/test_document_withoutI_iban.pdf").toURI()));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);


        pdfDocumentHandler.open(inputStream);


        Set<String> ibans = pdfDocumentHandler.extractIbans();


        assertTrue(ibans.isEmpty());


        pdfDocumentHandler.close();
    }

    @Test
    void testExtractIbans_withIbans() throws IOException, URISyntaxException {

        byte[] pdfBytes = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("pdfs/pdf_with_5_IBANs.pdf").toURI()));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);


        pdfDocumentHandler.open(inputStream);


        Set<String> ibans = pdfDocumentHandler.extractIbans();

        Set<String> expectedIbans = Set.of(
                "DE44123456789012345678",
                "DE39987654321002345678",
                "DE47563214358765432190",
                "DE21874523458765567891",
                "DE95234567890123456712"
        );

        assertEquals(5, ibans.size());
        assertTrue(ibans.containsAll(expectedIbans), "Nicht alle erwarteten IBANs wurden extrahiert.");

        pdfDocumentHandler.close();
    }

    @Test
    void testExtractIbans_documentNotLoaded() {

        assertThrows(IOException.class, () -> {
            pdfDocumentHandler.extractIbans();
        });
    }

    @Test
    void testCleanText_removesExcessSpaces() {
        String dirtyText = "This   is  a text  with  multiple    spaces.";

        String cleanedText = pdfDocumentHandler.cleanText(dirtyText);

        assertEquals("This is a text with multiple spaces.", cleanedText);
    }

    @Test
    void testCleanText_removesLineBreaks() {
        String dirtyText = "This is\na text\nwith line\nbreaks.";

        String cleanedText = pdfDocumentHandler.cleanText(dirtyText);

        assertEquals("This is a text with line breaks.", cleanedText);
    }
}
