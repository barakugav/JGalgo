package com.jgalgo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.EdgeIter;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPBellmanFord;
import com.jgalgo.SSSPDial1969;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.SSSPGoldberg1995;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class SSSPTestUtils extends TestUtils {

	private SSSPTestUtils() {
	}

	public static void testSSSPDirectedPositiveInt(Supplier<? extends SSSP> builder, long seed) {
		testSSSPPositiveInt(builder, true, seed);
	}

	public static void testSSSPUndirectedPositiveInt(Supplier<? extends SSSP> builder, long seed) {
		testSSSPPositiveInt(builder, false, seed);
	}

	private static void testSSSPPositiveInt(Supplier<? extends SSSP> builder, boolean directed, long seed) {
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		testSSSPPositiveInt(builder, directed, seed, phases);
	}

	static void testSSSPPositiveInt(Supplier<? extends SSSP> builder, boolean directed, long seed, List<Phase> phases) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = rand.nextInt(g.vertices().size());

			SSSP algo = builder.get();
			SSSP validationAlgo = algo instanceof SSSPDijkstra ? new SSSPDial1969() : new SSSPDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(Supplier<? extends SSSP> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(512, 4, 4), phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(2, 1024, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			int source = 0;

			SSSP algo = builder.get();
			SSSP validationAlgo = algo instanceof SSSPBellmanFord ? new SSSPGoldberg1995() : new SSSPBellmanFord();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testAlgo(Graph g, EdgeWeightFunc w, int source, SSSP algo, SSSP validationAlgo) {
		SSSP.Result result = algo.calcDistances(g, w, source);
		validateResult(g, w, source, result, validationAlgo);
	}

	static void validateResult(Graph g, EdgeWeightFunc w, int source, SSSP.Result result, SSSP validationAlgo) {
		SSSP.Result expectedRes = validationAlgo.calcDistances(g, w, source);

		if (result.foundNegativeCycle()) {
			Path cycle = null;
			try {
				cycle = result.getNegativeCycle();
			} catch (UnsupportedOperationException e) {
			}
			if (cycle != null) {
				double cycleWeight = getPathWeight(cycle, w);
				Assertions.assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
				Assertions.assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
				if (!expectedRes.foundNegativeCycle())
					throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
			} else {
				Assertions.assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle");
			}
			return;
		}
		Assertions.assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle");

		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			Assertions.assertEquals(expectedDistance, actualDistance, "Distance to vertex " + v + " is wrong");
			Path path = result.getPathTo(v);
			if (path != null) {
				double pathWeight = getPathWeight(path, w);
				Assertions.assertEquals(pathWeight, actualDistance, "Path to vertex " + v + " doesn't match distance ("
						+ actualDistance + " != " + pathWeight + "): " + path);
			} else {
				Assertions.assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + v + " is not infinity but path is null");
			}
		}
	}

	static double getPathWeight(Path path, EdgeWeightFunc w) {
		double totalWeight = 0;
		for (EdgeIter it = path.edgeIter(); it.hasNext();)
			totalWeight += w.weight(it.nextInt());
		return totalWeight;
	}

}