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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class KVertexConnectedComponentsWhiteMoodyTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0xb5f6dec71d7bdb77L;
		randGraphs(new KVertexConnectedComponentsWhiteMoody(), seed);
	}

	private static void randGraphs(KVertexConnectedComponentsAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 8).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(19, 39).repeat(64);
		tester.addPhase().withArgs(23, 52).repeat(32);
		tester.addPhase().withArgs(64, 256).repeat(20);
		tester.addPhase().withArgs(80, 2000).repeat(4);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			final int k = vertexConnectivity(g) + rand.nextInt(5);

			testKConnectedComponents(g, k, algo);
		});
	}

	private static <V, E> void testKConnectedComponents(Graph<V, E> g, int k, KVertexConnectedComponentsAlgo algo) {
		KVertexConnectedComponentsAlgo.Result<V, E> result = algo.findKVertexConnectedComponents(g, k);

		for (int c = 0; c < result.componentsNum(); c++)
			assertEquals(result.componentVertices(c).size(), new HashSet<>(result.componentVertices(c)).size(),
					"duplicate vertices in component: " + result.componentVertices(c));

		assertEquals(result.componentsNum(), Range.of(result.componentsNum()).intStream()
				.mapToObj(result::componentVertices).collect(Collectors.toSet()).size(),
				"duplicate components: " + result);

		for (int c = 0; c < result.componentsNum(); c++) {
			Graph<V, E> comp = result.componentSubGraph(c);
			assertTrue(vertexConnectivity(comp) >= k);
		}

		int N = result.componentsNum();
		if (N <= 10 && g.vertices().size() <= 64) {
			for (int bitmap = 0; bitmap < 1 << N; bitmap++) {
				if (Integer.bitCount(bitmap) < 2)
					continue;
				Set<V> comp = new ObjectOpenHashSet<>();
				for (int i = 0; i < N; i++)
					if ((bitmap & (1 << i)) != 0)
						comp.addAll(result.componentVertices(i));
				assertFalse(vertexConnectivity(g.subGraphCopy(comp, null)) >= k);
			}
		}
	}

	private static <V, E> int vertexConnectivity(Graph<V, E> g) {
		return MinimumVertexCutGlobal.newInstance().computeMinimumCut(g, null).size();
	}

	@Test
	public void negativeK() {
		KVertexConnectedComponentsAlgo algo = new KVertexConnectedComponentsWhiteMoody();
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(0, 1);
		g.addEdge(1, 2);

		final int k = -1;
		assertThrows(IllegalArgumentException.class, () -> algo.findKVertexConnectedComponents(g, k));
	}

	@Test
	public void testBuilderDefaultImpl() {
		KVertexConnectedComponentsAlgo alg = KVertexConnectedComponentsAlgo.newInstance();
		assertEquals(KVertexConnectedComponentsWhiteMoody.class, alg.getClass());
	}

}
