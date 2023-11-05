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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class TSPMetricUtils {

	private TSPMetricUtils() {}

	static IPath calcEulerianTourAndConvertToHamiltonianCycle(IndexGraph g, IndexGraph g1, int[] edgeRef) {
		int n = g.vertices().size();

		/* Assert degree is actually even in the new graph */
		assert g.vertices().intStream().allMatch(v -> g1.outEdges(v).size() % 2 == 0);

		/* Calculate Eulerian tour in the new graph */
		IPath tour = (IPath) EulerianTourAlgo.newInstance().computeEulerianTour(g1);
		assert isValidCycle(g1, tour);
		assert isPathVisitEvery(g1, tour);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		int firstVertex = -1, lastVertex = -1;
		Bitmap visited = new Bitmap(n);
		for (IEdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e0 = it.nextInt();
			int e = edgeRef[e0];
			final int u = it.sourceInt();
			if (firstVertex == -1)
				firstVertex = u;
			visited.set(u);
			while (visited.get(it.targetInt()) && it.hasNext()) {
				it.nextInt();
				e = g.getEdge(u, it.targetInt());
			}
			cycle.add(e);
			lastVertex = it.targetInt();
		}

		assert firstVertex == lastVertex;
		IPath cycle0 = new PathImpl(g, firstVertex, lastVertex, cycle);
		assert isValidCycle(g, cycle0);
		assert isPathVisitEvery(g, cycle0);
		return cycle0;
	}

	private static boolean isValidCycle(IndexGraph g, IPath path) {
		IEdgeIter it = path.edgeIter();
		it.nextInt();
		final int begin = it.sourceInt();
		for (;;) {
			if (!it.hasNext())
				return it.targetInt() == begin;
			int lastV = it.targetInt();
			it.nextInt();
			if (lastV != it.sourceInt())
				return false;
		}
	}

	private static boolean isPathVisitEvery(IndexGraph g, IPath path) {
		final int n = g.vertices().size();
		Bitmap visited = new Bitmap(n);
		for (int e : path.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			visited.set(u);
			visited.set(v);
		}
		for (int u = 0; u < n; u++)
			if (!visited.get(u))
				return false;
		return true;
	}

	static abstract class AbstractImpl implements TSPMetric {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Path<V, E> computeShortestTour(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (Path<V, E>) computeShortestTour((IndexGraph) g, w0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IPath indexPath = computeShortestTour(iGraph, iw);
				return PathImpl.pathFromIndexPath(g, indexPath);
			}
		}

		abstract IPath computeShortestTour(IndexGraph g, IWeightFunction w);

	}

}
