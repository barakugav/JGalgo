package com.jgalgo.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jgalgo.Connectivity;
import com.jgalgo.DiGraph;
import com.jgalgo.EdgeIter;
import com.jgalgo.EulerianTour;
import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EulerianTourTest extends TestUtils {

	@Test
	public void testRandGraphUndirectedAllEvenDegree() {
		final long seed = 0x92d17aaf1fe75a3fL;
		testRandGraphUndirected(true, seed);
	}

	@Test
	public void testRandGraphUndirectedTwoOddDegree() {
		final long seed = 0xfe1af8c840dbcd5bL;
		testRandGraphUndirected(false, seed);
	}

	@Test
	public void testRandGraphDirectedAllEqualInOutDegree() {
		final long seed = 0xd6802511f6b16d27L;
		testRandGraphDirected(true, seed);
	}

	@Test
	public void testRandGraphDirectedOneExtraInDegreeOneExtraOutDegree() {
		final long seed = 0x59a5e2af6122a61fL;
		testRandGraphDirected(false, seed);
	}

	private static void testRandGraphUndirected(boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = randUGraph(n, m, allEvenVertices, seedGen.nextSeed());
			Path tour = EulerianTour.calcTour(g);
			validateEulerainTour(g, tour);
		});
	}

	private static void testRandGraphDirected(boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = randDiGraph(n, m, allEqualInOutDegree, seedGen.nextSeed());
			Path tour = EulerianTour.calcTour(g);
			validateEulerainTour(g, tour);
		});
	}

	private static void validateEulerainTour(Graph g, Path tour) {
		IntSet usedEdges = new IntOpenHashSet(g.edges().size());
		for (EdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e = it.nextInt();
			boolean alreadyUsed = !usedEdges.add(e);
			Assertions.assertFalse(alreadyUsed, "edge appear twice in tour: " + e);
		}

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			Assertions.assertTrue(usedEdges.contains(e), "edge was not used: " + e);
		}
	}

	private static UGraph randUGraph(int n, int m, boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).doubleEdges(true)
				.selfEdges(true).cycles(true).connected(true).build();

		IntList oddVertices = new IntArrayList();
		for (int u = 0; u < n; u++)
			if (degreeWithoutSelfLoops(g, u) % 2 != 0)
				oddVertices.add(u);
		assert oddVertices.size() % 2 == 0;

		while (!oddVertices.isEmpty()) {
			int uIdx = rand.nextInt(oddVertices.size());
			int vIdx = rand.nextInt(oddVertices.size());
			int u = oddVertices.getInt(uIdx);
			int v = oddVertices.getInt(vIdx);
			if (u == v)
				continue;
			g.addEdge(u, v);
			assert degreeWithoutSelfLoops(g, u) % 2 == 0;
			assert degreeWithoutSelfLoops(g, v) % 2 == 0;

			if (uIdx < vIdx) {
				int temp = uIdx;
				uIdx = vIdx;
				vIdx = temp;
			}

			/* remove u and v from oddVertices */
			/* assume uIdx > vIdx */
			swapAndRemove(oddVertices, uIdx);
			swapAndRemove(oddVertices, vIdx);
		}

		for (int u = 0; u < n; u++)
			assert degreeWithoutSelfLoops(g, u) % 2 == 0;
		if (!allEvenVertices) {
			/* Add another edge resulting in two vertices with odd degree */
			if (n <= 1)
				throw new IllegalArgumentException();
			for (;;) {
				int u = rand.nextInt(n);
				int v = rand.nextInt(n);
				if (u == v)
					continue;
				g.addEdge(u, v);
				break;
			}
		}

		return g;
	}

	private static DiGraph randDiGraph(int n, int m, boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).doubleEdges(true)
				.selfEdges(true).cycles(true).connected(true).build();
		addEdgesUntilStronglyConnected(g);

		IntList lackingOutEdgesVertices = new IntArrayList();
		IntList lackingInEdgesVertices = new IntArrayList();
		IntList lackingOutEdgesNum = new IntArrayList();
		IntList lackingInEdgesNum = new IntArrayList();
		for (int u = 0; u < n; u++) {
			int outD = g.degreeOut(u);
			int inD = g.degreeIn(u);
			if (outD == inD)
				continue;
			if (outD > inD) {
				lackingInEdgesVertices.add(u);
				lackingInEdgesNum.add(outD - inD);
			} else {
				lackingOutEdgesVertices.add(u);
				lackingOutEdgesNum.add(inD - outD);
			}
		}

		for (;;) {
			if (lackingOutEdgesVertices.isEmpty()) {
				assert lackingInEdgesVertices.isEmpty();
				break;
			}
			int uIdx = rand.nextInt(lackingOutEdgesVertices.size());
			int vIdx = rand.nextInt(lackingInEdgesVertices.size());
			int u = lackingOutEdgesVertices.getInt(uIdx);
			int v = lackingInEdgesVertices.getInt(vIdx);
			if (u == v)
				continue;
			g.addEdge(u, v);

			/* remove u and v if they have enough out/in edges */
			/* assume uIdx > vIdx */
			int uLackingOutNum = lackingOutEdgesNum.getInt(uIdx);
			if (--uLackingOutNum > 0) {
				assert g.degreeIn(u) - g.degreeOut(u) == uLackingOutNum;
				lackingOutEdgesNum.set(uIdx, uLackingOutNum);
			} else {
				assert g.degreeIn(u) - g.degreeOut(u) == uLackingOutNum;
				swapAndRemove(lackingOutEdgesNum, uIdx);
				swapAndRemove(lackingOutEdgesVertices, uIdx);
			}
			int vLackingInNum = lackingInEdgesNum.getInt(vIdx);
			if (--vLackingInNum > 0) {
				assert g.degreeOut(v) - g.degreeIn(v) == vLackingInNum;
				lackingInEdgesNum.set(vIdx, vLackingInNum);
			} else {
				assert g.degreeOut(v) - g.degreeIn(v) == vLackingInNum;
				swapAndRemove(lackingInEdgesNum, vIdx);
				swapAndRemove(lackingInEdgesVertices, vIdx);
			}
		}

		for (int u = 0; u < n; u++)
			assert g.degreeOut(u) == g.degreeIn(u);
		if (!allEqualInOutDegree) {
			/*
			 * Add another edge resulting in one vertex with extra out degree, and one
			 * vertex with extra in degree
			 */
			if (n <= 1)
				throw new IllegalArgumentException();
			for (;;) {
				int u = rand.nextInt(n);
				int v = rand.nextInt(n);
				if (u == v)
					continue;
				g.addEdge(u, v);
				break;
			}
		}
		assert Connectivity.findStrongConnectivityComponents(g).ccNum == 1;
		return g;
	}

	private static int degreeWithoutSelfLoops(UGraph g, int u) {
		int d = 0;
		for (EdgeIter eit = g.edges(u); eit.hasNext();) {
			eit.nextInt();
			if (eit.v() != u)
				d++;
		}
		return d;
	}

	private static void swapAndRemove(IntList list, int idx) {
		list.set(idx, list.getInt(list.size() - 1));
		list.removeInt(list.size() - 1);
	}

	private static void addEdgesUntilStronglyConnected(DiGraph g) {
		Connectivity.Result connectivityRes = Connectivity.findStrongConnectivityComponents(g);
		int N = connectivityRes.ccNum;
		int[] v2V = connectivityRes.vertexToCC;
		if (N <= 1)
			return;

		int[] V2v = new int[N];
		Arrays.fill(V2v, -1);
		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			int V = v2V[v];
			if (V2v[V] == -1)
				V2v[V] = v;
		}

		for (int V = 1; V < N; V++) {
			g.addEdge(V2v[0], V2v[V]);
			g.addEdge(V2v[V], V2v[0]);
		}
	}

}