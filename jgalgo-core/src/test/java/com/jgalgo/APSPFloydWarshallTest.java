package com.jgalgo;

import org.junit.jupiter.api.Test;

class APSPFloydWarshallTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x80b8af9bfbd5e5d5L;
		APSPTestUtils.testAPSPDirectedPositiveInt(new APSPFloydWarshall(), seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0x307fc7bb8684a8b5L;
		APSPTestUtils.testAPSPUndirectedPositiveInt(new APSPFloydWarshall(), seed);
	}

	@Test
	public void testRandGraphDirectedNegativeInt() {
		final long seed = 0xd3037473c85e47b3L;
		APSPTestUtils.testAPSPDirectedNegativeInt(new APSPFloydWarshall(), seed);
	}

}
