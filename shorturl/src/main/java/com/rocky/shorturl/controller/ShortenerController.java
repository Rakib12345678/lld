package com.rocky.shorturl.controller;

import com.rocky.shorturl.dto.ShortenRequest;
import com.rocky.shorturl.service.ShortenerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.Optional;

public class ShortenerController {
    @Autowired
    private ShortenerService shortenerService;

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shorten(@RequestBody @Valid ShortenRequest request) {
        try {
            String shortUrl = shortenerService.shorten(request.getUrl());
            return ResponseEntity.ok().body(new java.util.HashMap<>() {{ put("shortUrl", shortUrl); }});
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<>() {{ put("error", ex.getMessage()); }});
        }
    }

    // Redirect endpoint: GET /{code}
    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        Optional<String> longUrl = shortenerService.resolve(code);
        if (longUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        // increment metric
        shortenerService.incrementRedirectCount(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl.get()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
