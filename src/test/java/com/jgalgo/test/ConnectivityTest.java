package com.jgalgo.test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.jgalgo.BFSIter;
import com.jgalgo.Connectivity;
import com.jgalgo.DiGraph;
import com.jgalgo.Graph;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.Pair;
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
			Pair<Integer, int[]> expected = calcUndirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Pair<Integer, int[]> calcUndirectedConnectivity(UGraph g) {
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
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
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
			Pair<Integer, int[]> expected = calcDirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Pair<Integer, int[]> calcDirectedConnectivity(DiGraph g) {
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
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
	}

	private static void assertConnectivityResultsEqual(Graph g, Pair<Integer, int[]> r1, Connectivity.Result r2) {
		assertEquals(r1.first(), r2.getNumberOfCC());
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r2.getNumberOfCC());
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			int cc1 = r1.second()[u];
			int cc2 = r2.getVertexCc(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	private static void validateConnectivityResult(Graph g, Connectivity.Result res) {
		BitSet ccs = new BitSet();
		int n = g.vertices().size();
		for (int v = 0; v < n; v++)
			ccs.set(res.getVertexCc(v));
		assertEquals(ccs.cardinality(), res.getNumberOfCC());
		for (int cc = 0; cc < res.getNumberOfCC(); cc++)
			assertTrue(ccs.get(cc));
	}

}
