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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class ShortestPathAllPairsTestUtils extends TestUtils {

	private ShortestPathAllPairsTestUtils() {}

	static void testAPSPPositive(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, w, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPCardinality(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			testAPSP(g, verticesSubset, null, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPDirectedNegative(ShortestPathAllPairs algo, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 256).repeat(10);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, w, algo, new ShortestPathSingleSourceGoldberg());
		});
	}

	private static <V, E> Collection<V> verticesSubset(Graph<V, E> g, boolean allVertices, long seed) {
		int n = g.vertices().size();
		if (allVertices || n <= 3)
			return g.vertices();
		Random rand = new Random(seed);
		Set<V> subset = new ObjectOpenHashSet<>();
		for (List<V> vs = new ArrayList<>(g.vertices()); subset.size() < n / 2;)
			subset.add(vs.get(rand.nextInt(n)));
		return subset;
	}

	static <V, E> void testAPSP(Graph<V, E> g, Collection<V> verticesSubset, WeightFunction<E> w,
			ShortestPathAllPairs algo, ShortestPathSingleSource validationAlgo) {
		ShortestPathAllPairs.Result<V, E> result = algo.computeAllShortestPaths(g, w);

		for (V source : verticesSubset) {
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
			assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle");

			for (V target : verticesSubset) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				Path<V, E> path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = WeightFunction.weightSum(w, path.edges());
					assertEquals(pathWeight, actualDistance, "Path to vertex " + target + " doesn't match distance ("
							+ actualDistance + " != " + pathWeight + "): " + path);
				} else {
					assertEquals(Double.POSITIVE_INFINITY, actualDistance,
							"Distance to vertex " + target + " is not infinity but path is null");
				}
			}
		}
	}

}
