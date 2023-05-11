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

public class MaximumFlowDinicTest extends TestBase {

	@Test
	public void testRandDiGraphs() {
		final long seed = 0xa79b303ec46fd984L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowDinic(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x6be26a022c1cd652L;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowDinic(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0xb49154497703863bL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowDinic());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
