package com.securenotes.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

  public String getStatus() {
    return "healthy";
  }

  public int add(int a, int b) {
    return a + b;
  }
}
