/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.alg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;

public class ShortestPathSingleSourceTestUtils extends TestUtils {

	private ShortestPathSingleSourceTestUtils() {}

	public static void testSSSPDirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, true, seed);
	}

	public static void testSSSPUndirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, false, seed);
	}

	private static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed) {
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(1);
		testSSSPPositiveInt(algo, directed, seed, tester);
	}

	static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed, PhasedTester tester) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial()
							: new ShortestPathSingleSourceDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPPositive(ShortestPathSingleSource algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(3542, 25436).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			WeightFunction<Integer> w = GraphsTestUtils.assignRandWeights(g, seedGen.nextSeed());
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo = new ShortestPathSingleSourceDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPCardinality(ShortestPathSingleSource algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial()
							: new ShortestPathSingleSourceDijkstra();
			testAlgo(g, null, source, algo, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(ShortestPathSingleSource algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 4).repeat(512);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(1024, 4096).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(true).build();
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			Integer source = g.vertices().iterator().next();

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceBellmanFord ? new ShortestPathSingleSourceGoldberg()
							: new ShortestPathSingleSourceBellmanFord();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static <V, E> void testAlgo(Graph<V, E> g, WeightFunction<E> w, V source, ShortestPathSingleSource algo,
			ShortestPathSingleSource validationAlgo) {
		ShortestPathSingleSource.Result<V, E> result = algo.computeShortestPaths(g, w, source);
		validateResult(g, w, source, result, validationAlgo);
	}

	static <V, E> void validateResult(Graph<V, E> g, WeightFunction<E> w, V source,
			ShortestPathSingleSource.Result<V, E> result, ShortestPathSingleSource validationAlgo) {
		ShortestPathSingleSource.Result<V, E> expectedRes = validationAlgo.computeShortestPaths(g, w, source);

		if (result.foundNegativeCycle()) {
			Path<V, E> cycle = null;
			try {
				cycle = result.getNegativeCycle();
			} catch (UnsupportedOperationException e) {
			}
			if (cycle != null) {
				double cycleWeight = w.weightSum(cycle.edges());
				assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
				assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
				if (!expectedRes.foundNegativeCycle())
					throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
			} else {
				assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle");
			}
			return;
		}
		assertFalse(expectedRes.foundNegativeCycle(),
				() -> "failed to find negative cycle: " + expectedRes.getNegativeCycle());

		for (V v : g.vertices()) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			assertEquals(expectedDistance, actualDistance, "Distance to vertex " + v + " is wrong");
			Path<V, E> path = result.getPath(v);
			if (path != null) {
				double pathWeight = WeightFunction.weightSum(w, path.edges());
				assertEquals(pathWeight, actualDistance, "Path to vertex " + v + " doesn't match distance ("
						+ actualDistance + " != " + pathWeight + "): " + path);
			} else {
				assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + v + " is not infinity but path is null");
			}
		}
	}

}
