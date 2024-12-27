package com.sdase.mahlware.scanner.streaming.controller;

import com.sdase.mahlware.scanner.streaming.exception.DocumentProcessingException;
import com.sdase.mahlware.scanner.streaming.model.CheckResultEvent;
import com.sdase.mahlware.scanner.streaming.service.DocumentCheckService;
import com.sdase.mahlware.scanner.streaming.util.Constants;
import com.sdase.mahlware.scanner.streaming.validator.UrlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentCheckController.class)
class DocumentCheckControllerTest {

    @MockBean
    private DocumentCheckService documentCheckService;
    @MockBean
    private UrlValidator urlValidator;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCheckDocument_shouldReturnOk() throws Exception {
        // WHEN
        CheckResultEvent mockResult = new CheckResultEvent();
        mockResult.setName(Constants.IBAN_CHECKER_EVENT_NAME);
        mockResult.setState(CheckResultEvent.StateEnum.OK);
        mockResult.setDetails(Constants.NO_BLACKLISTED_IBAN_FOUND);

        when(urlValidator.validateUrl(anyString())).thenReturn(true);

        when(documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/Testdaten.pdf")).thenReturn(mockResult);

        // THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"http://localhost/resources/pdfs/Testdaten.pdf\", \"fileType\":\"application/pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("OK"))
                .andExpect(jsonPath("$.details").value(Constants.NO_BLACKLISTED_IBAN_FOUND));
    }

    @Test
    public void testCheckDocument_shouldReturnBadrequest_WhenBlacklistedIbansExist() throws Exception {
        // WHEN
        CheckResultEvent mockResult = new CheckResultEvent();
        mockResult.setName(Constants.IBAN_CHECKER_EVENT_NAME);
        mockResult.setState(CheckResultEvent.StateEnum.SUSPICIOUS);
        mockResult.setDetails("Blacklisted IBANs found: DE11234567891012345678");
        when(urlValidator.validateUrl(anyString())).thenReturn(true);
        when(documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/Testdaten.pdf")).thenReturn(mockResult);

        //THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"http://localhost/resources/pdfs/Testdaten.pdf\", \"fileType\":\"application/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value((mockResult.getName())))
                .andExpect(jsonPath("$.state").value("SUSPICIOUS"))
                .andExpect(jsonPath("$.details").value(mockResult.getDetails()));
    }

    @Test
    public void testCheckDocument_shouldReturnError_whenIOExceptionOccurs() throws Exception {
        // WHEN
        when(urlValidator.validateUrl(anyString())).thenReturn(true);
        when(documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/Testdaten.pdf"))
                .thenThrow(new DocumentProcessingException("Error processing document"));

        // THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"http://localhost/resources/pdfs/Testdaten.pdf\", \"fileType\":\"application/pdf\"}"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.state").value("ERROR"))
                .andExpect(jsonPath("$.details").value("Error processing document"));
    }

    @Test
    public void testCheckDocument_shouldReturnIGNORED_whenUrlIsNotValid() throws Exception {
        // WHEN
        when(urlValidator.validateUrl(anyString())).thenReturn(false);

        // THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"http://localhost/resources/pdfs/Testdaten.pdf\", \"fileType\":\"application/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.state").value("IGNORED"))
                .andExpect(jsonPath("$.details").value("Invalid or unsafe URL provided."));
    }

    @Test
    public void testCheckDocument_shouldReturnIGNORED_whenUrlIsEmpty() throws Exception {
        // WHEN
        when(urlValidator.validateUrl(anyString())).thenReturn(false);
        when(documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/Testdaten.pdf")).thenReturn(new CheckResultEvent());


        // THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"\", \"fileType\":\"application/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.state").value("IGNORED"))
                .andExpect(jsonPath("$.details").value(
                        allOf(
                                containsString("url: URL must start with http or https."),
                                containsString("url: URL must not be empty.")
                        )
                ));
    }

    @Test
    public void testCheckDocument_shouldReturnIGNORED_whenFileTypeIsEmpty() throws Exception {
        // WHEN
        when(urlValidator.validateUrl(anyString())).thenReturn(false);
        when(documentCheckService.checkDocumentForBlacklistedIban("http://localhost/resources/pdfs/Testdaten.pdf")).thenReturn(new CheckResultEvent());


        // THEN
        mockMvc.perform(post("/check-document")
                        .contentType("application/json")
                        .content("{\"url\":\"http://localhost/resources/pdfs/Testdaten.pdf\", \"fileType\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.state").value("IGNORED"))
                .andExpect(jsonPath("$.details").value("fileType: FileType must not be empty.; "));
    }
}