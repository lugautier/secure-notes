package com.securenotes.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ProfileResponse(
    UUID id, String email, Set<String> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {}
