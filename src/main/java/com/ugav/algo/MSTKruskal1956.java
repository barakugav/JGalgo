package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graphs.EdgeWeightComparator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MSTKruskal1956 implements MST {

	/*
	 * O(m log n)
	 */

	public MSTKruskal1956() {
	}

	@Override
	public IntCollection calcMST(Graph<?> g0, WeightFunction w) {
		if (!(g0 instanceof Graph.Undirected<?>))
			throw new IllegalArgumentException("only undirected graphs are supported");
		Graph.Undirected<?> g = (Graph.Undirected<?>) g0;
		int n = g.vertices();
		if (n == 0)
			return IntLists.emptyList();

		/* sort edges */
		int m = g.edges();
		int[] edges = new int[m];
		for (int e = 0; e < m; e++)
			edges[e] = e;
		IntArrays.parallelQuickSort(edges, new EdgeWeightComparator(w));

		/* create union find data structure for each vertex */
		UnionFind uf = new UnionFindArray(n);

		/* iterate over the edges and build the MST */
		IntCollection mst = new IntArrayList(n - 1);
		for (int e = 0; e < m; e++) {
			int u = g.getEdgeSource(e);
			int v = g.getEdgeTarget(e);

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst.add(e);
			}
		}
		return mst;
	}

}
