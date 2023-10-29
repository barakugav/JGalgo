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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

class StronglyConnectedComponentsAlgoTarjan extends ConnectedComponentsUtils.AbstractStronglyConnectedComponentsAlgo {

	@Override
	VertexPartition findStronglyConnectedComponentsDirected(IndexGraph g) {
		final int n = g.vertices().size();
		IntStack s = new IntArrayList();
		IntStack p = new IntArrayList();
		int[] dfsPath = new int[n];
		int[] c = new int[n];
		IEdgeIter[] edges = new IEdgeIter[n];
		// TODO DFS stack class

		// implementation of Tarjan's strongly connected components algorithm
		// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		Arrays.fill(c, 0);
		int cNext = 1;

		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;
			dfsPath[0] = root;
			edges[0] = g.outEdges(root).iterator();
			c[root] = cNext++;
			s.push(root);
			p.push(root);

			dfs: for (int depth = 0;;) {
				for (IEdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (c[v] == 0) {
						c[v] = cNext++;
						s.push(v);
						p.push(v);

						dfsPath[++depth] = v;
						edges[depth] = g.outEdges(v).iterator();
						continue dfs;
					} else if (comp[v] == -1)
						while (c[p.topInt()] > c[v])
							p.popInt();
				}
				int u = dfsPath[depth];
				if (p.topInt() == u) {
					int v;
					do {
						v = s.popInt();
						comp[v] = compNum;
					} while (v != u);
					compNum++;
					p.popInt();
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return new VertexPartitions.Impl(g, compNum, comp);
	}

	@Override
	boolean isStronglyConnected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g).numberOfBlocks() <= 1;
	}

}
