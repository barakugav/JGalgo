package com.jgalgo;

public interface LCAStatic {

	/**
	 * Perform a static pre processing of a tree for future LCA (Lowest common
	 * ancestor) queries
	 *
	 * @param t a tree
	 * @param r root of the tree
	 */
	public void preProcessLCA(Graph t, int r);

	/**
	 * Calculate the LCA (Lowest common ancestor) of two vertices
	 *
	 * Can be called only after pre processing of a tree
	 *
	 * @param u first vertex
	 * @param v second vertex
	 * @return the index of the LCA index of the two vertices
	 */
	public int calcLCA(int u, int v);

}