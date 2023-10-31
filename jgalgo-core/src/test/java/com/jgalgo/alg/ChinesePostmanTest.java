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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ChinesePostmanTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xf022c4bc9a0866e6L;
		ChinesePostman algo = new ChinesePostmanImpl();
		testRandGraphs(algo, false, seed);
	}

	@Test
	public void testRandGraphsWithSelfEdges() {
		final long seed = 0x0d8252e452d6abdfL;
		ChinesePostman algo = new ChinesePostmanImpl();
		testRandGraphs(algo, true, seed);
	}

	private static void testRandGraphs(ChinesePostman algo, boolean selfEdges, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 10).repeat(16);
		tester.addPhase().withArgs(8, 32).repeat(16);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(selfEdges).cycles(true).connected(true).build();
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testGraph(g, w, algo);
		});
	}

	private static void testGraph(IntGraph g, IWeightFunctionInt w, ChinesePostman algo) {
		IPath chinesePostmanTour = algo.computeShortestEdgeVisitorCircle(g, w);

		/* Asserts all edges are traversed by the tour */
		IntSet tourEdges = new IntOpenHashSet();
		for (int e : chinesePostmanTour.edges())
			tourEdges.add(e);
		assertEquals(g.edges(), tourEdges);

		double chinesePostmanTourWeight = w.weightSum(chinesePostmanTour.edges());
		final int m = g.edges().size();
		if (m >= 32)
			return;

		double bestWeight = Double.POSITIVE_INFINITY;
		int[] es = g.edges().toIntArray();
		for (int bitmap = 0; bitmap < 1 << m; bitmap++) {
			IntGraphBuilder b = IntGraphBuilder.newFrom(g);
			IWeightsInt bWeights = b.addEdgesWeights("weights", int.class);
			for (int e : g.edges())
				bWeights.set(e, w.weightInt(e));

			for (int i = 0; i < m; i++) {
				if ((bitmap & (1 << i)) == 0)
					continue;
				int origEdge = es[i];
				int u = g.edgeSource(origEdge);
				int v = g.edgeTarget(origEdge);
				int duplicatedEdge = b.addEdge(v, u);
				bWeights.set(duplicatedEdge, bWeights.weightInt(origEdge));
			}
			IntGraph eulerianGraph = b.build();
			if (eulerianGraph.vertices().intStream().anyMatch(v -> nonSelfEdgesDegree(eulerianGraph, v) % 2 != 0))
				continue;

			IPath eulerianTour = EulerianTourAlgo.newInstance().computeEulerianTour(eulerianGraph);
			double eulerianTourWeight = bWeights.weightSum(eulerianTour.edges());
			if (bestWeight > eulerianTourWeight)
				bestWeight = eulerianTourWeight;
			assertTrue(eulerianTourWeight >= chinesePostmanTourWeight - 1e-6);
		}

		assertEquals(bestWeight, chinesePostmanTourWeight, 1e-6);
	}

	private static int nonSelfEdgesDegree(IntGraph g, int v) {
		int nonSelfEdgesCount = 0;
		for (IEdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
			eit.nextInt();
			if (eit.targetInt() != v)
				nonSelfEdgesCount++;
		}
		return nonSelfEdgesCount;
	}

}
