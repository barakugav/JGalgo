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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {}

	static void randGraphsBipartiteWeighted(MatchingAlgorithm algo, long seed) {
		randGraphsBipartiteWeighted(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void randGraphsBipartiteWeighted(MatchingAlgorithm algo, Boolean2ObjectFunction<Graph> graphImpl,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(2, 256, 256, 1200));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			Graph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgorithm validationAlgo =
					algo instanceof MatchingWeightedBipartiteSSSP ? new MatchingWeightedBipartiteHungarianMethod()
							: new MatchingWeightedBipartiteSSSP();
			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static void randBipartiteGraphsWeightedPerfect(MatchingAlgorithm algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 8, 8, 8), phase(32, 16, 16, 64), phase(8, 128, 128, 128),
				phase(4, 128, 128, 512), phase(1, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			Graph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, GraphsTestUtils.defaultGraphImpl(),
					seedGen.nextSeed());
			Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);

			MatchingAlgorithm cardinalityAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
			IntList unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
			unmatchedVerticesS.removeIf(v -> partition.getBool(v));
			unmatchedVerticesT.removeIf(v -> !partition.getBool(v));
			assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
			IntLists.shuffle(unmatchedVerticesS, new Random(seedGen.nextSeed()));
			IntLists.shuffle(unmatchedVerticesT, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVerticesS.size(); i++) {
				int u = unmatchedVerticesS.getInt(i);
				int v = unmatchedVerticesT.getInt(i);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgorithm validationUnweightedAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			MatchingAlgorithm validationWeightedAlgo =
					algo instanceof MatchingWeightedBipartiteHungarianMethod ? new MatchingWeightedGabow1990()
							: new MatchingWeightedBipartiteHungarianMethod();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void randGraphsWeighted(MatchingAlgorithm algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(6, 128, 128, 512), phase(1, 1024, 1024, 2300));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgorithm validationAlgo =
					algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
							: new MatchingWeightedGabow1990();

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static void testGraphWeighted(MatchingAlgorithm algo, Graph g, WeightFunction.Int w,
			MatchingAlgorithm validationAlgo) {
		Matching actual = algo.computeMaximumWeightedMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		double actualWeight = actual.weight(w);

		Matching expected = validationAlgo.computeMaximumWeightedMatching(g, w);
		double expectedWeight = expected.weight(w);

		if (actualWeight > expectedWeight) {
			System.err
					.println("matching is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(MatchingAlgorithm algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(2, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			if (g.vertices().size() % 2 != 0)
				throw new IllegalArgumentException("there is no perfect matching");

			MatchingAlgorithm cardinalityAlgo = new MatchingCardinalityGabow1976();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVertices = new IntArrayList(cardinalityMatch.unmatchedVertices());
			assert unmatchedVertices.size() % 2 == 0;
			IntLists.shuffle(unmatchedVertices, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVertices.size() / 2; i++) {
				int u = unmatchedVertices.getInt(i * 2 + 0);
				int v = unmatchedVertices.getInt(i * 2 + 1);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgorithm validationUnweightedAlgo = new MatchingCardinalityGabow1976();
			MatchingAlgorithm validationWeightedAlgo =
					algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
							: new MatchingWeightedGabow1990();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void testGraphWeightedPerfect(MatchingAlgorithm algo, Graph g, WeightFunction.Int w,
			MatchingAlgorithm validationUnweightedAlgo, MatchingAlgorithm validationWeightedAlgo) {
		Matching actual = algo.computeMaximumWeightedPerfectMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		int actualSize = actual.edges().size();
		double actualWeight = actual.weight(w);

		int expectedSize = validationUnweightedAlgo.computeMaximumCardinalityMatching(g).edges().size();
		if (actualSize > expectedSize) {
			System.err.println(
					"matching size is better than validation algo found: " + actualSize + " > " + expectedSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedSize, actualSize, "unexpected match size");

		Matching expected = validationWeightedAlgo.computeMaximumWeightedPerfectMatching(g, w);
		double expectedWeight = expected.weight(w);
		if (actualWeight > expectedWeight) {
			System.err.println(
					"matching weight is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

}
