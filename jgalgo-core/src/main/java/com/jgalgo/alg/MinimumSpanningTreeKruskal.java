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

import java.util.Objects;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.ds.UnionFind;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Kruskal's minimum spanning tree algorithm.
 *
 * <p>
 * The algorithm first sort all the edges of the graph by their weight, and then examine them in increasing weight
 * order. For each examined edge, if it connects two connected components that were not connected beforehand, the edge
 * is added to the forest. The algorithm terminate after all edges were examined.
 *
 * <p>
 * The running time of the algorithm is \(O(m \log n)\) and it uses linear time. This algorithm perform good in practice
 * and its running time compete with other algorithms such as {@link MinimumSpanningTreePrim}, which have better time
 * bounds in theory. Note that only undirected graphs are supported.
 *
 * <p>
 * Based on "On the shortest spanning subtree of a graph and the traveling salesman problem" by Kruskal, J. B. (1956) in
 * the book "Proceedings of the American Mathematical Society".
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Kruskal%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MinimumSpanningTreeKruskal implements MinimumSpanningTreeBase {

	private UnionFind.Builder unionFindBuilder = UnionFind.builder();
	private boolean parallelEnable = JGAlgoConfigImpl.ParallelByDefault;

	/**
	 * Construct a new MST algorithm object.
	 */
	MinimumSpanningTreeKruskal() {}

	/**
	 * [experimental API] Set the implementation of {@link UnionFind} used by this algorithm.
	 *
	 * @param builder a builder function that accept a number of elements \(n\) and create a {@link UnionFind} with IDs
	 *                    {@code 0,1,2,...,n-1}.
	 */
	void setUnionFindBuilder(UnionFind.Builder builder) {
		unionFindBuilder = Objects.requireNonNull(builder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	public MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		final int n = g.vertices().size();
		final int m = g.edges().size();
		if (n == 0 || m == 0)
			return MinimumSpanningTrees.IndexResult.Empty;

		/* sort edges */
		int[] edges = g.edges().toIntArray();
		JGAlgoUtils.sort(edges, 0, m, w, parallelEnable);

		/* create union find data structure for each vertex */
		UnionFind uf = unionFindBuilder.expectedSize(n).build();
		uf.makeMany(n);

		/* iterate over the edges and build the MST */
		IntArrayList mst = new IntArrayList(n - 1);
		for (int e : edges) {
			int U = uf.find(g.edgeSource(e));
			int V = uf.find(g.edgeTarget(e));

			if (U != V) {
				uf.union(U, V);
				mst.add(e);
			}
		}
		uf.clear();
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}

}
