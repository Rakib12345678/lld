package com.rocky.shorturl.service;

import com.rocky.shorturl.model.UrlMapping;
import com.rocky.shorturl.repository.UrlMappingRepository;
import com.rocky.shorturl.util.Base62;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ShortenerService {
    @Autowired
    private SequenceService sequenceService;


    @Autowired
    private UrlMappingRepository urlMappingRepository;


    @Value("${shortener.domain}")
    private String domain; // e.g. https://rocky.tech/


    /**
     * Create (or return existing) short URL for the given long URL.
     */
    @Transactional
    public String shorten(String longUrl) {
// validate URL
        if (!isValidUrl(longUrl)) throw new IllegalArgumentException("Invalid URL: " + longUrl);


// If longUrl already exists, return same short URL
        Optional<UrlMapping> existing = urlMappingRepository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return buildShortUrl(existing.get().getShortCode());
        }


        long seq = sequenceService.getNextSequence();
        String shortCode = Base62.encode(seq);
        UrlMapping mapping = new UrlMapping(seq, shortCode, longUrl);
        urlMappingRepository.save(mapping);
        return buildShortUrl(shortCode);
    }


    public Optional<String> resolve(String code) {
        Optional<UrlMapping> mapping = urlMappingRepository.findByShortCode(code);
        return mapping.map(UrlMapping::getLongUrl);
    }


    @Transactional
    public void incrementRedirectCount(String code) {
        Optional<UrlMapping> mappingOpt = urlMappingRepository.findByShortCode(code);
        mappingOpt.ifPresent(m -> {
            m.setRedirectCount(m.getRedirectCount() + 1);
            urlMappingRepository.save(m);
        });
    }


    private String buildShortUrl(String code) {
        if (domain.endsWith("/")) return domain + code;
        return domain + "/" + code;
    }


    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
