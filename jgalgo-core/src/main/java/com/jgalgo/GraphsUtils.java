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

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphsUtils {

	private GraphsUtils() {}

	static int[] calcDegree(IndexGraph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (int e : edges) {
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	static IndexGraph referenceGraph(IndexGraph g, Object refEdgeWeightKey) {
		final int n = g.vertices().size(), m = g.edges().size();
		IndexGraph gRef = IndexGraph.newBuilderDirected().setDirected(g.getCapabilities().directed())
				.expectedVerticesNum(n).expectedEdgesNum(m).build();
		Weights.Int edgeRef = gRef.addEdgesWeights(refEdgeWeightKey, int.class);

		for (int v = 0; v < n; v++)
			gRef.addVertex();
		for (int e : g.edges()) {
			int eRef = gRef.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(eRef, e);
		}
		return gRef;
	}

	static boolean containsSelfLoops(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		for (int u : g.vertices()) {
			for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				if (u == eit.target())
					return true;
			}
		}
		return false;
	}

	static boolean containsParallelEdges(IndexGraph g) {
		if (!g.getCapabilities().parallelEdges())
			return false;
		int n = g.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

	static class GraphCapabilitiesBuilder {

		private boolean vertexAdd;
		private boolean vertexAddValid;
		private boolean vertexRemove;
		private boolean vertexRemoveValid;
		private boolean edgeAdd;
		private boolean edgeAddValid;
		private boolean edgeRemove;
		private boolean edgeRemoveValid;
		private boolean parallelEdges;
		private boolean parallelEdgesValid;
		private boolean selfEdges;
		private boolean selfEdgesValid;
		private boolean directed;
		private boolean directedValid;

		private GraphCapabilitiesBuilder(boolean directed) {
			this.directed = directed;
			directedValid = true;
		}

		static GraphCapabilitiesBuilder newUndirected() {
			return new GraphCapabilitiesBuilder(false);
		}

		static GraphCapabilitiesBuilder newDirected() {
			return new GraphCapabilitiesBuilder(true);
		}

		GraphCapabilities build() {
			if (!vertexAddValid || !vertexRemoveValid || !edgeAddValid || !edgeRemoveValid || !parallelEdgesValid
					|| !selfEdgesValid || !directedValid)
				throw new IllegalStateException();
			return new GraphCapabilitiesImpl(vertexAdd, vertexRemove, edgeAdd, edgeRemove, parallelEdges, selfEdges,
					directed);
		}

		GraphCapabilitiesBuilder vertexAdd(boolean enable) {
			vertexAdd = enable;
			vertexAddValid = true;
			return this;
		}

		GraphCapabilitiesBuilder vertexRemove(boolean enable) {
			vertexRemove = enable;
			vertexRemoveValid = true;
			return this;
		}

		GraphCapabilitiesBuilder edgeAdd(boolean enable) {
			edgeAdd = enable;
			edgeAddValid = true;
			return this;
		}

		GraphCapabilitiesBuilder edgeRemove(boolean enable) {
			edgeRemove = enable;
			edgeRemoveValid = true;
			return this;
		}

		GraphCapabilitiesBuilder parallelEdges(boolean enable) {
			parallelEdges = enable;
			parallelEdgesValid = true;
			return this;
		}

		GraphCapabilitiesBuilder selfEdges(boolean enable) {
			selfEdges = enable;
			selfEdgesValid = true;
			return this;
		}

		GraphCapabilitiesBuilder directed(boolean enable) {
			directed = enable;
			directedValid = true;
			return this;
		}

	}

	private static class GraphCapabilitiesImpl implements GraphCapabilities {

		private final boolean vertexAdd;
		private final boolean vertexRemove;
		private final boolean edgeAdd;
		private final boolean edgeRemove;
		private final boolean parallelEdges;
		private final boolean selfEdges;
		private final boolean directed;

		GraphCapabilitiesImpl(boolean vertexAdd, boolean vertexRemove, boolean edgeAdd, boolean edgeRemove,
				boolean parallelEdges, boolean selfEdges, boolean directed) {
			this.vertexAdd = vertexAdd;
			this.vertexRemove = vertexRemove;
			this.edgeAdd = edgeAdd;
			this.edgeRemove = edgeRemove;
			this.parallelEdges = parallelEdges;
			this.selfEdges = selfEdges;
			this.directed = directed;
		}

		@Override
		public boolean vertexAdd() {
			return vertexAdd;
		}

		@Override
		public boolean vertexRemove() {
			return vertexRemove;
		}

		@Override
		public boolean edgeAdd() {
			return edgeAdd;
		}

		@Override
		public boolean edgeRemove() {
			return edgeRemove;
		}

		@Override
		public boolean parallelEdges() {
			return parallelEdges;
		}

		@Override
		public boolean selfEdges() {
			return selfEdges;
		}

		@Override
		public boolean directed() {
			return directed;
		}
	}

	static double weightSum(IntIterable collection, WeightFunction w) {
		return weightSum(collection.iterator(), w);
	}

	static double weightSum(IntIterator it, WeightFunction w) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			int cardinality = 0;
			for (; it.hasNext(); it.nextInt())
				cardinality++;
			return cardinality;
		}

		if (w instanceof WeightFunction.Int) {
			WeightFunction.Int w0 = (WeightFunction.Int) w;
			int sum = 0;
			while (it.hasNext())
				sum += w0.weightInt(it.nextInt());
			return sum;

		} else {
			double sum = 0;
			while (it.hasNext())
				sum += w.weight(it.nextInt());
			return sum;
		}
	}

	static final IndexIdMap IndexGraphMapIdentify = new IndexGraphMapIdentify();

	private static class IndexGraphMapIdentify implements IndexIdMap {
		@Override
		public int indexToId(int index) {
			return index;
		}

		@Override
		public int idToIndex(int id) {
			return id;
		}
	}

}
