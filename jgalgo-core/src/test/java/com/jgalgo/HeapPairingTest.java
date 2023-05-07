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

import java.util.Comparator;
import org.junit.jupiter.api.Test;

public class HeapPairingTest extends TestBase {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Heap.Builder<Integer> heapBuilder() {
		return new Heap.Builder<>() {

			@Override
			public Heap build(Comparator cmp) {
				return new HeapPairing<Integer, Object>(cmp).asHeap();
			}

			@Override
			public <OE> Heap.Builder<OE> objElements() {
				return (Heap.Builder<OE>) this;
			}

			@Override
			public <PE> Heap.Builder<PE> primitiveElements(Class<? extends PE> primitiveType) {
				return (Heap.Builder<PE>) this;
			}
		};
	}

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x7a98aed671bf0c81L;
		HeapTestUtils.testRandOpsDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0x3980b84440c200feL;
		HeapTestUtils.testRandOpsCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x25467ce9958980c1L;
		HeapTestUtils.testRandOpsAfterManyInserts(heapBuilder(), seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xc3cd155dfa9d97f6L;
		HeapTestUtils.testMeldDefaultCompare(heapBuilder(), seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0x5d201c45681ae903L;
		HeapTestUtils.testMeldCustomCompare(heapBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x90a80620c3ef1a43L;
		HeapReferenceableTestUtils.testDecreaseKeyDefaultCompare(HeapPairing::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x4204a31e91374f21L;
		HeapReferenceableTestUtils.testDecreaseKeyCustomCompare(HeapPairing::new, seed);
	}

}
