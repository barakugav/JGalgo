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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class GraphBuilderImpl<V, E> implements GraphBuilder<V, E> {

	final IndexGraphBuilder ibuilder;
	final IndexIdMapImpl<V> viMap;
	final IndexIdMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();

	GraphBuilderImpl(IndexGraphBuilder ibuilder) {
		this.ibuilder = ibuilder;
		viMap = IndexIdMapImpl.newEmpty(ibuilder.vertices(), false, 0);
		eiMap = IndexIdMapImpl.newEmpty(ibuilder.edges(), true, 0);
	}

	GraphBuilderImpl(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		this.ibuilder = IndexGraphBuilder.fromGraph(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIdMapImpl.newCopyOf(g.indexGraphVerticesMap(), null, ibuilder.vertices(), false, false);
		eiMap = IndexIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), null, ibuilder.edges(), true, false);
	}

	@Override
	public Set<V> vertices() {
		return viMap.idSet();
	}

	@Override
	public Set<E> edges() {
		return eiMap.idSet();
	}

	@Override
	public void addVertex(V vertex) {
		if (vertex == null)
			throw new NullPointerException("Vertex must be non null");
		int vIdx = ibuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = ibuilder.addVertex();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		if (vertices.isEmpty())
			return;
		for (V vertex : vertices)
			if (vertex == null)
				throw new NullPointerException("Vertex must be non null");

		final int verticesNumBefore = ibuilder.vertices().size();
		int nextIdx = verticesNumBefore;
		V duplicateVertex = null;
		for (V vertex : vertices) {
			boolean added = viMap.addIdIfNotDuplicate(vertex, nextIdx);
			if (!added) {
				duplicateVertex = vertex;
				break;
			}
			nextIdx++;
		}
		if (duplicateVertex != null) {
			for (; nextIdx-- > verticesNumBefore;)
				viMap.rollBackRemove(nextIdx);
			throw new IllegalArgumentException("Duplicate vertex: " + duplicateVertex);
		}
		ibuilder.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		if (edge == null)
			throw new NullPointerException("Edge must be non null");
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = ibuilder.edges().size();
		eiMap.addId(edge, eIdx);

		int eIdx2 = ibuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;
	}

	@Override
	public void expectedVerticesNum(int verticesNum) {
		ibuilder.expectedVerticesNum(verticesNum);
		viMap.ensureCapacity(verticesNum);
	}

	@Override
	public void expectedEdgesNum(int edgesNum) {
		ibuilder.expectedEdgesNum(edgesNum);
		eiMap.ensureCapacity(edgesNum);
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
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
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
		viMap.idsClear();
		eiMap.idsClear();
		verticesWeights.clear();
		edgesWeights.clear();
	}

	@Override
	public boolean isDirected() {
		return ibuilder.isDirected();
	}

	@Override
	public Graph<V, E> build() {
		return buildFromReIndexed(ibuilder.reIndexAndBuild(true, true));
	}

	@Override
	public Graph<V, E> buildMutable() {
		return buildFromReIndexed(ibuilder.reIndexAndBuildMutable(true, true));
	}

	private Graph<V, E> buildFromReIndexed(IndexGraphBuilder.ReIndexedGraph reIndexedGraph) {
		IndexGraph iGraph = reIndexedGraph.graph();
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
		return new GraphImpl<>(iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
	}

}
