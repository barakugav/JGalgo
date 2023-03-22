package com.ugav.algo;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.algo.GraphImplTestUtils.GraphImpl;
import com.ugav.algo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntCollection;

class MatchingBipartiteTestUtils extends TestUtils {

	private MatchingBipartiteTestUtils() {
	}

	static GraphBipartite randGraphBipartite(int sn, int tn, int m, GraphImpl graphImpl) {
		return (GraphBipartite.UGraph) new RandomGraphBuilder().sn(sn).tn(tn).m(m).directed(false).bipartite(true)
				.doubleEdges(false).selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	static GraphBipartite createGraphBipartiteFromAdjacencyMatrix(int sSize, int[][] m) {
		int n = m.length;
		GraphBipartite g = new GraphBipartiteArrayUndirected(sSize, n - sSize);
		for (int u = 0; u < n; u++) {
			for (int v = u + 1; v < n; v++) {
				if (m[u][v] == 0)
					continue;
				g.addEdge(u, v);
			}
		}
		return g;
	}

	static void randBipartiteGraphs(Supplier<? extends Matching> builder) {
		randBipartiteGraphs(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static void randBipartiteGraphs(Supplier<? extends Matching> builder, GraphImpl graphImpl) {
		List<Phase> phases = List.of(phase(256, 4, 4, 4), phase(128, 16, 16, 64), phase(16, 128, 128, 128),
				phase(16, 128, 128, 512), phase(2, 1024, 1024, 1024), phase(1, 1024, 1024, 5467));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];
			GraphBipartite g = randGraphBipartite(sn, tn, m, graphImpl);

			Matching algo = builder.get();
			int expeced = calcExpectedMaxMatching(g);
			testBipartiteAlgo(algo, g, expeced);
		});
	}

	private static void testBipartiteAlgo(Matching algo, GraphBipartite g, int expectedMatchSize) {
		IntCollection match = algo.calcMaxMatching(g);

		MatchingUnweightedTestUtils.validateMatching(g, match);

		if (match.size() > expectedMatchSize) {
			System.err.println(
					"matching is bigger than validation algo found: " + match.size() + " > " + expectedMatchSize);
			throw new IllegalStateException();
		}
		Assertions.assertTrue(match.size() == expectedMatchSize, "unexpected match size");
	}

	private static int calcExpectedMaxMatching(GraphBipartite g) {
		int sn = g.svertices(), tn = g.tvertices();
		boolean[][] m = new boolean[sn][tn];
		for (int u = 0; u < sn; u++) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				m[u][v - sn] = true;
			}
		}
		return maxBPM(m);
	}

	/*
	 * Maximum Bipartite Matching implementation of Ford-Fulkerson algorithm from
	 * the Internet
	 */
	private static int maxBPM(boolean g[][]) {
		int sn = g.length, tn = g[0].length;
		int[] matchR = new int[tn];

		for (int i = 0; i < tn; ++i)
			matchR[i] = -1;

		int result = 0;
		for (int u = 0; u < sn; u++) {
			boolean[] visited = new boolean[tn];
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
