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

public class SSSPDialTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x1ecd0cadb4951d87L;
		SSSPTestUtils.testSSSPDirectedPositiveInt(new SSSPDial(), seed);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		final long seed = 0xadc83d79349e7784L;
		SSSPTestUtils.testSSSPUndirectedPositiveInt(new SSSPDial(), seed);
	}

	@Test
	public void testSSSPUndirectedCardinality() {
		final long seed = 0xaf3606c2a17e51f6L;
		SSSPTestUtils.testSSSPCardinality(new SSSPDial(), false, seed);
	}

	@Test
	public void testSSSPDirectedCardinality() {
		final long seed = 0x248003d317888444L;
		SSSPTestUtils.testSSSPCardinality(new SSSPDial(), true, seed);
	}

}
