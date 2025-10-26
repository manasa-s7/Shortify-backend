package dev.nandu.cloudshorty.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "links")
public class Link {
    @Id
    @Column(length = 16)
    private String code;

    @Column(nullable = false, length = 2000)
    private String longUrl;

    @Column(nullable = false, length = 64)
    private String ownerToken;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private long clickCount = 0;

    private Instant lastClickAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String referrersJson; // JSON: {"host": count}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    public String getOwnerToken() { return ownerToken; }
    public void setOwnerToken(String ownerToken) { this.ownerToken = ownerToken; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public long getClickCount() { return clickCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }
    public Instant getLastClickAt() { return lastClickAt; }
    public void setLastClickAt(Instant lastClickAt) { this.lastClickAt = lastClickAt; }
    public String getReferrersJson() { return referrersJson; }
    public void setReferrersJson(String referrersJson) { this.referrersJson = referrersJson; }
}
