package dev.nandu.cloudshorty.repo;

import dev.nandu.cloudshorty.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, String> {
    @Query("select l from Link l where l.ownerToken = ?1 order by l.createdAt desc")
    List<Link> findByOwnerTokenOrderByCreatedAtDesc(String ownerToken);
}
