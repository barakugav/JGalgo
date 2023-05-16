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

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * A directed graph implementation using a two dimensional table to store all edges.
 * <p>
 * If the graph contains \(n\) vertices, table of size {@code [n][n]} stores the edges of the graph. The implementation
 * does not support multiple edges with identical source and target.
 * <p>
 * This implementation is efficient for use cases where fast lookups of edge \((u,v)\) are required, as they can be
 * answered in \(O(1)\) time, but it should not be the default choice for a directed graph.
 *
 * @see    GraphTableUndirected
 * @author Barak Ugav
 */
class GraphTableDirected extends GraphTableAbstract {

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphTableDirected(int n) {
		super(n, Capabilities);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		return new EdgeIterIn(v);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges.get(u).set(v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		edges.get(u).set(v, EdgeNone);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges.get(u1).set(v1, e2);
		edges.get(u2).set(v2, e1);
		super.edgeSwap(e1, e2);
	}

	@Override
	public void clearEdges() {
		final int m = edges().size();
		for (int e = 0; e < m; e++) {
			int u = edgeSource(e), v = edgeTarget(e);
			edges.get(u).set(v, EdgeNone);
		}
		super.clearEdges();
	}

	@Override
	public void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		if (edges.get(v).getInt(u) != EdgeNone && u != v)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges.get(u).set(v, EdgeNone);
		edges.get(v).set(u, e);
		super.reverseEdge0(e);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		for (IntIterator eit1 = edgesOut(v1); eit1.hasNext();)
			replaceEdgeSource(eit1.nextInt(), v2);
		for (IntIterator eit1 = edgesOut(v2); eit1.hasNext();)
			replaceEdgeSource(eit1.nextInt(), v1);
		for (IntIterator eit1 = edgesIn(v1); eit1.hasNext();)
			replaceEdgeTarget(eit1.nextInt(), v2);
		for (IntIterator eit1 = edgesIn(v2); eit1.hasNext();)
			replaceEdgeTarget(eit1.nextInt(), v1);
		super.vertexSwap(v1, v2);
	}

	private static final GraphCapabilities Capabilities = new GraphCapabilities() {
		@Override
		public boolean vertexAdd() {
			return true;
		}

		@Override
		public boolean vertexRemove() {
			return true;
		}

		@Override
		public boolean edgeAdd() {
			return true;
		}

		@Override
		public boolean edgeRemove() {
			return true;
		}

		@Override
		public boolean parallelEdges() {
			return false;
		}

		@Override
		public boolean selfEdges() {
			return true;
		}

		@Override
		public boolean directed() {
			return true;
		}
	};

}
