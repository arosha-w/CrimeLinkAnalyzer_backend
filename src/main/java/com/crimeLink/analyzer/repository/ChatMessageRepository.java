package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop50ByOrderByCreatedAtDesc();

    List<ChatMessage> findByCreatedAtAfterOrderByCreatedAtAsc(LocalDateTime after);
}
