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

import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class TreePathMaximaUtils {

	static abstract class AbstractImpl implements TreePathMaxima {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> TreePathMaxima.Result<V, E> computeHeaviestEdgeInTreePaths(Graph<V, E> tree, WeightFunction<E> w,
				TreePathMaxima.Queries<V, E> queries) {
			if (tree instanceof IndexGraph && queries instanceof TreePathMaxima.IQueries) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				TreePathMaxima.IQueries queries0 = (TreePathMaxima.IQueries) queries;
				return (TreePathMaxima.Result<V, E>) computeHeaviestEdgeInTreePaths((IndexGraph) tree, w0, queries0);
			} else if (tree instanceof IntGraph) {
				IndexGraph iGraph = tree.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) tree).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) tree).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				TreePathMaxima.IQueries iQueries;
				if (queries instanceof TreePathMaxima.IQueries) {
					iQueries = new IndexQueriesFromIntQueries((TreePathMaxima.IQueries) queries, viMap);
				} else {
					iQueries =
							new IndexQueriesFromObjQueries<>((TreePathMaxima.Queries<Integer, Integer>) queries, viMap);
				}
				TreePathMaxima.IResult indexResult = computeHeaviestEdgeInTreePaths(iGraph, iw, iQueries);
				return (TreePathMaxima.Result<V, E>) new IntResultFromIndexResult(indexResult, eiMap);

			} else {
				IndexGraph iGraph = tree.indexGraph();
				IndexIdMap<V> viMap = tree.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = tree.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				TreePathMaxima.IQueries iQueries = new IndexQueriesFromObjQueries<>(queries, viMap);
				TreePathMaxima.IResult indexResult = computeHeaviestEdgeInTreePaths(iGraph, iw, iQueries);
				return new ObjResultFromIndexResult<>(indexResult, eiMap);
			}
		}

		abstract TreePathMaxima.IResult computeHeaviestEdgeInTreePaths(IndexGraph tree, IWeightFunction w,
				TreePathMaxima.IQueries queries);

	}

	static class ObjQueriesImpl<V, E> implements TreePathMaxima.Queries<V, E> {
		private final List<V> qs;

		ObjQueriesImpl() {
			qs = new ObjectArrayList<>();
		}

		@Override
		public void addQuery(V u, V v) {
			qs.add(u);
			qs.add(v);
		}

		@Override
		public V getQuerySource(int idx) {
			return qs.get(idx * 2 + 0);
		}

		@Override
		public V getQueryTarget(int idx) {
			return qs.get(idx * 2 + 1);
		}

		@Override
		public int size() {
			return qs.size() / 2;
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class IntQueriesImpl implements TreePathMaxima.IQueries {
		private final LongList qs;

		IntQueriesImpl() {
			qs = new LongArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(JGAlgoUtils.longPack(u, v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return JGAlgoUtils.long2low(qs.getLong(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
			return JGAlgoUtils.long2high(qs.getLong(idx));
		}

		@Override
		public int size() {
			return qs.size();
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class ResultImpl implements TreePathMaxima.IResult {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
		}

		@Override
		public int getHeaviestEdgeInt(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

	static class IndexQueriesFromObjQueries<V, E> implements TreePathMaxima.IQueries {
		private final TreePathMaxima.Queries<V, E> qs;
		private final IndexIdMap<V> viMap;

		IndexQueriesFromObjQueries(TreePathMaxima.Queries<V, E> qs, IndexIdMap<V> viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToId(u), viMap.indexToId(v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return viMap.idToIndex(qs.getQuerySource(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
			return viMap.idToIndex(qs.getQueryTarget(idx));
		}

		@Override
		public int size() {
			return qs.size();
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class IndexQueriesFromIntQueries implements TreePathMaxima.IQueries {
		private final TreePathMaxima.IQueries qs;
		private final IndexIntIdMap viMap;

		IndexQueriesFromIntQueries(TreePathMaxima.IQueries qs, IndexIntIdMap viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToIdInt(u), viMap.indexToIdInt(v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return viMap.idToIndex(qs.getQuerySourceInt(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
			return viMap.idToIndex(qs.getQueryTargetInt(idx));
		}

		@Override
		public int size() {
			return qs.size();
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements TreePathMaxima.Result<V, E> {

		private final TreePathMaxima.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(TreePathMaxima.IResult indexRes, IndexIdMap<E> eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public E getHeaviestEdge(int queryIdx) {
			int eIdx = indexRes.getHeaviestEdgeInt(queryIdx);
			return eIdx == -1 ? null : eiMap.indexToId(eIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	private static class IntResultFromIndexResult implements TreePathMaxima.IResult {

		private final TreePathMaxima.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(TreePathMaxima.IResult indexRes, IndexIntIdMap eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int getHeaviestEdgeInt(int queryIdx) {
			int eIdx = indexRes.getHeaviestEdgeInt(queryIdx);
			return eIdx == -1 ? -1 : eiMap.indexToIdInt(eIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	static boolean verifyMST(IndexGraph g, IWeightFunction w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		Assertions.Graphs.onlyUndirected(g);
		int n = g.vertices().size();
		IndexGraphBuilder mstBuilder = IndexGraphBuilder.newUndirected();
		mstBuilder.expectedVerticesNum(n);
		mstBuilder.expectedEdgesNum(mstEdges.size());

		for (int v = 0; v < n; v++) {
			int vBuilder = mstBuilder.addVertex();
			assert v == vBuilder;
		}
		double[] mstWeights = new double[mstEdges.size()];
		for (int e : mstEdges) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mstBuilder.addEdge(u, v);
			mstWeights[ne] = w.weight(e);
		}
		IndexGraph mst = mstBuilder.build();
		if (!Trees.isTree(mst))
			return false;

		TreePathMaxima.IQueries queries = TreePathMaxima.IQueries.newInstance();
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u != v)
				queries.addQuery(u, v);
		}
		TreePathMaxima.IResult tpmResults =
				(TreePathMaxima.IResult) tpmAlgo.computeHeaviestEdgeInTreePaths(mst, e -> mstWeights[e], queries);

		int i = 0;
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			if (g.edgeSource(e) == g.edgeTarget(e))
				continue;
			int mstEdge = tpmResults.getHeaviestEdgeInt(i++);
			if (mstEdge == -1 || w.weight(e) < mstWeights[mstEdge])
				return false;
		}
		return true;
	}

}
