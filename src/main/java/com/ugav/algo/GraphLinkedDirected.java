package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphLinkedDirected extends GraphLinkedAbstract implements DiGraph {

	private final Weights<Node> edgesIn;
	private final Weights<Node> edgesOut;

	public GraphLinkedDirected() {
		this(0);
	}

	public GraphLinkedDirected(int n) {
		this(n, null, null);
	}

	protected GraphLinkedDirected(int n, IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy) {
		super(verticesIDStrategy, edgesIDStrategy);
		edgesIn = new VerticesWeights.Builder(this, null).ofObjs(null);
		/* We use 'edgesIn' to maintain the current vertices in the graph */
		IDStrategy vIDStrategy = getVerticesIDStrategy();
		WeightsAbstract<Node> verticesSet = (WeightsAbstract<Node>) edgesIn;
		verticesSet.forceAdd = true;
		for (int i = 0; i < n; i++) {
			int u = vIDStrategy.nextID(i);
			verticesSet.keyAdd(u);
		}
		addInternalVerticesWeight(edgesIn, false);

		VerticesWeights.Builder vBuilder = new VerticesWeights.Builder(this, () -> vertices().size());
		edgesOut = vBuilder.ofObjs(null);
		addInternalVerticesWeight(edgesOut);
	}

	@Override
	public IntSet vertices() {
		return ((WeightsAbstract<Node>) edgesIn).keysSet();
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItrOut(edgesOut.get(u));
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeVertexItrIn(edgesIn.get(v));
	}

	@Override
	public int addEdge(int u, int v) {
		Node e = (Node) addEdgeNode(u, v);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Node e) {
		int u = e.u, v = e.v;
		Node next;
		next = edgesOut.get(u);
		if (next != null) {
			next.prevOut = e;
			e.nextOut = next;
		}
		edgesOut.set(u, e);
		next = edgesIn.get(v);
		if (next != null) {
			next.prevIn = e;
			e.nextIn = next;
		}
		edgesIn.set(v, e);
	}

	@Override
	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int edge) {
		Node n = (Node) getNode(edge);
		super.removeEdge(edge);
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
	}

	@Override
	public void removeEdgesAllOut(int u) {
		checkVertexIdx(u);
		for (Node p = edgesOut.get(u), next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInNode(p);
			super.removeEdge(p.id);
		}
		edgesOut.set(u, null);
	}

	@Override
	public void removeEdgesAllIn(int v) {
		checkVertexIdx(v);
		for (Node p = edgesIn.get(v), next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutNode(p);
			super.removeEdge(p.id);
		}
		edgesIn.set(v, null);
	}

	private void removeEdgeOutNode(Node e) {
		Node next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut.set(e.u, next);
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
	}

	private void removeEdgeInNode(Node e) {
		Node next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn.set(e.v, next);
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
	}

	@Override
	public void reverseEdge(int e) {
		checkEdgeIdx(e);
		Node n = (Node) getNode(e);
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		int w = n.u;
		n.u = n.v;
		n.v = w;
		addEdgeToLists(n);
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		for (IntIterator it = vertices().iterator(); it.hasNext();) {
			int u = it.nextInt();
			// TODO do some sort of 'addKey' instead of set, no need
			edgesOut.set(u, null);
			edgesIn.set(u, null);
		}
		super.clearEdges();
	}

	private abstract class EdgeVertexItr extends GraphLinkedAbstract.EdgeItr {

		EdgeVertexItr(Node p) {
			super(p);
		}

		@Override
		public int u() {
			return last.u;
		}

		@Override
		public int v() {
			return last.v;
		}

	}

	private class EdgeVertexItrOut extends EdgeVertexItr {

		EdgeVertexItrOut(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextOut;
		}

	}

	private class EdgeVertexItrIn extends EdgeVertexItr {

		EdgeVertexItrIn(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextIn;
		}

	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextOut;
		private Node nextIn;
		private Node prevOut;
		private Node prevIn;

		Node(int id, int u, int v) {
			super(id, u, v);
		}

	}

}
