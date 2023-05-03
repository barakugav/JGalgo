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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.jgalgo.HeapTestUtils.TestMode;

@SuppressWarnings("boxing")
public class RedBlackTreeExtendedTest extends TestBase {

	private static class HeapWrapper<E> implements Heap<E> {

		private final Heap<E> h;

		public HeapWrapper(Heap<E> heap) {
			h = Objects.requireNonNull(heap);
		}

		@Override
		public int size() {
			return h.size();
		}

		@Override
		public boolean isEmpty() {
			return h.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return h.contains(o);
		}

		@Override
		public Iterator<E> iterator() {
			return h.iterator();
		}

		@Override
		public Object[] toArray() {
			return h.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return h.toArray(a);
		}

		@Override
		public boolean add(E e) {
			return h.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return h.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return h.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			return h.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return h.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return h.retainAll(c);
		}

		@Override
		public void clear() {
			h.clear();
		}

		@Override
		public HeapReference<E> insert(E e) {
			return h.insert(e);
		}

		@Override
		public E findMin() {
			return h.findMin();
		}

		@Override
		public E extractMin() {
			return h.extractMin();
		}

		@Override
		public void meld(Heap<? extends E> other) {
			h.meld(other);
		}

		@Override
		public Comparator<? super E> comparator() {
			return h.comparator();
		}

	}

	private static class SizeValidatorTree<E> extends HeapWrapper<E> {

		private SizeValidatorTree(RedBlackTree<E> tree, RedBlackTreeExtension.Size<E> sizeExt) {
			super(tree);
		}

		@SuppressWarnings("unused")
		static <E> SizeValidatorTree<E> newInstance(Comparator<? super E> c) {
			RedBlackTreeExtension.Size<E> sizeExt = new RedBlackTreeExtension.Size<>();
			RedBlackTree<E> tree = new RedBlackTreeExtended<>(c, List.of(sizeExt));
			return new SizeValidatorTree<>(tree, sizeExt);
		}

	}

	@Test
	public void testExtensionSizeRandOps() {
		final long seed = 0xe5136a0085e719d1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Comparator<Integer> compare = null;
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Size<Integer> sizeExt = new RedBlackTreeExtension.Size<>();
			RedBlackTree<Integer> tree = new RedBlackTreeExtended<>(compare, List.of(sizeExt));

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());

			for (HeapReference<Integer> node : tree.refsSet()) {
				int expectedSize = 0;

				for (@SuppressWarnings("unused")
				HeapReference<Integer> descendant : Utils.iterable(tree.experimental_subTreeIterator(node)))
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
		final Comparator<Integer> compare = null;
		List<Phase> phases = List.of(phase(64, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Min<Integer> minExt = new RedBlackTreeExtension.Min<>();
			RedBlackTree<Integer> tree = new RedBlackTreeExtended<>(compare, List.of(minExt));

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());

			for (HeapReference<Integer> node : tree.refsSet()) {
				int expectedMin = Integer.MAX_VALUE;
				for (HeapReference<Integer> descendant : Utils.iterable(tree.experimental_subTreeIterator(node)))
					expectedMin = Math.min(expectedMin, descendant.get());

				int actualMin = minExt.getSubTreeMin(node).get();
				assertEquals(expectedMin, actualMin, "Min extension reported wrong value");
			}
		});
	}

	@Test
	public void testExtensionMaxRandOps() {
		final long seed = 0x7674bddef0a0863bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Comparator<Integer> compare = null;
		List<Phase> phases = List.of(phase(64, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			RedBlackTreeExtension.Max<Integer> maxExt = new RedBlackTreeExtension.Max<>();
			RedBlackTree<Integer> tree = new RedBlackTreeExtended<>(compare, List.of(maxExt));

			HeapTestUtils.testHeap(tree, n, m, TestMode.Normal, false, compare, seedGen.nextSeed());
			for (HeapReference<Integer> node : tree.refsSet()) {
				int expectedMax = Integer.MIN_VALUE;
				for (HeapReference<Integer> descendant : Utils.iterable(tree.experimental_subTreeIterator(node)))
					expectedMax = Math.max(expectedMax, descendant.get());

				int actualMax = maxExt.getSubTreeMax(node).get();
				assertEquals(expectedMax, actualMax, "Max extension reported wrong value");
			}
		});
	}

}
