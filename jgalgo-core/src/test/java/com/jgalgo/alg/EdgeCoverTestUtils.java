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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestUtils.PhasedTester;
import com.jgalgo.internal.util.TestUtils.SeedGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class EdgeCoverTestUtils {

	static void testRandGraphsCardinality(EdgeCover algo, long seed) {
		testRandGraphs(algo, seed, false);
	}

	static void testRandGraphsWeighted(EdgeCover algo, long seed) {
		testRandGraphs(algo, seed, true);
	}

	private static void testRandGraphs(EdgeCover algo, long seed, boolean weighted) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 6).repeat(64);
		tester.addPhase().withArgs(8, 12).repeat(64);
		tester.addPhase().withArgs(8, 16).repeat(32);
		tester.addPhase().withArgs(64, 256).repeat(16);
		tester.addPhase().withArgs(1024, 2048).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(true).build();

			RandomIntUnique rand = new RandomIntUnique(0, m * 16, seedGen.nextSeed());
			WeightsInt<Integer> weights;
			if (weighted) {
				weights = g.addEdgesWeights("weight", int.class);
				for (Integer e : g.edges())
					weights.set(e, rand.next());
			} else {
				weights = null;
			}

			EdgeCoverTestUtils.testEdgeCover(g, weights, algo);
		});
	}

	static <V, E> void testEdgeCover(Graph<V, E> g, WeightFunctionInt<E> w, EdgeCover algo) {
		Set<E> ec = algo.computeMinimumEdgeCover(g, w);

		for (V v : g.vertices()) {
			boolean isCovered = g.outEdges(v).stream().anyMatch(ec::contains);
			if (g.isDirected())
				isCovered |= g.inEdges(v).stream().anyMatch(ec::contains);
			assertTrue(isCovered, "vertex is not covered: " + v);
		}

		assertTrue(EdgeCover.isCover(g, ec));

		final int m = g.edges().size();
		if (m <= 16) {

			/* check all covers */
			Set<E> bestCover = null;
			List<E> edges = new ObjectArrayList<>(g.edges());
			Set<E> cover = new ObjectOpenHashSet<>(m);
			ToDoubleFunction<Set<E>> coverWeight = c -> WeightFunction.weightSum(w, c);
			coverLoop: for (int bitmap = 0; bitmap < 1 << m; bitmap++) {
				cover.clear();
				assert cover.isEmpty();
				for (int i = 0; i < m; i++)
					if ((bitmap & (1 << i)) != 0)
						cover.add(edges.get(i));
				for (V v : g.vertices())
					if (!g.outEdges(v).stream().anyMatch(cover::contains)
							&& (!g.isDirected() || !g.inEdges(v).stream().anyMatch(cover::contains)))
						continue coverLoop; /* don't cover all vertices */
				if (bestCover == null || coverWeight.applyAsDouble(bestCover) > coverWeight.applyAsDouble(cover))
					bestCover = new ObjectOpenHashSet<>(cover);
			}

			assertNotNull(bestCover);
			assertEquals(coverWeight.applyAsDouble(bestCover), WeightFunction.weightSum(w, ec));
		}
	}

}
