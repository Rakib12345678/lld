package com.rocky.shorturl.dto;

public class ShortenRequest {

//    @NotBlank(message = "URL must not be blank")
    private String url;

    public ShortenRequest() {}

    public ShortenRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}