```markdown
# IBAN Checker Service

This service is designed to check documents (PDF format) for blacklisted IBANs. It provides a REST API that allows users to submit documents for verification. The service validates URLs, downloads documents, extracts IBANs, and checks whether they are blacklisted.

## Features

- **URL Validation**: Verifies that the provided URL starts with `http` or `https` and belongs to a list of allowed domains.
- **PDF Document Handling**: Downloads and opens PDF documents, then extracts IBANs from the document text.
- **Blacklisted IBAN Check**: Checks whether any extracted IBANs match a list of blacklisted IBANs.
- **Error Handling**: Handles invalid URLs, network issues, and other errors gracefully, providing appropriate error messages.

## Technology Stack

- **Java**: Version 21
- **Spring Boot**: Version 3.4.1
- **Apache PDFBox**: Version 2.0.27
- **JUnit**: Version 5.7.2

## Endpoint Overview

### `/check-document` - Check Document for Blacklisted IBANs

**POST Request**

- **Request Body** (JSON):

```json
{
  "url": "string",
  "fileType": "string"
}
```

- **Response** (JSON):

#### 1. Valid URL, No Blacklisted IBANs

```json
{
  "state": "OK",
  "details": "No blacklisted IBAN found",
  "name": "IBAN-Checker"
}
```

#### 2. Valid URL, Blacklisted IBAN Found

```json
{
  "state": "SUSPICIOUS",
  "details": "Blacklisted IBAN found: [DE89370400440532013000]",
  "name": "IBAN-Checker"
}
```

#### 3. Invalid URL

```json
{
  "state": "IGNORED",
  "details": "Invalid or unsafe URL provided.",
  "name": "IBAN-Checker"
}
```

## Service Workflow

1. **Request**: The client sends a POST request to the `/check-document` endpoint with the URL of the document to check and the file type (e.g., `application/pdf`).
2. **URL Validation**: The service first validates the URL. If the URL is invalid or unsafe, it responds with an error.
3. **Document Download**: If the URL is valid, the service attempts to download the document.
4. **IBAN Extraction**: After successfully downloading the document, the service extracts any IBANs from the text.
5. **Blacklisted IBAN Check**: The extracted IBANs are compared against a list of blacklisted IBANs.
6. **Response**: The service responds with the result, indicating whether any blacklisted IBANs were found, and provides additional details.

## Setup and Configuration

### Dependencies

- Spring Boot
- Apache PDFBox
- Jackson for JSON serialization
- JUnit for testing

### Configuration

- **Allowed Domains**: The service supports URL validation for allowed domains. The allowed domains can be configured in `application.properties` or via environment variables.

```properties
allowed.domains=http://localhost:8080
iban.blacklist=DE89370400440532013000,GB33BUKB20201555555555,DE15300606010505780780
```

### Environment Variables

- `allowed.domains`: Comma-separated list of allowed domains.
- `iban.blacklist`: Comma-separated list of blacklisted IBANs.

## Example Requests and Responses

### Request (Valid URL, No Blacklisted IBANs)

**Request**:

```json
{
  "url": "http://example.com/document_without_blacklisted_iban.pdf",
  "fileType": "application/pdf"
}
```

**Response**:

```json
{
  "state": "OK",
  "details": "No blacklisted IBAN found",
  "name": "IBAN-Checker"
}
```

### Request (Valid URL, Blacklisted IBAN Found)

**Request**:

```json
{
  "url": "http://example.com/document_with_blacklisted_iban.pdf",
  "fileType": "application/pdf"
}
```

**Response**:

```json
{
  "state": "SUSPICIOUS",
  "details": "Blacklisted IBAN found: [DE89370400440532013000]",
  "name": "IBAN-Checker"
}
```

### Request (Invalid URL)

**Request**:

```json
{
  "url": "invalid-url",
  "fileType": "application/pdf"
}
```

**Response**:

```json
{
  "state": "IGNORED",
  "details": "Invalid or unsafe URL provided.",
  "name": "IBAN-Checker"
}
```

## Testing

The project includes unit tests to ensure the correct operation of the service.

### Example Test for the Controller

```java
@Test
void testCheckDocument_InvalidUrl() throws Exception {
    mockMvc.perform(post("/check-document")
            .contentType("application/json")
            .content("{\"url\":\"invalid-url\", \"fileType\":\"application/pdf\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.state").value("IGNORED"))
            .andExpect(jsonPath("$.details").value("Invalid or unsafe URL provided."));
}
```

### Example Test for the Service

```java
@Test
void testCheckDocumentForBlacklistedIban_withBlacklistedIban() {
    String documentUrl = "http://example.com/document_with_blacklisted_iban.pdf";
    
    CheckResultEvent result = documentCheckService.checkDocumentForBlacklistedIban(documentUrl);
    
    assertEquals(CheckResultEvent.StateEnum.SUSPICIOUS, result.getState());
    assertTrue(result.getDetails().contains("Blacklisted IBAN found"));
}
