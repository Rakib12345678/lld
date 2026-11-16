package com.rocky.shorturl.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "url_mappings")
public class UrlMapping {

    @Id
    private String id; // Mongo generated id

    private long seq; // sequence number used to build short code
    private String shortCode; // base62 string
    private String longUrl;
    private Instant createdAt;
    private long redirectCount = 0;

    public UrlMapping() {}

    public UrlMapping(long seq, String shortCode, String longUrl) {
        this.seq = seq;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = Instant.now();
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public long getRedirectCount() { return redirectCount; }
    public void setRedirectCount(long redirectCount) { this.redirectCount = redirectCount; }
}