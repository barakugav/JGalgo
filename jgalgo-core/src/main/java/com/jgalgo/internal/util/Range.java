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

import java.util.NoSuchElementException;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSpliterator;

public class Range extends AbstractIntSet {

	private final int from, to;

	private Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public static Range range(int to) {
		return new Range(0, to);
	}

	public static Range range(int from, int to) {
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
		return new IntIterator() {

			int x = from;

			@Override
			public int nextInt() {
				if (x >= to)
					throw new NoSuchElementException();
				return x++;
			}

			@Override
			public boolean hasNext() {
				return x < to;
			}
		};
	}

	@Override
	public IntSpliterator spliterator() {
		return super.spliterator();
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

	public IntStream filter(IntPredicate predicate) {
		return intStream().filter(predicate);
	}

	public boolean allMatch(IntPredicate allMatch) {
		return intStream().allMatch(allMatch);
	}

}
