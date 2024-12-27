package com.sdase.mahlware.scanner.streaming.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CheckEvent {

    public static final String HEADER_REQUESTER_SERVICE = "requester_service";

    @NotNull(message = "URL must not be null.")
    @NotBlank(message = "URL must not be empty.")
    @Pattern(regexp = "^(http|https)://.*", message = "URL must start with http or https.")
    private String url;

    @NotNull(message = "FileType must not be null.")
    @NotBlank(message = "FileType must not be empty.")
    private String fileType;

    public String getUrl() {
        return url;
    }

    public CheckEvent setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public CheckEvent setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
