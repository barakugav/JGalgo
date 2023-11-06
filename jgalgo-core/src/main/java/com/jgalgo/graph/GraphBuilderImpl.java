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
package com.jgalgo.graph;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSets;

class GraphBuilderImpl {

	static <V, E> GraphBuilder<V, E> newFrom(Graph<V, E> g, boolean copyWeights) {
		return g.isDirected() ? new GraphBuilderImpl.Directed<>(g, copyWeights)
				: new GraphBuilderImpl.Undirected<>(g, copyWeights);
	}

	private abstract static class Abstract<V, E> implements GraphBuilder<V, E> {

		final IndexGraphBuilder ibuilder;
		private final Object2IntOpenHashMap<V> vIdToIndex;
		private final ObjectArrayList<V> vIndexToId;
		private final Set<V> vertices;
		private final Object2IntOpenHashMap<E> eIdToIndex;
		private final ObjectArrayList<E> eIndexToId;
		private final Set<E> edges;
		final IndexIdMapImpl<V> viMap;
		final IndexIdMapImpl<E> eiMap;
		private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
		private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();

		Abstract(IndexGraphBuilder ibuilder) {
			assert ibuilder.vertices().isEmpty();
			assert ibuilder.edges().isEmpty();
			this.ibuilder = ibuilder;
			vIdToIndex = new Object2IntOpenHashMap<>();
			vIdToIndex.defaultReturnValue(-1);
			vIndexToId = new ObjectArrayList<>();
			vertices = ObjectSets.unmodifiable(vIdToIndex.keySet());
			eIdToIndex = new Object2IntOpenHashMap<>();
			eIdToIndex.defaultReturnValue(-1);
			eIndexToId = new ObjectArrayList<>();
			edges = ObjectSets.unmodifiable(eIdToIndex.keySet());
			viMap = new IndexIdMapImpl<>(vIdToIndex, vIndexToId, false);
			eiMap = new IndexIdMapImpl<>(eIdToIndex, eIndexToId, true);
		}

		Abstract(Graph<V, E> g, boolean copyWeights) {
			final int n = g.vertices().size();
			final int m = g.edges().size();
			this.ibuilder = IndexGraphBuilder.newFrom(g.indexGraph(), copyWeights);
			vIdToIndex = new Object2IntOpenHashMap<>(n);
			vIdToIndex.defaultReturnValue(-1);
			vIndexToId = new ObjectArrayList<>(n);
			vertices = ObjectSets.unmodifiable(vIdToIndex.keySet());
			eIdToIndex = new Object2IntOpenHashMap<>(m);
			eIdToIndex.defaultReturnValue(-1);
			eIndexToId = new ObjectArrayList<>(m);
			edges = ObjectSets.unmodifiable(eIdToIndex.keySet());
			viMap = new IndexIdMapImpl<>(vIdToIndex, vIndexToId, false);
			eiMap = new IndexIdMapImpl<>(eIdToIndex, eIndexToId, true);

			IndexIdMap<V> gViMap = g.indexGraphVerticesMap();
			IndexIdMap<E> gEiMap = g.indexGraphEdgesMap();
			for (int vIdx = 0; vIdx < n; vIdx++) {
				V v = gViMap.indexToId(vIdx);
				vIndexToId.add(v);
				vIdToIndex.put(v, vIdx);
			}
			for (int eIdx = 0; eIdx < m; eIdx++) {
				E e = gEiMap.indexToId(eIdx);
				eIndexToId.add(e);
				eIdToIndex.put(e, eIdx);
			}
		}

		@Override
		public Set<V> vertices() {
			return vertices;
		}

		@Override
		public Set<E> edges() {
			return edges;
		}

		@Override
		public void addVertex(V vertex) {
			int vIndex = ibuilder.addVertex();
			V vId = vertex;
			assert vIndex == vIndexToId.size();
			vIndexToId.add(vId);
			int oldVal = vIdToIndex.put(vId, vIndex);
			if (oldVal != vIdToIndex.defaultReturnValue())
				throw new IllegalArgumentException("duplicate vertex: " + vId);
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			int sourceIdx = vIdToIndex.getInt(source);
			int targetIdx = vIdToIndex.getInt(target);
			if (targetIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException("invalid vertex: " + target);
			if (sourceIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException("invalid vertex: " + source);

			int eIndex = ibuilder.addEdge(sourceIdx, targetIdx);
			E eId = edge;
			assert eIndex == eIndexToId.size();
			eIndexToId.add(eId);
			int oldVal = eIdToIndex.put(eId, eIndex);
			if (oldVal != eIdToIndex.defaultReturnValue())
				throw new IllegalArgumentException("duplicate edge: " + edge);
		}

		@Override
		public void expectedVerticesNum(int verticesNum) {
			ibuilder.expectedVerticesNum(verticesNum);
			vIdToIndex.ensureCapacity(verticesNum);
			vIndexToId.ensureCapacity(verticesNum);
		}

		@Override
		public void expectedEdgesNum(int edgesNum) {
			ibuilder.expectedEdgesNum(edgesNum);
			eIdToIndex.ensureCapacity(edgesNum);
			eIndexToId.ensureCapacity(edgesNum);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
			WeightsImpl.Index<T> indexWeights = ibuilder.getVerticesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.ObjMapped.newInstance(iw, viMap));
		}

		@Override
		public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
				T defVal) {
			ibuilder.addVerticesWeights(key, type, defVal);
			return getVerticesWeights(key);
		}

		@Override
		public Set<String> getVerticesWeightsKeys() {
			return ibuilder.getVerticesWeightsKeys();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
			WeightsImpl.Index<T> indexWeights = ibuilder.getEdgesWeights(key);
			if (indexWeights == null)
				return null;
			return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
					iw -> WeightsImpl.ObjMapped.newInstance(iw, eiMap));
		}

		@Override
		public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
				T defVal) {
			ibuilder.addEdgesWeights(key, type, defVal);
			return getEdgesWeights(key);
		}

		@Override
		public Set<String> getEdgesWeightsKeys() {
			return ibuilder.getEdgesWeightsKeys();
		}

		@Override
		public void clear() {
			ibuilder.clear();
			vIdToIndex.clear();
			vIndexToId.clear();
			eIdToIndex.clear();
			eIndexToId.clear();
		}

		private static class IndexIdMapImpl<K> implements IndexIdMap<K> {
			private final Object2IntMap<K> idToIndex;
			private final ObjectList<K> indexToId;
			private final boolean isEdges;

			IndexIdMapImpl(Object2IntMap<K> idToIndex, ObjectList<K> indexToId, boolean isEdges) {
				this.idToIndex = idToIndex;
				this.indexToId = indexToId;
				this.isEdges = isEdges;
			}

			@Override
			public K indexToId(int index) {
				return indexToId.get(index);
			}

			@Override
			public int idToIndex(K id) {
				int idx = idToIndex.getInt(id);
				if (idx < 0)
					throw new IndexOutOfBoundsException("No such " + (isEdges ? "edge" : "vertex") + ": " + id);
				return idx;
			}
		}

		static <K> IndexIdMap<K> reIndexedIdMap(IndexIdMap<K> iMapOrig, IndexGraphBuilder.ReIndexingMap indexingMap) {
			return new IndexIdMap<>() {

				@Override
				public K indexToId(int index) {
					return iMapOrig.indexToId(indexingMap.reIndexedToOrig(index));
				}

				@Override
				public int idToIndex(K id) {
					return indexingMap.origToReIndexed(iMapOrig.idToIndex(id));
				}
			};
		}
	}

	static class Undirected<V, E> extends GraphBuilderImpl.Abstract<V, E> {

		Undirected(IndexGraphBuilder indexBuilder) {
			super(indexBuilder);
		}

		Undirected() {
			this(IndexGraphBuilder.newUndirected());
		}

		Undirected(Graph<V, E> g, boolean copyWeights) {
			super(g, copyWeights);
			Assertions.Graphs.onlyUndirected(g);
		}

		@Override
		public Graph<V, E> build() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuild(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap<V> viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap<E> eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Undirected<>(iGraph, viMap, eiMap);
		}

		@Override
		public Graph<V, E> buildMutable() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuildMutable(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap<V> viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap<E> eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Undirected<>(iGraph, viMap, eiMap);
		}
	}

	static class Directed<V, E> extends GraphBuilderImpl.Abstract<V, E> {

		Directed(IndexGraphBuilder indexBuilder) {
			super(indexBuilder);
		}

		Directed() {
			this(IndexGraphBuilder.newDirected());
		}

		Directed(Graph<V, E> g, boolean copyWeights) {
			super(g, copyWeights);
			Assertions.Graphs.onlyDirected(g);
		}

		@Override
		public Graph<V, E> build() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuild(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap<V> viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap<E> eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Directed<>(iGraph, viMap, eiMap);
		}

		@Override
		public Graph<V, E> buildMutable() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuildMutable(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap<V> viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap<E> eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Directed<>(iGraph, viMap, eiMap);
		}
	}

}
