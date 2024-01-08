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
package com.jgalgo.internal.util;

import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSpliterator;

public final class Range extends AbstractIntSet {

	private final int from, to;

	private Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public static Range range(int to) {
		if (0 > to)
			throw new IllegalArgumentException("negative 'to': " + to);
		return new Range(0, to);
	}

	public static Range range(int from, int to) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		return new Range(from, to);
	}

	@Override
	public int size() {
		return to - from;
	}

	@Override
	public boolean contains(int key) {
		return from <= key && key < to;
	}

	@Override
	public IntIterator iterator() {
		return new Iter(from, to);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Range) {
			Range r = (Range) o;
			if (isEmpty())
				return r.isEmpty();
			return to == r.to && from == r.from;

		} else if (o instanceof IntSet) {
			IntSet s = (IntSet) o;
			int size = size();
			if (size != s.size())
				return false;
			if (size == 0)
				return true;
			int min, max;
			IntIterator it = s.iterator();
			min = max = it.nextInt();
			while (--size > 0) {
				int x = it.nextInt();
				if (max < x) {
					max = x;
				} else if (min > x) {
					min = x;
				}
			}
			return min == from && max == to - 1;

		} else {
			return super.equals(o);
		}
	}

	@Override
	public int hashCode() {
		/* hash code compatible with IntSet */
		return (from + to - 1) * (to - from) / 2;
	}

	public IntStream map(IntUnaryOperator mapper) {
		return intStream().map(mapper);
	}

	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		return intStream().mapToObj(mapper);
	}

	public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
		return intStream().mapToDouble(mapper);
	}

	public LongStream mapToLong(IntToLongFunction mapper) {
		return intStream().mapToLong(mapper);
	}

	public IntStream filter(IntPredicate predicate) {
		return intStream().filter(predicate);
	}

	public boolean allMatch(IntPredicate allMatch) {
		return intStream().allMatch(allMatch);
	}

	@Override
	public IntSpliterator spliterator() {
		return new SplitIter(from, to);
	}

	private static class Iter implements IntIterator {

		private int x;
		private final int to;

		Iter(int from, int to) {
			assert from <= to;
			this.x = from;
			this.to = to;
		}

		@Override
		public boolean hasNext() {
			return x < to;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			return x++;
		}

		@Override
		public int skip(final int n) {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			if (n < to - x) {
				x += n;
				return n;
			} else {
				int begin = x;
				x = to;
				return x - begin;
			}
		}
	}

	private static class SplitIter implements IntSpliterator {

		private int x;
		private final int to;

		SplitIter(int from, int to) {
			assert from <= to;
			this.x = from;
			this.to = to;
		}

		@Override
		public int characteristics() {
			return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
					| Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.IMMUTABLE;
		}

		@Override
		public long estimateSize() {
			return to - x;
		}

		@Override
		public boolean tryAdvance(IntConsumer action) {
			if (x >= to)
				return false;
			action.accept(x++);
			return true;
		}

		@Override
		public IntSpliterator trySplit() {
			int size = to - x;
			if (size < 2)
				return null;
			int mid = x + size / 2;
			IntSpliterator prefix = new SplitIter(x, mid);
			x = mid;
			return prefix;
		}

		@Override
		public void forEachRemaining(final IntConsumer action) {
			for (; x < to; x++)
				action.accept(x);
		}

		@Override
		public long skip(long n) {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			if (n < to - x) {
				x += n;
				return n;
			} else {
				int begin = x;
				x = to;
				return x - begin;
			}
		}

		@Override
		public IntComparator getComparator() {
			return null;
		}
	}

}
