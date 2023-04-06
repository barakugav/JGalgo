package com.jgalgo;


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
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		UGraph g = (UGraph) g0;
		int n = g.vertices().size();
		if (n == 0)
			return IntLists.emptyList();

		/* sort edges */
		int[] edges = g.edges().toIntArray();
		IntArrays.parallelQuickSort(edges, w);

		/* create union find data structure for each vertex */
		UnionFind uf = new UnionFindArray(n);

		/* iterate over the edges and build the MST */
		IntCollection mst = new IntArrayList(n - 1);
		for (int e : edges) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst.add(e);
			}
		}
		return mst;
	}

}