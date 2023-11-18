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

package com.jgalgo.internal.ds;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class HeapPairingTestIntKeys extends TestBase {

	private static Heap.Builder heapBuilder() {
		return ReferenceableHeapTestUtils.heapBuilderFromReferenceableHeapBuilder(heapReferenceableBuilder());
	}

	private static ReferenceableHeap.Builder heapReferenceableBuilder() {
		return (keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntPairingHeap((IntComparator) comparator);
			if (keyType == int.class && valueType == void.class)
				return new IntPairingHeap((IntComparator) comparator);
			if (keyType == double.class && valueType == int.class)
				return new DoubleIntPairingHeap((DoubleComparator) comparator);
			if (keyType == double.class && valueType == Object.class)
				return new DoubleObjPairingHeap<>((DoubleComparator) comparator);
			if (keyType == Object.class && valueType == void.class)
				return new ObjPairingHeap<>(comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
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
		ReferenceableHeapTestUtils.testDecreaseKeyDefaultCompare(heapReferenceableBuilder(), seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x4204a31e91374f21L;
		ReferenceableHeapTestUtils.testDecreaseKeyCustomCompare(heapReferenceableBuilder(), seed);
	}

}
