package com.sdase.mahlware.scanner.streaming.service;

import com.sdase.mahlware.scanner.streaming.document.DocumentDownloader;
import com.sdase.mahlware.scanner.streaming.document.PdfDocumentHandler;
import com.sdase.mahlware.scanner.streaming.model.CheckResultEvent;
import com.sdase.mahlware.scanner.streaming.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentCheckServiceTest {

    private final String documentUrl = "http://localhost/resources/pdfs/test.pdf";
    @Mock
    private PdfDocumentHandler pdfDocumentHandler;
    @Mock
    private DocumentDownloader documentDownloader;
    @InjectMocks
    private DocumentCheckService documentCheckService;

    @BeforeEach
    void setUp() {
        documentCheckService.setBlacklistedIbans("DE89370400440532013000,GB33BUKB20201555555555,DE15300606010505780780");
    }

    @Test
    void stestCheckDocumentForBlacklistedIban_noBlacklistedIbans() throws Exception {

        doNothing().when(pdfDocumentHandler).open(any());

        Set<String> mockIbans = new HashSet<>();
        when(pdfDocumentHandler.extractIbans()).thenReturn(mockIbans);

        CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/test_document_withoutI_iban.pdf");

        assertNotNull(result, "Result should not be null");
        assertEquals(CheckResultEvent.StateEnum.OK, result.getState(), "Expected OK state");
        assertEquals(Constants.NO_BLACKLISTED_IBAN_FOUND, result.getDetails(), "Expected 'No Blacklisted IBAN Found' message");

        verify(pdfDocumentHandler, times(1)).extractIbans();
    }


    @Test
    void testCheckDocumentForBlacklistedIban_noBlacklistedIbans() throws Exception {
        doNothing().when(pdfDocumentHandler).open(any());
        Set<String> mockIbans = new HashSet<>();
        when(pdfDocumentHandler.extractIbans()).thenReturn(mockIbans);

        CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban(documentUrl);

        assertEquals(CheckResultEvent.StateEnum.OK, result.getState());
        assertEquals(Constants.NO_BLACKLISTED_IBAN_FOUND, result.getDetails());
        verify(pdfDocumentHandler, times(1)).extractIbans();
    }

    @Test
    void testCheckDocumentForBlacklistedIban_blacklistedIbansFound() throws Exception {
        doNothing().when(pdfDocumentHandler).open(any());
        Set<String> mockIbans = new HashSet<>();
        mockIbans.add("DE89370400440532013000");
        when(pdfDocumentHandler.extractIbans()).thenReturn(mockIbans);

        CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban(documentUrl);

        assertEquals(CheckResultEvent.StateEnum.SUSPICIOUS, result.getState());
        assertTrue(result.getDetails().contains("Blacklisted IBANs found"));
        verify(pdfDocumentHandler, times(1)).extractIbans();
    }

    @Test
    void testCheckDocumentForBlacklistedIban_shouldReturnError_whenIOExceptionOccurs() throws Exception {
        doNothing().when(pdfDocumentHandler).open(any());
        when(pdfDocumentHandler.extractIbans()).thenThrow(new IOException("Error processing document"));

        CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban(documentUrl);

        assertEquals(CheckResultEvent.StateEnum.ERROR, result.getState());
        assertEquals("Error processing document: Error processing document", result.getDetails());
        verify(pdfDocumentHandler, times(1)).extractIbans();
    }

    @Test
    void testCheckDocumentForBlacklistedIban_shouldReturnError_whenUnexpectedErrorOccurs() throws Exception {
        doNothing().when(pdfDocumentHandler).open(any());
        when(pdfDocumentHandler.extractIbans()).thenThrow(new RuntimeException("Unexpected error"));

        CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban(documentUrl);

        assertEquals(CheckResultEvent.StateEnum.ERROR, result.getState());
        assertEquals("Unexpected error processing document: Unexpected error", result.getDetails());
        verify(pdfDocumentHandler, times(1)).extractIbans();
    }

}
