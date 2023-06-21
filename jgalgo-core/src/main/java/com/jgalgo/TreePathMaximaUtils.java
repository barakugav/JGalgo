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

import java.util.Objects;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;

class TreePathMaximaUtils {

	static abstract class AbstractImpl implements TreePathMaxima {

		@Override
		public TreePathMaxima.Result computeHeaviestEdgeInTreePaths(Graph tree, WeightFunction w,
				TreePathMaxima.Queries queries) {
			if (tree instanceof IndexGraph)
				return computeHeaviestEdgeInTreePaths((IndexGraph) tree, w, queries);

			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap viMap = tree.indexGraphVerticesMap();
			IndexIdMap eiMap = tree.indexGraphEdgesMap();
			w = WeightsImpl.indexWeightFuncFromIdWeightFunc(w, eiMap);
			queries = new IndexQueriesFromQueries(queries, viMap);

			TreePathMaxima.Result indexResult = computeHeaviestEdgeInTreePaths(iGraph, w, queries);
			return new ResultFromIndexResult(indexResult, eiMap);
		}

		abstract TreePathMaxima.Result computeHeaviestEdgeInTreePaths(IndexGraph tree, WeightFunction w,
				TreePathMaxima.Queries queries);

	}

	static class QueriesImpl implements TreePathMaxima.Queries {
		private final IntList qs;

		QueriesImpl() {
			qs = new IntArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(u);
			qs.add(v);
		}

		@Override
		public IntIntPair getQuery(int idx) {
			return IntIntPair.of(qs.getInt(idx * 2), qs.getInt(idx * 2 + 1));
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

	static class ResultImpl implements TreePathMaxima.Result {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
		}

		@Override
		public int getHeaviestEdge(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

	static class IndexQueriesFromQueries implements TreePathMaxima.Queries {
		private final TreePathMaxima.Queries qs;
		private final IndexIdMap viMap;

		IndexQueriesFromQueries(TreePathMaxima.Queries qs, IndexIdMap viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToId(u), viMap.indexToId(v));
		}

		@Override
		public IntIntPair getQuery(int idx) {
			IntIntPair idQuery = qs.getQuery(idx);
			return IntIntPair.of(viMap.idToIndex(idQuery.firstInt()), viMap.idToIndex(idQuery.secondInt()));
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

	private static class ResultFromIndexResult implements TreePathMaxima.Result {

		private final TreePathMaxima.Result res;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(TreePathMaxima.Result res, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int getHeaviestEdge(int queryIdx) {
			int eIdx = res.getHeaviestEdge(queryIdx);
			return eIdx == -1 ? -1 : eiMap.indexToId(eIdx);
		}

		@Override
		public int size() {
			return res.size();
		}
	}

	static boolean verifyMST(IndexGraph g, WeightFunction w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		ArgumentCheck.onlyUndirected(g);
		int n = g.vertices().size();
		GraphBuilderFixedUnmapped mstBuilder = GraphBuilderFixedUnmapped.newUndirected();
		for (int v = 0; v < n; v++) {
			int vFixed = mstBuilder.addVertex();
			assert v == vFixed;
		}
		int[] edgeRef = new int[mstEdges.size()];
		for (int e : mstEdges) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mstBuilder.addEdge(u, v);
			edgeRef[ne] = e;
		}
		IndexGraph mst = mstBuilder.build();
		if (!Trees.isTree(mst))
			return false;

		TreePathMaxima.Queries queries = TreePathMaxima.Queries.newInstance();
		for (int e : g.edges())
			queries.addQuery(g.edgeSource(e), g.edgeTarget(e));
		WeightFunction w0 = e -> w.weight(edgeRef[e]);
		TreePathMaxima.Result tpmResults = tpmAlgo.computeHeaviestEdgeInTreePaths(mst, w0, queries);

		int i = 0;
		for (int e : g.edges()) {
			int mstEdge = tpmResults.getHeaviestEdge(i++);
			if (mstEdge == -1 || w.weight(e) < w0.weight(mstEdge))
				return false;
		}
		return true;
	}

}
