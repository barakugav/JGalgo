package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm for directed graphs.
 * <p>
 * A spanning tree in directed graph is defined similarly to a spanning tree in
 * undirected graph, but the 'spanning tree' does not yield a strongly connected
 * graph, but a weakly connected tree rooted at some vertex.
 *
 * @author Barak Ugav
 */
public interface MDST extends MST {

	/**
	 * {@inheritDoc}
	 * <p>
	 * The result tree will be rooted at some vertex chosen by the algorithm.
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w);

	/**
	 * Compute a minimum directed spanning tree (MDST) in a directed graph, rooted
	 * at the given vertex
	 *
	 * @param g    a directed graph
	 * @param w    an edge weight function
	 * @param root vertex in the graph the spanning tree will be rooted from
	 * @return all edges composing the spanning tree
	 */
	public IntCollection computeMinimumSpanningTree(DiGraph g, EdgeWeightFunc w, int root);

}
