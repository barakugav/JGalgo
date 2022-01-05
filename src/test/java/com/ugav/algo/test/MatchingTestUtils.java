package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Matching;

class MatchingTestUtils {

	private MatchingTestUtils() {
		throw new InternalError();
	}

	static boolean randGraphs(Matching algo) {
		int[][] phases = { { 256, 16, 8 }, { 256, 16, 16 }, { 128, 32, 32 }, { 128, 32, 64 }, { 64, 64, 64 },
				{ 64, 64, 128 }, { 16, 256, 256 }, { 16, 256, 512 }, { 4, 2048, 2048 }, { 4, 2048, 8192 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];
			for (int i = 0; i < repeat; i++) {
				Graph<Void> g = GraphsTestUtils.randGraph(n, m);

				int expeced = calcExpectedMaxMatching(g);
				if (!testAlgo(algo, g, expeced))
					return false;
			}
		}
		return true;
	}

	private static <E> boolean testAlgo(Matching algo, Graph<E> g, int expectedMatchSize) {
		Collection<Edge<E>> match = algo.calcMaxMatching(g);

		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		for (Edge<E> e : match) {
			for (int v : new int[] { e.u(), e.v() }) {
				if (matched[v] != null) {
					TestUtils.printTestStr("Vertex " + v + " is matched twice: " + matched[v] + ", " + e + "\n");
					return false;
				}
			}
		}

		if (match.size() != expectedMatchSize) {
			TestUtils.printTestStr("unexpected match size: " + match.size() + " != " + expectedMatchSize + "\n");
			return false;
		}
		return true;
	}

	private static <E> int calcExpectedMaxMatching(Graph<E> g) {
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		List<Integer>[] graph = new List[n];
		for (int u = 0; u < n; u++) {
			graph[u] = new ArrayList<>();
			for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();)
				graph[u].add(it.next().v());
		}
		return EdmondsMaximumCardinalityMatching.maxMatching(graph);
	}

	/* implementation of general graphs maximum matching from the Internet */

	private static class EdmondsMaximumCardinalityMatching {
		private static int lca(int[] match, int[] base, int[] p, int a, int b) {
			boolean[] used = new boolean[match.length];
			while (true) {
				a = base[a];
				used[a] = true;
				if (match[a] == -1)
					break;
				a = p[match[a]];
			}
			while (true) {
				b = base[b];
				if (used[b])
					return b;
				b = p[match[b]];
			}
		}

		private static void markPath(int[] match, int[] base, boolean[] blossom, int[] p, int v, int b, int children) {
			for (; base[v] != b; v = p[match[v]]) {
				blossom[base[v]] = blossom[base[match[v]]] = true;
				p[v] = children;
				children = match[v];
			}
		}

		private static int findPath(List<Integer>[] graph, int[] match, int[] p, int root) {
			int n = graph.length;
			boolean[] used = new boolean[n];
			Arrays.fill(p, -1);
			int[] base = new int[n];
			for (int i = 0; i < n; ++i)
				base[i] = i;
			used[root] = true;
			int qh = 0;
			int qt = 0;
			int[] q = new int[n];
			q[qt++] = root;
			while (qh < qt) {
				int v = q[qh++];
				for (int to : graph[v]) {
					if (base[v] == base[to] || match[v] == to)
						continue;
					if (to == root || match[to] != -1 && p[match[to]] != -1) {
						int curbase = lca(match, base, p, v, to);
						boolean[] blossom = new boolean[n];
						markPath(match, base, blossom, p, v, curbase, to);
						markPath(match, base, blossom, p, to, curbase, v);
						for (int i = 0; i < n; ++i)
							if (blossom[base[i]]) {
								base[i] = curbase;
								if (!used[i]) {
									used[i] = true;
									q[qt++] = i;
								}
							}
					} else if (p[to] == -1) {
						p[to] = v;
						if (match[to] == -1)
							return to;
						to = match[to];
						used[to] = true;
						q[qt++] = to;
					}
				}
			}
			return -1;
		}

		public static int maxMatching(List<Integer>[] graph) {
			int n = graph.length;
			int[] match = new int[n];
			Arrays.fill(match, -1);
			int[] p = new int[n];
			for (int i = 0; i < n; ++i) {
				if (match[i] == -1) {
					int v = findPath(graph, match, p, i);
					while (v != -1) {
						int pv = p[v];
						int ppv = match[pv];
						match[v] = pv;
						match[pv] = v;
						v = ppv;
					}
				}
			}
			int matches = 0;
			for (int i = 0; i < n; ++i)
				if (match[i] != -1)
					++matches;
			return matches / 2;
		}
	}

}
