package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringGreedyTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xe57268894020f1d1L;
		ColoringTestUtils.testRandGraphs(new ColoringGreedy(), seed);
	}

}
