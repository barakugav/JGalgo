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
package com.jgalgo.alg;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumVertexCutSTEdgeCutTest extends TestBase {

	@Test
	public void testRandGraphDirected() {
		final long seed = 0x36a317d1b8ab08b4L;
		MinimumVertexCutSTTestUtils.testRandGraphs(new MinimumVertexCutSTEdgeCut(), true, seed);
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x431ccd689c0ecea9L;
		MinimumVertexCutSTTestUtils.testRandGraphs(new MinimumVertexCutSTEdgeCut(), false, seed);
	}

}
