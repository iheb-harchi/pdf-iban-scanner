package com.sdase.mahlware.scanner.streaming.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class UrlValidator {

    @Value("${allowed.domains}")
    private String allowedDomains;

    public boolean validateUrl(String urlStr) {
        try {
            new URL(urlStr);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            URL url = new URL(urlStr);
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            URL url = new URL(urlStr);
            String host = url.getHost();
            return allowedDomains.contains(host);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public void setAllowedDomains(String allowedDomains) {
        this.allowedDomains = allowedDomains;
    }
}
