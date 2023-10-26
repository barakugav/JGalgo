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

package com.jgalgo.graph;

/**
 * An undirected graph implementation using linked lists to store edge lists.
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphLinkedDirected
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
class GraphLinkedUndirected extends GraphLinkedAbstract {

	private Edge[] edges;
	private final DataContainer.Obj<Edge> edgesContainer;

	private static final Edge[] EmptyEdgeArr = new Edge[0];

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(false, true, true);

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalVerticesContainer(edgesContainer);
	}

	GraphLinkedUndirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);

		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalVerticesContainer(edgesContainer);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesContainer.clear(edges, vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		assert v1 != v2;

		final int tempV = -2;
		for (Edge p = edges[v1], next; p != null; p = next) {
			next = p.next(v1);
			if (p.source == v1)
				p.source = tempV;
			if (p.target == v1)
				p.target = tempV;
		}
		for (Edge p = edges[v2], next; p != null; p = next) {
			next = p.next(v2);
			if (p.source == v2)
				p.source = v1;
			if (p.target == v2)
				p.target = v1;
		}
		for (Edge p = edges[v1], next; p != null; p = next) {
			next = p.next(tempV);
			if (p.source == tempV)
				p.source = v2;
			if (p.target == tempV)
				p.target = v2;
		}

		edgesContainer.swap(edges, v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	@Override
	public int addEdge(int source, int target) {
		Edge e = (Edge) addEdgeObj(source, target);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Edge e) {
		int source = e.source, target = e.target;
		Edge next;
		if ((next = edges[source]) != null) {
			e.nextSet(source, next);
			next.prevSet(source, e);
		}
		edges[source] = e;
		if (source != target) {
			if ((next = edges[target]) != null) {
				e.nextSet(target, next);
				next.prevSet(target, e);
			}
			edges[target] = e;
		}
	}

	@Override
	Edge allocEdge(int id, int source, int target) {
		return new Edge(id, source, target);
	}

	@Override
	Edge getEdge(int edge) {
		return (Edge) super.getEdge(edge);
	}

	@Override
	void removeEdgeImpl(int edge) {
		Edge e = getEdge(edge);
		removeEdgePointers(e, e.source);
		if (e.source != e.target)
			removeEdgePointers(e, e.target);
		super.removeEdgeImpl(edge);
	}

	private void removeEdgePointers(Edge e, int w) {
		Edge next = e.next(w), prev = e.prev(w);
		if (prev == null) {
			edges[w] = next;
		} else {
			prev.nextSet(w, next);
			e.prevSet(w, null);
		}
		if (next != null) {
			next.prevSet(w, prev);
			e.nextSet(w, null);
		}
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (Edge p = edges[source], next; p != null; p = next) {
			// update u list
			next = p.next(source);
			p.nextSet(source, null);
			p.prevSet(source, null);

			// update v list
			if (p.source != p.target) {
				int target = p.getEndpoint(source);
				removeEdgePointers(p, target);
			}

			edgeSwapBeforeRemove(p.id);
			super.removeEdgeImpl(p.id);
		}
		edges[source] = null;
	}

	@Override
	public void removeOutEdgesOf(int source) {
		removeEdgesOf(source);
	}

	@Override
	public void removeInEdgesOf(int target) {
		removeEdgesOf(target);
	}

	@Override
	public void reverseEdge(int edge) {
		// Do nothing
	}

	@Override
	public void clearEdges() {
		for (int m = edges().size(), e = 0; e < m; e++) {
			Edge p = getEdge(e);
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		edgesContainer.clear(edges);
		super.clearEdges();
	}

	private static class Edge extends GraphLinkedAbstract.Edge {

		private Edge nextu, nextv;
		private Edge prevu, prevv;

		Edge(int id, int source, int target) {
			super(id, source, target);
		}

		Edge next(int w) {
			assert w == source || w == target;
			return w == source ? nextu : nextv;
		}

		void nextSet(int w, Edge n) {
			if (w == source) {
				nextu = n;
			} else {
				assert w == target;
				nextv = n;
			}
		}

		Edge prev(int w) {
			if (w == source) {
				return prevu;
			} else {
				assert w == target;
				return prevv;
			}
		}

		void prevSet(int w, Edge n) {
			if (w == source) {
				prevu = n;
			} else {
				assert w == target;
				prevv = n;
			}
		}

		int getEndpoint(int w) {
			if (w == source) {
				return target;
			} else {
				assert w == target;
				return source;
			}
		}
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edges[source]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edges[target]);
		}
	}

	private class EdgeIterOut extends GraphLinkedAbstract.EdgeItr {

		private final int source;

		EdgeIterOut(int source, Edge p) {
			super(p);
			this.source = source;
		}

		@Override
		Edge nextEdge(GraphLinkedAbstract.Edge n) {
			return ((Edge) n).next(source);
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			int u0 = last.source, v0 = last.target;
			return source == u0 ? v0 : u0;
		}
	}

	private class EdgeIterIn extends GraphLinkedAbstract.EdgeItr {

		private final int target;

		EdgeIterIn(int target, Edge p) {
			super(p);
			this.target = target;
		}

		@Override
		Edge nextEdge(GraphLinkedAbstract.Edge n) {
			return ((Edge) n).next(target);
		}

		@Override
		public int source() {
			int u0 = last.source, v0 = last.target;
			return target == u0 ? v0 : u0;
		}

		@Override
		public int target() {
			return target;
		}
	}
}