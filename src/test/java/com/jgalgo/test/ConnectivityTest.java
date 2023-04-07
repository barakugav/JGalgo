package com.jgalgo.test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jgalgo.BFSIter;
import com.jgalgo.Connectivity;
import com.jgalgo.DiGraph;
import com.jgalgo.Graph;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class ConnectivityTest extends TestUtils {

	@Test
	public void randGraphUndirected() {
		final long seed = 0xb3f19acd0e1041deL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			Connectivity.Result actual = Connectivity.findConnectivityComponents(g);
			validateConnectivityResult(g, actual);
			Connectivity.Result expected = calcUndirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Connectivity.Result calcUndirectedConnectivity(UGraph g) {
		int n = g.vertices().size();
		int ccNum = 0;
		int[] vertexToCC = new int[n];
		Arrays.fill(vertexToCC, -1);

		for (int start = 0; start < n; start++) {
			if (vertexToCC[start] != -1)
				continue;
			int ccIdx = ccNum++;
			for (BFSIter it = new BFSIter(g, start); it.hasNext();)
				vertexToCC[it.nextInt()] = ccIdx;
		}
		return new Connectivity.Result(ccNum, vertexToCC);
	}

	@Test
	public void randGraphDirected() {
		final long seed = 0xd21f8ca761bc1aaeL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Connectivity.Result actual = Connectivity.findStrongConnectivityComponents(g);
			validateConnectivityResult(g, actual);
			Connectivity.Result expected = calcDirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Connectivity.Result calcDirectedConnectivity(DiGraph g) {
		int n = g.vertices().size();
		BitSet[] reach = new BitSet[n];
		for (int start = 0; start < n; start++) {
			reach[start] = new BitSet(n);
			for (BFSIter it = new BFSIter(g, start); it.hasNext();)
				reach[start].set(it.nextInt());
		}

		int ccNum = 0;
		int[] vertexToCC = new int[n];
		Arrays.fill(vertexToCC, -1);

		for (int u = 0; u < n; u++) {
			if (vertexToCC[u] != -1)
				continue;
			int ccIdx = ccNum++;
			vertexToCC[u] = ccIdx;
			for (IntIterator it = Utils.bitSetIterator(reach[u]); it.hasNext();) {
				int v = it.nextInt();
				if (reach[v].get(u))
					vertexToCC[v] = ccIdx;
			}
		}
		return new Connectivity.Result(ccNum, vertexToCC);
	}

	private static void assertConnectivityResultsEqual(Graph g, Connectivity.Result r1, Connectivity.Result r2) {
		Assertions.assertEquals(r1.ccNum, r2.ccNum);
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r1.ccNum);
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			int cc1 = r1.getVertexCcIndex(u);
			int cc2 = r2.getVertexCcIndex(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				Assertions.assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	private static void validateConnectivityResult(Graph g, Connectivity.Result res) {
		BitSet ccs = new BitSet();
		int n = g.vertices().size();
		for (int v = 0; v < n; v++)
			ccs.set(res.getVertexCcIndex(v));
		Assertions.assertEquals(ccs.cardinality(), res.ccNum);
		for (int cc = 0; cc < res.ccNum; cc++)
			Assertions.assertTrue(ccs.get(cc));
	}

}
