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

import com.jgalgo.internal.util.Assertions;

abstract class GraphLinkedAbstract extends GraphBaseIndexMutable {

	private Edge[] edges;
	private final DataContainer.Obj<Edge> edgesContainer;
	private static final Edge[] EmptyEdgeArr = new Edge[0];

	GraphLinkedAbstract(IndexGraphBase.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(super.edges, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalEdgesContainer(edgesContainer);
	}

	GraphLinkedAbstract(IndexGraphBase.Capabilities capabilities, IndexGraph g, boolean copyWeights) {
		super(capabilities, g, copyWeights);
		edgesContainer = new DataContainer.Obj<>(super.edges, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalEdgesContainer(edgesContainer);
		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			edges[e] = allocEdge(e, g.edgeSource(e), g.edgeTarget(e));
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		Edge n = getEdge(edge);
		if (endpoint == n.source) {
			return n.target;
		} else if (endpoint == n.target) {
			return n.source;
		} else {
			throw new IllegalArgumentException("The given vertex (idx=" + endpoint
					+ ") is not an endpoint of the edge (idx=" + n.source + ", idx=" + n.target + ")");
		}
	}

	Edge getEdge(int edge) {
		Edge n = edges[edge];
		assert n.id == edge;
		return n;
	}

	@Override
	void removeEdgeImpl(int edge) {
		edgesContainer.clear(edges, edge);
		super.removeEdgeImpl(edge);
	}

	Edge addEdgeObj(int source, int target) {
		int e = super.addEdge(source, target);
		Edge n = allocEdge(e, source, target);
		edges[e] = n;
		return n;
	}

	abstract Edge allocEdge(int id, int source, int target);

	@Override
	void edgeSwap(int e1, int e2) {
		Edge n1 = getEdge(e1), n2 = getEdge(e2);
		n1.id = e2;
		n2.id = e1;
		edgesContainer.swap(edges, e1, e2);
		super.edgeSwap(e1, e2);
	}

	@Override
	public int edgeSource(int edge) {
		checkEdge(edge);
		return getEdge(edge).source;
	}

	@Override
	public int edgeTarget(int edge) {
		checkEdge(edge);
		return getEdge(edge).target;
	}

	@Override
	public void clearEdges() {
		edgesContainer.clear(edges);
		super.clearEdges();
	}

	abstract class EdgeItr implements EdgeIter {

		private Edge next;
		Edge last;

		EdgeItr(Edge p) {
			this.next = p;
		}

		abstract Edge nextEdge(Edge n);

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			next = nextEdge(last = next);
			return last.id;
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return next.id;
		}

		@Override
		public void remove() {
			if (last == null)
				throw new IllegalStateException();
			removeEdge(last.id);
			last = null;
		}
	}

	abstract static class Edge {

		int id;
		int source, target;

		Edge(int id, int source, int target) {
			this.id = id;
			this.source = source;
			this.target = target;
		}

	}

}