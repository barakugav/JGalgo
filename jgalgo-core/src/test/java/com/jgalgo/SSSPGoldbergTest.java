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

public class SSSPGoldbergTest extends TestBase {

	@Test
	public void testRandGraphPositiveInt() {
		final long seed = 0x502218b82d4ab25aL;
		SSSPTestUtils.testSSSPDirectedPositiveInt(new SSSPGoldberg(), seed);
	}

	@Test
	public void testRandGraphNegativeInt() {
		final long seed = 0x15f829173b4f088bL;
		SSSPTestUtils.testSSSPDirectedNegativeInt(new SSSPGoldberg(), seed);
	}

	@Test
	public void testSSSPDirectedCardinality() {
		final long seed = 0x30a5e66dc18d88b3L;
		SSSPTestUtils.testSSSPCardinality(new SSSPGoldberg(), true, seed);
	}
}
