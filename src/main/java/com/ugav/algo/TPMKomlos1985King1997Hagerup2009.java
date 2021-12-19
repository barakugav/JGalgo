package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class TPMKomlos1985King1997Hagerup2009 implements TPM {

	private TPMKomlos1985King1997Hagerup2009() {
	}

	private static final TPMKomlos1985King1997Hagerup2009 INSTANCE = new TPMKomlos1985King1997Hagerup2009();

	public static TPMKomlos1985King1997Hagerup2009 getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> Edge<E>[] calcTPM(Graph<E> t, WeightFunction<E> w, int[] queries) {
		if (t.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		if (queries.length % 2 != 0)
			throw new IllegalArgumentException("queries should be in format [u0, v0, u1, v1, ...]");
		if (!Graphs.isTree(t))
			throw new IllegalArgumentException("only trees are supported");
		int queriesNum = queries.length / 2;
		@SuppressWarnings("unchecked")
		Edge<E>[] res = new Edge[queriesNum];
		if (t.vertices() == 0)
			return res;

		Tuple<Graph<Ref<E>>, Integer> r = buildBoruvkaFullyBranchingTree(t, w);
		Graph<Ref<E>> t0 = r.e1;
		int root = r.e2;

		int[] lcaQueries = splitQueriesIntoLCAQueries(t0, root, queries);

		Tuple<Edge<Ref<E>>[], int[]> r2 = getEdgeToParentsAndDepth(t0, root);
		Edge<Ref<E>>[] edgeToParent = r2.e1;
		int[] depths = r2.e2;

		int[] q = calcQueriesPerVertex(lcaQueries, depths, edgeToParent);
		Edge<Ref<E>>[][] a = calcAnswersPerVertex(t0, root, q, edgeToParent, t.vertices());
		return extractEdgesFromAnswers(a, q, lcaQueries, depths);
	}

	private static <E> Edge<E>[] extractEdgesFromAnswers(Edge<Ref<E>>[][] a, int[] q, int[] lcaQueries, int[] depths) {
		int queriesNum = lcaQueries.length / 4;
		@SuppressWarnings("unchecked")
		Edge<E>[] res = new Edge[queriesNum];

		for (int i = 0; i < queriesNum; i++) {
			int u = lcaQueries[i * 4];
			int v = lcaQueries[i * 4 + 2];
			int lca = lcaQueries[i * 4 + 1];
			int lcaDepth = depths[lca];

			Edge<Ref<E>> ua = null, va = null;

			int qusize = Integer.bitCount(q[u]);
			for (int j = 0; j < qusize; j++) {
				if (getIthOneBit(q[u], j) == lcaDepth) {
					ua = a[u][j];
					break;
				}
			}
			int qvsize = Integer.bitCount(q[v]);
			for (int j = 0; j < qvsize; j++) {
				if (getIthOneBit(q[v], j) == lcaDepth) {
					va = a[v][j];
					break;
				}
			}

			res[i] = (va == null || (ua != null && ua.val().w >= va.val().w)) ? (ua != null ? ua.val().orig : null)
					: (va != null ? va.val().orig : null);
		}

		return res;
	}

	private static <E> Edge<Ref<E>>[][] calcAnswersPerVertex(Graph<Ref<E>> t, int root, int[] q,
			Edge<Ref<E>>[] edgeToParent, int leavesNum) {
		int n = t.vertices();
		int[] a = new int[n];

		int leavesDepth = Graphs.getFullyBranchingTreeDepth(t, root);

		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[][] res = new Edge[leavesNum][];

		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edges = new Edge[n * 2];
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edgesToRoot = new Edge[leavesDepth];

		int[] edgesOffset = new int[n];
		int[] edgesCount = new int[n];
		int[] edgesIdx = new int[n];

		edgesOffset[0] = 0;
		edgesCount[0] = t.getEdgesArr(root, edges, 0);
		edgesIdx[0] = 0;

		for (int depth = 0; depth >= 0;) {
			// find next child from end of DFS path
			Edge<Ref<E>> edgeToChild = null;
			for (int i = edgesIdx[depth]; i < edgesCount[depth]; i++) {
				Edge<Ref<E>> e = edges[edgesOffset[depth] + i];
				if (e != edgeToParent[e.u()]) {
					edgeToChild = edges[edgesOffset[depth] + i];
					edgesIdx[depth] = i + 1;
					break;
				}
			}

			if (edgeToChild != null) {
				int u = edgeToChild.u(); // parent
				int v = edgeToChild.v(); // child

				a[v] = subseq(a[u], q[u], q[v]);
				int j = binarySearch(a[v], edgeToChild.val().w, edgesToRoot);
				a[v] = repSuf(a[v], depth + 1, j);

				edgesToRoot[depth] = edgeToChild;
				if (depth + 1 != leavesDepth) {
					// add vertex to end of DFS path
					depth++;
					edgesOffset[depth] = edgesOffset[depth - 1] + edgesCount[depth - 1];
					edgesCount[depth] = t.getEdgesArr(v, edges, edgesOffset[depth]);
					edgesIdx[depth] = 0;
				} else {
					// TODO
					int qvsize = Integer.bitCount(q[v]);
					@SuppressWarnings("unchecked")
					Edge<Ref<E>>[] resv = new Edge[qvsize];
					for (int i = 0; i < qvsize; i++) {
						int b = getIthOneBit(q[v], i);
						int s = Integer.numberOfTrailingZeros(successor(a[v], 1 << b));
						resv[i] = edgesToRoot[s - 1];
					}
					res[v] = resv;

					edgesToRoot[depth] = null;
				}
			} else {
				// return to previous vertex in DFS path
				if (edgeToParent[depth] == null)
					break;
				depth--;
			}
		}
		return res;
	}

	private static int successor(int a, int b) {
//		int r = 0, bsize = Integer.bitCount(b);
//		for (int i = 0; i < bsize; i++)
//			for (int bit = getIthOneBit(b, i) + 1; bit < Integer.SIZE; bit++)
//				if ((a & (1 << bit)) != 0) {
//					r |= 1 << bit;
//					break;
//				}
//		return r;

		/*
		 * Don't even ask why the commented code above is equivalent to the bit tricks
		 * below. Hagerup 2009.
		 */
		return a & (~(a | b) ^ ((~a | b) + b));
	}

	private static int getIthOneBit(int x, int b) {
		if (b < 0 || b >= Integer.bitCount(x))
			throw new IndexOutOfBoundsException(Integer.toBinaryString(x) + "[" + b + "]");

		while (true) {
			int nextBit = Integer.numberOfTrailingZeros(x);
			if (b-- == 0)
				return nextBit;
			x &= ~(1 << nextBit);
		}
	}

	private static int subseq(int au, int qu, int qv) {
		return successor(au, qv);
	}

	private static <E> int binarySearch(int av, double w, Edge<Ref<E>>[] edgesToRoot) {
		// TODO
		int avsize = Integer.bitCount(av);
		for (int i = avsize - 1; i >= 0; i--) {
			int avi = getIthOneBit(av, i);
			if (edgesToRoot[avi - 1].val().w >= w)
				return avi + 1;
		}
		return 0;
	}

	private static int repSuf(int av, int depth, int j) {
		av &= (1 << j) - 1;
		av |= 1 << depth;
		return av;
	}

	private static <E> Tuple<Graph<Ref<E>>, Integer> buildBoruvkaFullyBranchingTree(Graph<E> g, WeightFunction<E> w) {
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] minEdges = new Edge[n];
		double[] minEdgesWeight = new double[n];
		int[] vNext = new int[n];
		int[] path = new int[n];
		int[] vTv = new int[n];
		int[] vTvNext = new int[n];

		for (int v = 0; v < n; v++)
			vTv[v] = v;

		Graph.Modifiable<Ref<E>> t = new GraphArray<>(DirectedType.Undirected, n);
		for (Graph<Ref<E>> G = createRefGraph(g, w); (n = G.vertices()) > 1;) {

			// Find minimum edge of each vertex
			Arrays.fill(minEdges, 0, n, null);
			Arrays.fill(minEdgesWeight, 0, n, Double.MAX_VALUE);
			for (int u = 0; u < n; u++) {
				for (Iterator<Edge<Ref<E>>> it = G.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();

					double eWeight = e.val().w;
					if (eWeight < minEdgesWeight[u]) {
						minEdges[u] = e;
						minEdgesWeight[u] = eWeight;
					}
				}
			}

			// find connectivity components, and label each vertex with new super vertex
			final int UNVISITED = -1;
			final int IN_PATH = -2;
			Arrays.fill(vNext, 0, n, UNVISITED);
			int nNext = 0;
			for (int u = 0; u < n; u++) {
				int pathLength = 0;
				// find all reachable vertices from u
				for (int p = u;;) {
					if (vNext[p] == UNVISITED) {
						// another vertex on the path, continue
						path[pathLength++] = p;
						vNext[p] = IN_PATH;

						p = minEdges[p].v();
						continue;
					}

					// if found label use it label, else - add new label
					int V = vNext[p] >= 0 ? vNext[p] : nNext++;
					// assign the new label to all trees on path
					while (pathLength-- > 0)
						vNext[path[pathLength]] = V;
					break;
				}
			}

			// construct new layer in the output tree graph
			for (int V = 0; V < nNext; V++)
				vTvNext[V] = t.newVertex();
			for (int u = 0; u < n; u++)
				t.addEdge(vTv[u], vTvNext[vNext[u]]).val(minEdges[u].val());
			int[] temp = vTv;
			vTv = vTvNext;
			vTvNext = temp;

			// contract G to new graph with the super vertices
			Graph.Modifiable<Ref<E>> gNext = new GraphArray<>(DirectedType.Undirected, nNext);
			for (int u = 0; u < n; u++) {
				int U = vNext[u];
				for (Iterator<Edge<Ref<E>>> it = G.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();
					int V = vNext[e.v()];
					if (U != V)
						gNext.addEdge(U, V).val(e.val());
				}
			}

			@SuppressWarnings("rawtypes")
			Graph.Modifiable tempg = ((Graph.Modifiable) G);
			tempg.clear();
			G = gNext;
		}
		return new Tuple<>(t, vTv[0]);
	}

	private static <E> int[] splitQueriesIntoLCAQueries(Graph<Ref<E>> t, int root, int[] queries) {
		int queriesNum = queries.length / 2;
		int[] lcaQueries = new int[queriesNum * 4];

		LCA lcaAlgo = LCARMQBenderFarachColton2000.getInstace();
		LCA.Result lcaRes = lcaAlgo.preprocessLCA(t, root);
		for (int q = 0; q < queriesNum; q++) {
			int u = queries[q * 2], v = queries[q * 2 + 1];
			int lca = lcaRes.query(u, v);
			lcaQueries[q * 4] = u;
			lcaQueries[q * 4 + 1] = lca;
			lcaQueries[q * 4 + 2] = v;
			lcaQueries[q * 4 + 3] = lca;
		}
		return lcaQueries;
	}

	private static <E> Tuple<Edge<Ref<E>>[], int[]> getEdgeToParentsAndDepth(Graph<Ref<E>> t, int root) {
		int n = t.vertices();
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] edgeToParent = new Edge[n];
		int[] depths = new int[n];

		int[] layer = new int[n];
		int[] layerNext = new int[n];
		int layerSize = 0;

		layer[layerSize++] = root;

		for (int depth = 0; layerSize > 0; depth++) {
			int layerSizeNext = 0;

			for (int u; layerSize > 0;) {
				u = layer[--layerSize];
				depths[u] = depth;

				Edge<Ref<E>> ep = edgeToParent[u];
				int parent = ep != null ? ep.v() : -1;

				for (Iterator<Edge<Ref<E>>> it = t.edges(u); it.hasNext();) {
					Edge<Ref<E>> e = it.next();
					int v = e.v();
					if (v == parent)
						continue;
					edgeToParent[v] = e.twin();
					layerNext[layerSizeNext++] = v;
				}
			}

			int[] temp = layer;
			layer = layerNext;
			layerNext = temp;
			layerSize = layerSizeNext;
		}
		return new Tuple<>(edgeToParent, depths);
	}

	private static <E> int[] calcQueriesPerVertex(int[] lcaQueries, int[] depths, Edge<Ref<E>>[] edgeToParent) {
		int n = edgeToParent.length;
		int[] layer = new int[n];
		int[] layerNext = new int[n];
		int layerSize = 0;

		int[] q = new int[n];
		Arrays.fill(q, 0);

		int queriesNum = lcaQueries.length / 2;
		for (int query = 0; query < queriesNum; query++) {
			int u = lcaQueries[query * 2];
			int ancestor = lcaQueries[query * 2 + 1];
			if (u == ancestor)
				continue;
			q[u] |= 1 << depths[ancestor];
		}

		for (int u = 0; u < n; u++)
			layer[layerSize++] = u;

		while (layerSize > 0) {
			int layerSizeNext = 0;

			for (int u; layerSize > 0;) {
				u = layer[--layerSize];

				Edge<Ref<E>> ep = edgeToParent[u];
				if (ep == null)
					continue;
				int parent = ep.v();
				q[parent] |= q[u] & ~(1 << depths[parent]);

				layerNext[layerSizeNext++] = parent;
			}

			int[] temp = layer;
			layer = layerNext;
			layerNext = temp;
			layerSize = layerSizeNext;
		}

		return q;
	}

	private static class Ref<E> {

		final Edge<E> orig;
		final double w;

		Ref(Edge<E> e, double w) {
			orig = e;
			this.w = w;
		}

		public int hashCode() {
			return orig.hashCode();
		}

		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref<?> o = (Ref<?>) other;
			return orig.equals(o.orig);
		}

		public String toString() {
			return "R(" + orig + ")";
		}

	}

	private static <E> Graph<Ref<E>> createRefGraph(Graph<E> g, WeightFunction<E> w) {
		Graph.Modifiable<Ref<E>> g0 = GraphLinked.builder().setDirected(false).setVertexNum(g.vertices()).build();
		for (Iterator<Edge<E>> it = g.edges(); it.hasNext();) {
			Edge<E> e = it.next();
			Ref<E> v = new Ref<>(e, w.weight(e));
			g0.addEdge(e.u(), e.v()).val(v);
		}
		return g0;
	}

}
