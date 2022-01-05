package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.Edge;

public class MatchingGabow1976 implements Matching {

	private MatchingGabow1976() {
	}

	private static final MatchingGabow1976 INSTANCE = new MatchingGabow1976();

	public static MatchingGabow1976 getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g) {
		if (g.isDirected())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices();

		int[] queue = new int[n];
		int[] tree = new int[n];
		int[] root = new int[n];
		boolean[] isEven = new boolean[n];

		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] bridge = new Edge[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] parent = new Edge[n]; // vertex -> edge

		@SuppressWarnings("unchecked")
		Edge<E>[] augPath = new Edge[n];
		boolean[] setmatch = new boolean[n];

		int[] blossomBaseSearchNotes = new int[n];
		int blossomBaseSearchNotesIndex = 0;
		int[] blossomVertices = new int[n];

		UnionFind uf = UnionFindImpl.getInstance();
		@SuppressWarnings("unchecked")
		UnionFind.Elm<Integer>[] ufElms = new UnionFind.Elm[n];

		while (true) {
			int treeNum = 0;
			Arrays.fill(tree, -1);
			Arrays.fill(isEven, false);

			for (int u = 0; u < n; u++)
				ufElms[u] = uf.make(u);

			int augPathSize = 0;

			int queueBegin = 0, queueEnd = 0;
			for (int u = 0; u < n; u++) {
				if (matched[u] != null)
					continue;
				root[tree[u] = treeNum++] = u;
				isEven[u] = true;
				queue[queueEnd++] = u;

			}
			bfs: while (queueBegin != queueEnd) {
				int u = queue[queueBegin++];
				int uTree = tree[u];
				int uRoot = root[uTree];

				for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
					Edge<E> e = it.next();
					int v = e.v();
					int vTree = tree[v];

					if (vTree == -1) {
						// unexplored vertex, add to tree
						Edge<E> matchedEdge = matched[v];
						tree[v] = uTree;
						parent[v] = e.twin();

						v = matchedEdge.v();
						tree[v] = uTree;
						isEven[v] = true;
						queue[queueEnd++] = v;
						continue;
					}

					int vBase = uf.find(ufElms[v]).get();
					if (!isEven[vBase])
						// edge to an odd vertex in some tree, ignore
						continue;

					if (vTree == uTree) {
						// Blossom
						int uBase = uf.find(ufElms[u]).get();
						if (uBase == vBase)
							// edge within existing blossom, ignore
							continue;

						// Find base for the new blossom
						int base, searchIdx = ++blossomBaseSearchNotesIndex;
						blossomBaseSearch: for (int[] ps = new int[] { uBase, vBase };;) {
							for (int i = 0; i < ps.length; i++) {
								int p = ps[i];
								if (p == -1)
									continue;
								if (blossomBaseSearchNotes[p] == searchIdx) {
									base = p;
									break blossomBaseSearch;
								}
								blossomBaseSearchNotes[p] = searchIdx;
								if (p != uRoot) {
									p = parent[matched[p].v()].v(); // move 2 up
									ps[i] = uf.find(ufElms[p]).get();
								} else
									ps[i] = -1;
							}
						}

						// Find all vertices of the blossom
						int blossomVerticesSize = 0;
						for (int p : new int[] { uBase, vBase }) {
							Edge<E> brigeEdge = p == uBase ? e : e.twin();
							while (p != base) {
								// handle even vertex
								blossomVertices[blossomVerticesSize++] = p;

								// handle odd vertex
								p = matched[p].v();
								blossomVertices[blossomVerticesSize++] = p;

								queue[queueEnd++] = p; // add the odd vertex that became even to the queue
								bridge[p] = brigeEdge;

								p = uf.find(ufElms[parent[p].v()]).get();
							}
						}

						// Union all UF elements in the new blossom
						UnionFind.Elm<Integer> baseElm = ufElms[base];
						for (int i = 0; i < blossomVerticesSize; i++)
							uf.union(baseElm, ufElms[blossomVertices[i]]);
						uf.find(baseElm).set(base); // make sure the UF value is the base

					} else {
						// augmenting path
						augPathSize = findPath(u, uRoot, isEven, matched, parent, bridge, augPath, 0);
						augPath[augPathSize++] = e;
						augPathSize = findPath(v, root[vTree], isEven, matched, parent, bridge, augPath, augPathSize);
						break bfs;
					}
				}
			}
			if (augPathSize == 0)
				break;

			for (int i = 0; i < augPathSize; i++) {
				Edge<E> e = augPath[i];
				setmatch[i] = matched[e.u()] == null || matched[e.u()].v() != e.v();
			}
			for (int i = 0; i < augPathSize; i++) {
				Edge<E> e = augPath[i];
				if (setmatch[i]) {
					matched[e.u()] = e;
					matched[e.v()] = e.twin();
				}
			}
		}

		List<Edge<E>> res = new ArrayList<>();
		for (int u = 0; u < n; u++)
			if (matched[u] != null && u < matched[u].v())
				res.add(matched[u]);
		return res;
	}

	private static <E> int findPath(int s, int t, boolean[] isEven, Edge<E>[] match, Edge<E>[] parent, Edge<E>[] bridge,
			Edge<E>[] path, int pathSize) {
		if (s == t)
			return pathSize;
		if (isEven[s]) {
			path[pathSize++] = match[s];
			path[pathSize++] = parent[match[s].v()];
			return findPath(parent[match[s].v()].v(), t, isEven, match, parent, bridge, path, pathSize);
		} else {
			Edge<E> vw = bridge[s];
			int v = vw.u(), w = vw.v();
			path[pathSize++] = match[s];
			pathSize = findPath(v, match[s].v(), isEven, match, parent, bridge, path, pathSize);
			path[pathSize++] = vw;
			return findPath(w, t, isEven, match, parent, bridge, path, pathSize);
		}
	}

}
