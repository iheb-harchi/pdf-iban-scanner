package com.sdase.mahlware.scanner.streaming.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlValidatorTest {

    private UrlValidator urlValidator;

    @BeforeEach
    public void setup() {
        urlValidator = new UrlValidator();
        urlValidator.setAllowedDomains("example.com, trusted-domain.com, another-trusted.com");
    }

    @Test
    public void testValidHttpUrlWithAllowedDomain() {
        String testUrl = "http://example.com/test.pdf";

        boolean result = urlValidator.validateUrl(testUrl);

        assertTrue(result);
    }

    @Test
    public void testValidHttpsUrlWithAllowedDomain() {
        String testUrl = "https://trusted-domain.com/test.pdf";

        boolean result = urlValidator.validateUrl(testUrl);

        assertTrue(result);
    }

    @Test
    public void testInvalidUrlWithDisallowedDomain() {
        String testUrl = "http://unauthorized-domain.com/test.pdf";

        boolean result = urlValidator.validateUrl(testUrl);

        assertFalse(result);
    }

    @Test
    public void testInvalidUrlWithMalformedUrl() {
        String testUrl = "htp://malformed-url.com";

        boolean result = urlValidator.validateUrl(testUrl);

        assertFalse(result);
    }

    @Test
    public void testInvalidUrlWithMissingProtocol() {
        String testUrl = "ftp://example.com/test.pdf";

        boolean result = urlValidator.validateUrl(testUrl);

        assertFalse(result);
    }

    @Test
    public void testValidUrlWithNoProtocol() {
        String testUrl = "example.com/test.pdf";

        boolean result = urlValidator.validateUrl(testUrl);

        assertFalse(result);
    }
}
