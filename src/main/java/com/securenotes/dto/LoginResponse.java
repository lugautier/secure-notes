package com.securenotes.dto;

public record LoginResponse(String token, Long expiresIn) {}
