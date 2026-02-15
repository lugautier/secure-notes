package com.securenotes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HealthServiceTest {

  private HealthService healthService;

  @BeforeEach
  void setUp() {
    healthService = new HealthService();
  }

  @Test
  void testGetStatus_ReturnsHealthy() {
    String result = healthService.getStatus();
    assertEquals("healthy", result);
  }

  @Test
  void testAdd_TwoNumbers_ReturnsSum() {
    int result = healthService.add(2, 3);
    assertEquals(5, result);
  }

  @Test
  void testAdd_NegativeNumbers_ReturnsCorrectSum() {
    int result = healthService.add(-5, 3);
    assertEquals(-2, result);
  }
}
