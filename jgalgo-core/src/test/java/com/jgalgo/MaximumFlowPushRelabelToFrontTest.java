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

public class MaximumFlowPushRelabelToFrontTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x8fb191d57a090f45L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x3d296bd5e39fbefbL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x5817e5c904a5dad1L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc833101540b8e5f1L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelToFront());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
