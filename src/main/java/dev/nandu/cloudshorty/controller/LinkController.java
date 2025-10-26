package dev.nandu.cloudshorty.controller;

import dev.nandu.cloudshorty.model.Link;
import dev.nandu.cloudshorty.service.LinkService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController
@Validated
public class LinkController {

    private final LinkService service;

    @Value("${app.base-url}")
    private String baseUrl;

    public LinkController(LinkService service) {
        this.service = service;
    }

    // ✅ Create Short Link
    @PostMapping("/api/links")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String longUrl = body.getOrDefault("longUrl", "");
        String ownerToken = body.getOrDefault("ownerToken", "");
        if (ownerToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ownerToken required"));
        }

        try {
            Link l = service.create(longUrl, ownerToken);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", l.getCode());
            resp.put("longUrl", l.getLongUrl());
            resp.put("shortUrl", service.shortUrl(l.getCode()));
            resp.put("ownerToken", l.getOwnerToken());
            resp.put("createdAt", l.getCreatedAt());
            resp.put("clickCount", l.getClickCount());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ List Links by Owner Token
    @GetMapping("/api/links")
    public List<Link> list(@RequestParam @NotBlank String ownerToken) {
        return service.listByOwner(ownerToken);
    }

    // ✅ Delete Link
    @DeleteMapping("/api/links/{code}")
    public ResponseEntity<?> delete(@PathVariable String code, @RequestParam @NotBlank String ownerToken) {
        service.delete(code, ownerToken);
        return ResponseEntity.noContent().build();
    }

    // ✅ Redirect + Count Clicks (Fixed)
    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirect(
            @PathVariable String code,
            @RequestHeader(value = "referer", required = false) String referer) {

        Optional<Link> linkOpt = service.findByCode(code);
        if (linkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Link link = linkOpt.get();
        service.recordClick(link, referer);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(link.getLongUrl()));

        // 🧠 Prevent caching the redirect
        headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        headers.add("Pragma", "no-cache");

        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Found
    }
}
