package dev.nandu.cloudshorty.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nandu.cloudshorty.model.Link;
import dev.nandu.cloudshorty.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

@Service
public class LinkService {
    private final LinkRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkService(LinkRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Link create(String longUrl, String ownerToken) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }
        String code = randomCode(6);
        while (repo.existsById(code)) code = randomCode(6);
        Link l = new Link();
        l.setCode(code);
        l.setLongUrl(longUrl);
        l.setOwnerToken(ownerToken);
        l.setCreatedAt(Instant.now());
        l.setClickCount(0);
        l.setReferrersJson("{}");
        return repo.save(l);
    }

    @Transactional(readOnly = true)
    public Optional<Link> findByCode(String code) {
        return repo.findById(code);
    }

    @Transactional(readOnly = true)
    public List<Link> listByOwner(String ownerToken) {
        return repo.findByOwnerTokenOrderByCreatedAtDesc(ownerToken);
    }

    @Transactional
    public void delete(String code, String ownerToken) {
        repo.findById(code).ifPresent(l -> {
            if (Objects.equals(l.getOwnerToken(), ownerToken)) {
                repo.deleteById(code);
            }
        });
    }

    // ✅ Fixed recordClick method — ensures count always updates
    @Transactional
    
public void recordClick(Link link, String referer) {
    // increment click count
    link.setClickCount(link.getClickCount() + 1);
    link.setLastClickAt(Instant.now());

    // handle referrers
    try {
        Map<String, Long> refs = mapper.readValue(
            Optional.ofNullable(link.getReferrersJson()).orElse("{}"),
            new TypeReference<Map<String, Long>>() {}
        );

        String host = refererHost(referer);
        if (host != null && !host.isBlank()) {
            refs.put(host, refs.getOrDefault(host, 0L) + 1);
        }

        link.setReferrersJson(mapper.writeValueAsString(refs));
    } catch (Exception e) {
        e.printStackTrace();
    }

    // save immediately to database
    repo.saveAndFlush(link);

    // debug log
    System.out.println("✅ Click recorded for code: " + link.getCode() +
                       " | Total clicks: " + link.getClickCount());
}

   
    public String shortUrl(String code) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/r/" + code;
    }

    private static String randomCode(int n) {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }

    private static boolean isValidUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static String refererHost(String referer) {
        try {
            return new URI(referer).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
