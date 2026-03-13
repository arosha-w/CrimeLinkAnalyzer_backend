package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.BackupMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupMetadataRepository extends JpaRepository<BackupMetadata, Long> {

    List<BackupMetadata> findAllByOrderByCreatedAtDesc();

    Optional<BackupMetadata> findByFilename(String filename);
}
