package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.HeapBinomial;

public class HeapBinomialTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(HeapBinomial::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(HeapBinomial::new);
	}

	@Test
	public void testDecreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapBinomial::new);
	}

}