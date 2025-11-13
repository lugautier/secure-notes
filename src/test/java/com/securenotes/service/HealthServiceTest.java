package com.securenotes.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HealthServiceTest {

	@Autowired
	private HealthService healthService;

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
