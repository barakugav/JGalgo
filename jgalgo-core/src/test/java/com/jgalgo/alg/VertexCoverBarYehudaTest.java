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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class VertexCoverBarYehudaTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final VertexCover algo = new VertexCoverBarYehuda();
		final double appxFactor = 2;

		final long seed = 0x3c94d9694bd37614L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 16).repeat(256);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(1024, 2048).repeat(16);
		tester.addPhase().withArgs(8096, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());

			RandomIntUnique rand = new RandomIntUnique(0, 163454, seedGen.nextSeed());
			WeightsInt<Integer> weight = g.addVerticesWeights("weight", int.class);
			for (Integer v : g.vertices())
				weight.set(v, rand.next());

			testVC(g, weight, algo, appxFactor);
		});
	}

	private static <V, E> void testVC(Graph<V, E> g, WeightFunctionInt<V> w, VertexCover algo, double appxFactor) {
		VertexCover.Result<V, E> vc = algo.computeMinimumVertexCover(g, w);

		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			assertTrue(vc.isInCover(u) || vc.isInCover(v), "edge is not covered: " + e);
		}

		assertTrue(VertexCover.isCover(g, vc.vertices()));

		final int n = g.vertices().size();
		if (n < 16) {

			/* check all covers */
			Set<V> bestCover = null;
			List<V> vertices = new ArrayList<>(g.vertices());
			Set<V> cover = new ObjectOpenHashSet<>(n);
			ToDoubleFunction<Set<V>> coverWeight = c -> w.weightSum(c);
			coverLoop: for (int bitmap = 0; bitmap < 1 << n; bitmap++) {
				cover.clear();
				assert cover.isEmpty();
				for (int i = 0; i < n; i++)
					if ((bitmap & (1 << i)) != 0)
						cover.add(vertices.get(i));
				for (E e : g.edges())
					if (!cover.contains(g.edgeSource(e)) && !cover.contains(g.edgeTarget(e)))
						continue coverLoop; /* don't cover all edges */
				if (bestCover == null || coverWeight.applyAsDouble(bestCover) > coverWeight.applyAsDouble(cover))
					bestCover = new ObjectOpenHashSet<>(cover);
			}

			assertNotNull(bestCover);
			assertTrue(w.weightSum(vc.vertices()) / appxFactor <= coverWeight.applyAsDouble(bestCover));
		}
	}

}
