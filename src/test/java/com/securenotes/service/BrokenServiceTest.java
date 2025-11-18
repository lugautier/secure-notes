package com.securenotes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class BrokenServiceTest {

  @Test
  void testBrokenAddition_ShouldFail() {
    // This test intentionally fails to demonstrate CI/CD catching failures
    int result = 2 + 3;
    assertEquals(10, result); // ❌ Expects 10 but gets 5 - FAIL
  }
}
