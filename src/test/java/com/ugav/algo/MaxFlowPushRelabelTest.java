package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new);
	}
}
