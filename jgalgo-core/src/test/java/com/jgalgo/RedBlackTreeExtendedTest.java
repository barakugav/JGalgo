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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.HeapReferenceableTestUtils.TestMode;
import it.unimi.dsi.fastutil.ints.IntComparator;

@SuppressWarnings("boxing")
public class RedBlackTreeExtendedTest extends TestBase {

	@Test
	public void testExtensionSizeRandOps() {
		final long seed = 0xe5136a0085e719d1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Size<Integer, Void> sizeExt = new RedBlackTreeExtension.Size<>();
			RedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(sizeExt));

			HeapReferenceableTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());

			for (HeapReference<Integer, Void> node : tree) {
				int expectedSize = 0;

				for (@SuppressWarnings("unused")
				HeapReference<Integer, Void> descendant : Utils.iterable(tree.subTreeIterator(node)))
					expectedSize++;

				int actualSize = sizeExt.getSubTreeSize(node);
				assertEquals(expectedSize, actualSize, "Size extension reported wrong value");
			}
		});
	}

	@Test
	public void testExtensionMinRandOps() {
		final long seed = 0xe5136a0085e719d1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		List<Phase> phases = List.of(phase(64, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Min<Integer, Void> minExt = new RedBlackTreeExtension.Min<>();
			RedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(minExt));

			HeapReferenceableTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());

			for (HeapReference<Integer, Void> node : tree) {
				int expectedMin = Integer.MAX_VALUE;
				for (HeapReference<Integer, Void> descendant : Utils.iterable(tree.subTreeIterator(node)))
					expectedMin = Math.min(expectedMin, descendant.key());

				int actualMin = minExt.getSubTreeMin(node).key();
				assertEquals(expectedMin, actualMin, "Min extension reported wrong value");
			}
		});
	}

	@Test
	public void testExtensionMaxRandOps() {
		final long seed = 0x7674bddef0a0863bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		List<Phase> phases = List.of(phase(64, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Max<Integer, Void> maxExt = new RedBlackTreeExtension.Max<>();
			RedBlackTree<Integer, Void> tree = new RedBlackTreeExtended<>(compare, List.of(maxExt));

			HeapReferenceableTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());
			for (HeapReference<Integer, Void> node : tree) {
				int expectedMax = Integer.MIN_VALUE;
				for (HeapReference<Integer, Void> descendant : Utils.iterable(tree.subTreeIterator(node)))
					expectedMax = Math.max(expectedMax, descendant.key());

				int actualMax = maxExt.getSubTreeMax(node).key();
				assertEquals(expectedMax, actualMax, "Max extension reported wrong value");
			}
		});
	}

}