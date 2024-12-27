package com.sdase.mahlware.scanner.streaming.controller;

import com.sdase.mahlware.scanner.streaming.model.CheckEvent;
import com.sdase.mahlware.scanner.streaming.model.CheckResultEvent;
import com.sdase.mahlware.scanner.streaming.service.DocumentCheckService;
import com.sdase.mahlware.scanner.streaming.util.Constants;
import com.sdase.mahlware.scanner.streaming.validator.UrlValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentCheckController {

    private final DocumentCheckService documentCheckService;
    private final UrlValidator urlValidator;

    public DocumentCheckController(DocumentCheckService documentCheckService, UrlValidator urlValidator) {
        this.documentCheckService = documentCheckService;
        this.urlValidator = urlValidator;
    }

    @PostMapping("/check-document")
    public ResponseEntity<CheckResultEvent> checkDocument(@Valid @RequestBody CheckEvent checkEvent) {
        String url = checkEvent.getUrl();
        if (!urlValidator.validateUrl(url)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CheckResultEvent(CheckResultEvent.StateEnum.IGNORED, Constants.IBAN_CHECKER_EVENT_NAME, "Invalid or unsafe URL provided."));
        }

        CheckResultEvent resultEvent = documentCheckService.checkDocumentForBlacklistedIban(url);

        if (resultEvent.getState() == CheckResultEvent.StateEnum.OK) {
            System.err.println(ResponseEntity.ok(resultEvent));
            return ResponseEntity.ok(resultEvent);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultEvent);
        }
    }
}
