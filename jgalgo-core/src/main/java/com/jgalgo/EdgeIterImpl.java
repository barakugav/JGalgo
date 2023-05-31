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

import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;

interface EdgeIterImpl extends EdgeIter, Utils.IterPeekable.Int {

	static final EdgeIterImpl EmptyEdgeIter = new EdgeIterImpl() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public int nextInt() {
			throw new NoSuchElementException();
		}

		@Override
		public int peekNext() {
			throw new NoSuchElementException();
		}

		@Override
		public int source() {
			throw new NoSuchElementException();
		}

		@Override
		public int target() {
			throw new NoSuchElementException();
		}
	};

	static final EdgeSet EmptyEdgeSet = new EmptyEdgeSet();

	static class EmptyEdgeSet extends AbstractIntSet implements EdgeSet {

		private EmptyEdgeSet() {}

		@Override
		public boolean contains(int key) {
			return false;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public EdgeIter iterator() {
			return EmptyEdgeIter;
		}

	}

}
