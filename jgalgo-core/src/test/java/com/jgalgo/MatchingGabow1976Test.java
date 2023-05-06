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

public class MatchingGabow1976Test extends TestBase {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x915c26f5de8fd97aL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MaximumMatchingCardinalityGabow1976(), seed);
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0x6809f5efef8504e9L;
		MatchingUnweightedTestUtils.randGraphs(new MaximumMatchingCardinalityGabow1976(), seed);
	}

}
