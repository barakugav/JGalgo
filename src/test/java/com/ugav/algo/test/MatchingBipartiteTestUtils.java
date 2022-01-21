package com.ugav.algo.test;

import java.util.Collection;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.Matching;
import com.ugav.algo.test.GraphsTestUtils.RandomGraphBuilder;

class MatchingBipartiteTestUtils {

	private MatchingBipartiteTestUtils() {
		throw new InternalError();
	}

	static <E> GraphBipartite<E> randGraphBipartite(int sn, int tn, int m) {
		return (GraphBipartite<E>) new RandomGraphBuilder().sn(sn).tn(tn).m(m).bipartite(true).directed(false)
				.doubleEdges(false).selfEdges(false).cycles(true).connected(false).<E>build();
	}

	static GraphBipartite<Void> createGraphBipartiteFromAdjacencyMatrix(int sSize, int[][] m) {
		int n = m.length;
		GraphBipartite<Void> g = new GraphBipartiteArray<>(DirectedType.Undirected, sSize, n - sSize);
		for (int u = 0; u < n; u++) {
			for (int v = u + 1; v < n; v++) {
				if (m[u][v] == 0)
					continue;
				g.addEdge(u, v);
			}
		}
		return g;
	}

	static boolean randBipartiteGraphs(Matching algo) {
		int[][] phases = { { 256, 4, 4, 4 }, { 128, 16, 16, 64 }, { 16, 128, 128, 128 }, { 16, 128, 128, 512 },
				{ 4, 1024, 1024, 1024 }, { 4, 1024, 1024, 8192 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int sn = args[1];
			int tn = args[2];
			int m = args[3];
			GraphBipartite<Void> g = randGraphBipartite(sn, tn, m);
			int expeced = calcExpectedMaxMatching(g);
			return testBipartiteAlgo(algo, g, expeced);
		});
	}

	private static <E> boolean testBipartiteAlgo(Matching algo, GraphBipartite<E> g, int expectedMatchSize) {
		Collection<Edge<E>> match = algo.calcMaxMatching(g);

		if (!MatchingUnweightedTestUtils.validateMatching(match))
			return false;

		if (match.size() < expectedMatchSize) {
			TestUtils.printTestStr("unexpected match size: " + match.size() + " != " + expectedMatchSize + "\n");
			return false;
		} else if (match.size() > expectedMatchSize) {
			TestUtils.printTestStr("matching is bigger than validation algo found: " + match.size() + " > "
					+ expectedMatchSize + "\n");
			throw new InternalError();
		}

		return true;
	}

	private static <E> int calcExpectedMaxMatching(GraphBipartite<E> g) {
		int sn = g.svertices(), tn = g.tvertices();
		boolean m[][] = new boolean[sn][tn];
		for (int u = 0; u < sn; u++)
			for (Edge<E> e : Utils.iterable(g.edges(u)))
				m[u][e.v() - sn] = true;

		return maxBPM(m);
	}

	/*
	 * Maximum Bipartite Matching implementation of Ford-Fulkerson algorithm from
	 * the Internet
	 */
	private static int maxBPM(boolean g[][]) {
		int sn = g.length, tn = g[0].length;
		int matchR[] = new int[tn];

		for (int i = 0; i < tn; ++i)
			matchR[i] = -1;

		int result = 0;
		for (int u = 0; u < sn; u++) {
			boolean visited[] = new boolean[tn];
			for (int i = 0; i < tn; ++i)
				visited[i] = false;

			if (bpm(g, u, visited, matchR))
				result++;
		}
		return result;
	}

	private static boolean bpm(boolean g[][], int u, boolean visited[], int matchR[]) {
		int tn = g[0].length;
		for (int v = 0; v < tn; v++) {
			if (g[u][v] && !visited[v]) {
				visited[v] = true;
				if (matchR[v] < 0 || bpm(g, matchR[v], visited, matchR)) {
					matchR[v] = u;
					return true;
				}
			}
		}
		return false;
	}

}
