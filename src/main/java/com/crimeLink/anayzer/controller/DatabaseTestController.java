package com.crimeLink.anayzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            response.put("connected", true);
            response.put("database", connection.getMetaData().getDatabaseProductName());
            response.put("version", connection.getMetaData().getDatabaseProductVersion());
            response.put("url", connection.getMetaData().getURL());
            response.put("username", connection.getMetaData().getUserName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("connected", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}
