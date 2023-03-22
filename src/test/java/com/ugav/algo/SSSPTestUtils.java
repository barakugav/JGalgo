package com.ugav.algo;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.Graphs.PathIter;
import com.ugav.algo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntList;

@SuppressWarnings("boxing")
class SSSPTestUtils extends TestUtils {

	private SSSPTestUtils() {
	}

	static void testSSSPDirectedPositiveInt(Supplier<? extends SSSP> builder) {
		testSSSPPositiveInt(builder, true);
	}

	static void testSSSPUndirectedPositiveInt(Supplier<? extends SSSP> builder) {
		testSSSPPositiveInt(builder, false);
	}

	private static void testSSSPPositiveInt(Supplier<? extends SSSP> builder, boolean directed) {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(directed).doubleEdges(true).selfEdges(true)
					.cycles(true).connected(false).build();
			GraphsTestUtils.assignRandWeightsIntPos(g);
			WeightFunctionInt w = g.edgesWeight("weight");
			int source = rand.nextInt(g.verticesNum());

			SSSP algo = builder.get();
			SSSP.Result actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPDijkstra ? new SSSPDial1969() : new SSSPDijkstra();
			validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(Supplier<? extends SSSP> builder) {
		List<Phase> phases = List.of(phase(512, 4, 4), phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(2, 1024, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(true).selfEdges(true).cycles(true)
					.connected(true).build();
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt w = g.edgesWeight("weight");
			int source = 0;

			SSSP algo = builder.get();
			SSSP.Result actualRes = algo.calcDistances(g, w, source);

			SSSP validationAlgo = algo instanceof SSSPBellmanFord ? new SSSPGoldberg1995() : new SSSPBellmanFord();
			validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

	static void validateResult(Graph g, WeightFunction w, int source, SSSP.Result result, SSSP validationAlgo) {
		SSSP.Result expectedRes = validationAlgo.calcDistances(g, w, source);

		if (result.foundNegativeCycle()) {
			IntList cycle = null;
			try {
				cycle = result.getNegativeCycle();
			} catch (UnsupportedOperationException e) {
			}
			if (cycle != null) {
				double cycleWeight = getPathWeight(g, cycle, w);
				assertTrue(cycleWeight != Double.NaN, "Invalid cycle: ", cycle, "\n");
				assertTrue(cycleWeight < 0, "Cycle is not negative: ", cycle, "\n");
				if (!expectedRes.foundNegativeCycle())
					throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
			} else {
				assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle\n");
			}
			return;
		}
		assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle\n");

		int n = g.verticesNum();
		for (int v = 0; v < n; v++) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			assertEq(expectedDistance, actualDistance, "Distance to vertex ", v, " is wrong");
			IntList path = result.getPathTo(v);
			if (path != null) {
				double pathWeight = getPathWeight(g, path, w);
				assertEq(pathWeight, actualDistance, "Path to vertex ", v, " doesn't match distance (", actualDistance,
						" != ", pathWeight, "): ", path, "\n");
			} else {
				assertEq(Double.POSITIVE_INFINITY, actualDistance, "Distance to vertex ", v,
						" is not infinity but path is null\n");
			}
		}
	}

	private static double getPathWeight(Graph g, IntList path, WeightFunction w) {
		double totalWeight = 0;
		for (PathIter it = PathIter.of(g, path); it.hasNext();)
			totalWeight += w.weight(it.nextEdge());
		return totalWeight;
	}

}
