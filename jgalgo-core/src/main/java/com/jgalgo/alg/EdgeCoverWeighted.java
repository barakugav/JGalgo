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

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;

class EdgeCoverWeighted extends EdgeCovers.AbstractImpl {

	private final MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();

	@Override
	EdgeCover.Result computeMinimumEdgeCover(IndexGraph g, IWeightFunction w) {
		if (w == null)
			w = IWeightFunction.CardinalityWeightFunction;
		final int n = g.vertices().size();
		final int m = g.edges().size();

		IndexGraphBuilder gb = IndexGraphBuilder.newUndirected();
		gb.expectedVerticesNum(n * 2);
		gb.expectedEdgesNum(m * 2 + n);
		for (int v = 0; v < n; v++) {
			gb.addVertex();
			gb.addVertex();
		}
		for (int e = 0; e < m; e++) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			gb.addEdge(u * 2 + 0, v * 2 + 0);
			gb.addEdge(u * 2 + 1, v * 2 + 1);
		}
		final int vvEdgeThreshold = gb.edges().size();
		for (int v = 0; v < n; v++)
			gb.addEdge(v * 2 + 0, v * 2 + 1);
		IndexGraph g2 = gb.build();

		IWeightFunction w2;
		int[] minAdjacentEdge = new int[n];
		Arrays.fill(minAdjacentEdge, -1);
		if (w instanceof IWeightFunctionInt) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			int[] minAdjacentWeight = new int[n];
			Arrays.fill(minAdjacentWeight, Integer.MAX_VALUE);
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int ew = wInt.weightInt(e);
				if (minAdjacentWeight[u] > ew) {
					minAdjacentWeight[u] = ew;
					minAdjacentEdge[u] = e;
				}
				if (minAdjacentWeight[v] > ew) {
					minAdjacentWeight[v] = ew;
					minAdjacentEdge[v] = e;
				}
			}
			for (int v = 0; v < n; v++)
				if (minAdjacentEdge[v] == -1)
					throw new IllegalArgumentException(
							"no edge cover exists, vertex with index " + v + " has no edges");
			IWeightFunctionInt w2Int =
					e -> e < vvEdgeThreshold ? wInt.weightInt(e / 2) : 2 * minAdjacentWeight[e - vvEdgeThreshold];
			w2 = w2Int;

		} else {
			double[] minAdjacentWeight = new double[n];
			Arrays.fill(minAdjacentWeight, Double.POSITIVE_INFINITY);
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double ew = w.weight(e);
				if (minAdjacentWeight[u] > ew) {
					minAdjacentWeight[u] = ew;
					minAdjacentEdge[u] = e;
				}
				if (minAdjacentWeight[v] > ew) {
					minAdjacentWeight[v] = ew;
					minAdjacentEdge[v] = e;
				}
			}
			for (int v = 0; v < n; v++)
				if (minAdjacentEdge[v] == -1)
					throw new IllegalArgumentException(
							"no edge cover exists, vertex with index " + v + " has no edges");
			IWeightFunction w0 = w;
			w2 = e -> e < vvEdgeThreshold ? w0.weight(e / 2) : 2 * minAdjacentWeight[e - vvEdgeThreshold];
		}

		Matching matching = matchingAlgo.computeMinimumWeightedPerfectMatching(g2, w2);
		BitSet cover = new BitSet(m);
		for (int e = 0; e < m; e++)
			if (matching.containsEdge(e * 2 + 0))
				cover.set(e);
		for (int v = 0; v < n; v++)
			if (matching.containsEdge(vvEdgeThreshold + v))
				cover.set(minAdjacentEdge[v]);
		return new EdgeCovers.ResultImpl(g, cover);
	}

}
