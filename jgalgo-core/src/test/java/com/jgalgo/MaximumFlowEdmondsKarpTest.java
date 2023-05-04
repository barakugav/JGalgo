/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowEdmondsKarpTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x398eea4097bc0600L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowEdmondsKarp(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0xa180ffaa75a62d0cL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowEdmondsKarp(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0xaa7eab04a9b554cbL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowEdmondsKarp());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
