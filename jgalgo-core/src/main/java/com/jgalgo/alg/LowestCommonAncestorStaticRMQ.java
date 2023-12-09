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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.RMQStatic;
import com.jgalgo.internal.ds.RMQStaticComparator;
import com.jgalgo.internal.util.Assertions;

/**
 * Static LCA implementation using RMQ.
 *
 * <p>
 * By traversing the tree once and assigning for each vertex a number corresponding to its depth, the LCA query is
 * equivalent to a range minimum query. This RMQ problem is a special case of RMQ, as the different between any pair of
 * consecutive elements is always +1/-1, and {@link RMQStaticPlusMinusOne} can be used.
 *
 * <p>
 * The algorithm require preprocessing of \(O(n)\) time and space and answer queries in \(O(1)\) time.
 *
 * <p>
 * Based on 'Fast Algorithms for Finding Nearest Common Ancestors' by D. Harel, R. Tarjan (1984).
 *
 * @author Barak Ugav
 */
class LowestCommonAncestorStaticRMQ extends LowestCommonAncestorStaticAbstract {

	private final RMQStatic rmq;

	/**
	 * Create a new static LCA algorithm object.
	 */
	LowestCommonAncestorStaticRMQ() {
		RMQStatic.Builder rmqBuilder = RMQStatic.builder();
		rmqBuilder.setOption("impl", "plus-minus-one");
		rmq = rmqBuilder.build();
	}

	@Override
	LowestCommonAncestorStatic.IDataStructure preProcessTree(IndexGraph tree, int root) {
		Assertions.Graphs.onlyTree(tree, root);

		final int n = tree.vertices().size();
		int[] depths = new int[n * 2];
		int[] vs = new int[n * 2];
		int[] parent = new int[n];
		IEdgeIter[] edgeIters = new IEdgeIter[n];
		// TODO DFS stack class

		parent[0] = -1;
		edgeIters[0] = tree.outEdges(root).iterator();

		int sequenceLength = 0;
		dfs: for (int u = root, depth = 0;;) {
			depths[sequenceLength] = depth;
			vs[sequenceLength] = u;
			sequenceLength++;

			for (IEdgeIter eit = edgeIters[depth]; eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (v == parent[depth])
					continue;
				depth++;
				parent[depth] = u;
				edgeIters[depth] = tree.outEdges(v).iterator();
				u = v;
				continue dfs;
			}
			u = parent[depth];
			if (--depth < 0)
				break;
		}
		Arrays.fill(edgeIters, 0, n, null);

		depths = Arrays.copyOf(depths, sequenceLength);
		vs = Arrays.copyOf(vs, sequenceLength);

		int[] vToDepthsIdx = new int[n];
		Arrays.fill(vToDepthsIdx, -1);
		for (int i = 0; i < sequenceLength; i++) {
			int v = vs[i];
			if (vToDepthsIdx[v] == -1)
				vToDepthsIdx[v] = i;
		}

		RMQStatic.DataStructure rmqDS = rmq.preProcessSequence(RMQStaticComparator.ofIntArray(depths), depths.length);
		return new DS(n, vs, vToDepthsIdx, rmqDS);
	}

	private static class DS implements LowestCommonAncestorStatic.IDataStructure {

		private final int n;
		private final int[] vs;
		private final int[] vToDepthsIdx;
		private final RMQStatic.DataStructure rmqDS;

		DS(int n, int[] vs, int[] vToDepthsIdx, RMQStatic.DataStructure rmqDS) {
			this.n = n;
			this.vs = vs;
			this.vToDepthsIdx = vToDepthsIdx;
			this.rmqDS = rmqDS;
		}

		@Override
		public int findLca(int u, int v) {
			Assertions.Graphs.checkVertex(u, n);
			Assertions.Graphs.checkVertex(v, n);
			int uIdx = vToDepthsIdx[u];
			int vIdx = vToDepthsIdx[v];
			if (uIdx > vIdx) {
				int temp = uIdx;
				uIdx = vIdx;
				vIdx = temp;
			}
			int lcaIdx = rmqDS.findMinimumInRange(uIdx, vIdx);
			return vs[lcaIdx];
		}
	}

}
