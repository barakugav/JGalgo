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

import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class MinimumDirectedSpanningTreeTarjanTest extends TestBase {

	private static class MDSTUndirectedWrapper implements MinimumSpanningTree {

		private final MinimumDirectedSpanningTree algo;

		MDSTUndirectedWrapper(MinimumDirectedSpanningTree algo) {
			this.algo = algo;
		}

		@Override
		public MinimumSpanningTree.Result computeMinimumSpanningTree(Graph g, WeightFunction w) {
			if (g.getCapabilities().directed())
				return algo.computeMinimumDirectedSpanningTree(g, w, 0);
			int n = g.vertices().size();
			Graph dg = GraphFactory.newDirected().expectedVerticesNum(n).newGraph();
			for (int i = 0; i < n; i++)
				dg.addVertex();

			Int2IntMap gToDg = new Int2IntOpenHashMap();
			for (IntIterator it1 = g.vertices().iterator(), it2 = dg.vertices().iterator(); it1.hasNext();)
				gToDg.put(it1.nextInt(), it2.nextInt());

			Weights.Int edgeRef = dg.addEdgesWeights("edgeRef", int.class, Integer.valueOf(-1));
			for (int u : g.vertices()) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					edgeRef.set(dg.addEdge(gToDg.get(u), gToDg.get(v)), e);
					edgeRef.set(dg.addEdge(gToDg.get(v), gToDg.get(u)), e);
				}
			}
			int root = dg.vertices().iterator().nextInt();
			MinimumSpanningTree.Result mst0 =
					algo.computeMinimumDirectedSpanningTree(dg, e -> w.weight(edgeRef.getInt(e)), root);
			IntCollection mst = new IntArrayList(mst0.edges().size());
			for (int e : mst0.edges())
				mst.add(edgeRef.getInt(e));
			return new MinimumSpanningTreeUtils.ResultImpl(mst);
		}
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x9234356819f0ea1dL;
		MinimumSpanningTreeTestUtils.testRandGraph(new MDSTUndirectedWrapper(new MinimumDirectedSpanningTreeTarjan()),
				seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		testRandGraph(new MinimumDirectedSpanningTreeTarjan(), GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void testRandGraph(MinimumDirectedSpanningTree algo, Boolean2ObjectFunction<Graph> graphImpl,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 5).repeat(256);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			testRandGraph(algo, g, w);
		});
	}

	private static void testRandGraph(MinimumDirectedSpanningTree algo, Graph g, WeightFunction w) {
		int root = g.vertices().iterator().nextInt();
		@SuppressWarnings("unused")
		MinimumSpanningTree.Result mst = algo.computeMinimumDirectedSpanningTree(g, w, root);
		// TODO verify the result
	}

}
