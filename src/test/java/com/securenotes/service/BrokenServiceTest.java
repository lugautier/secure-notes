package com.securenotes.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrokenServiceTest {

	@Test
	void testBrokenAddition_ShouldFail() {
		// This test intentionally fails to demonstrate CI/CD catching failures
		int result = 2 + 3;
		assertEquals(10, result);  // ❌ Expects 10 but gets 5 - FAIL
	}
}
