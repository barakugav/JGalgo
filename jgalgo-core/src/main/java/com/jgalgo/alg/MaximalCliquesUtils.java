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

import java.util.Iterator;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

class MaximalCliquesUtils {

	static abstract class AbstractImpl implements MaximalCliques {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <V, E> Iterator<Set<V>> iterateMaximalCliques(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (Iterator) iterateMaximalCliques((IndexGraph) g);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				Iterator<IntSet> indexResult = iterateMaximalCliques(iGraph);
				return (Iterator) new IntResultFromIndexResult(indexResult, viMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				Iterator<IntSet> indexResult = iterateMaximalCliques(iGraph);
				return new ObjResultFromIndexResult<>(indexResult, viMap);
			}
		}

		abstract Iterator<IntSet> iterateMaximalCliques(IndexGraph g);

	}

	private static class IntResultFromIndexResult implements Iterator<IntSet> {

		private final Iterator<IntSet> indexResult;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(Iterator<IntSet> indexResult, IndexIntIdMap viMap) {
			this.indexResult = indexResult;
			this.viMap = viMap;
		}

		@Override
		public boolean hasNext() {
			return indexResult.hasNext();
		}

		@Override
		public IntSet next() {
			return IndexIdMaps.indexToIdSet(indexResult.next(), viMap);
		}
	}

	private static class ObjResultFromIndexResult<V> implements Iterator<Set<V>> {

		private final Iterator<IntSet> indexResult;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Iterator<IntSet> indexResult, IndexIdMap<V> viMap) {
			this.indexResult = indexResult;
			this.viMap = viMap;
		}

		@Override
		public boolean hasNext() {
			return indexResult.hasNext();
		}

		@Override
		public Set<V> next() {
			return IndexIdMaps.indexToIdSet(indexResult.next(), viMap);
		}
	}

}
