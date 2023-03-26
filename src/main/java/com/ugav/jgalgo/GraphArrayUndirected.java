package com.ugav.jgalgo;

public class GraphArrayUndirected extends GraphArrayAbstract implements UGraph {

	private final DataContainer.Obj<int[]> edges;
	private final DataContainer.Int edgesNum;

	public GraphArrayUndirected() {
		this(0);
	}

	public GraphArrayUndirected(int n) {
		super(n, Capabilities);
		edgesNum = new DataContainer.Int(n, 0);
		edges = new DataContainer.Obj<>(n, EmptyIntArr);

		addInternalVerticesDataContainer(edgesNum);
		addInternalVerticesDataContainer(edges);
	}

	@Override
	public EdgeIter edges(int u) {
		checkVertexIdx(u);
		return new EdgeIt(u, edges.get(u), edgesNum.getInt(u));
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edges, edgesNum, u, e);
		if (u != v)
			addEdgeToList(edges, edgesNum, v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edges, edgesNum, u, e);
		removeEdgeFromList(edges, edgesNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int i1 = edgeIndexOf(edges, edgesNum, u1, e1);
		int j1 = edgeIndexOf(edges, edgesNum, v1, e1);
		int i2 = edgeIndexOf(edges, edgesNum, u2, e2);
		int j2 = edgeIndexOf(edges, edgesNum, v2, e2);
		int[] u1es = edges.get(u1), v1es = edges.get(v1);
		int[] u2es = edges.get(u2), v2es = edges.get(v2);
		u1es[i1] = e2;
		v1es[j1] = e2;
		u2es[i2] = e1;
		v2es[j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesAll(int u) {
		checkVertexIdx(u);
		while (edgesNum.getInt(u) > 0)
			removeEdge(edges.get(u)[0]);
	}

	@Override
	public int degree(int u) {
		checkVertexIdx(u);
		return edgesNum.getInt(u);
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++) {
			edges.set(u, EmptyIntArr);
			edgesNum.set(u, 0);
		}
		super.clearEdges();
	}

	private class EdgeIt extends GraphArrayAbstract.EdgeIt {

		private final int u;

		EdgeIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			int u0 = edgeSource(lastEdge);
			int v0 = edgeTarget(lastEdge);
			return u == u0 ? v0 : u0;
		}

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
			return true;
		}

		@Override
		public boolean selfEdges() {
			return true;
		}

		@Override
		public boolean directed() {
			return false;
		}
	};

}
