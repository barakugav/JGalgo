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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class BiConnectedComponentsAlgoHopcroftTarjanTest extends TestBase {

	@Test
	public void randGraphUndirected() {
		final long seed = 0xda9272921794ecfaL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 5, 6), phase(64, 16, 32), phase(32, 64, 256), phase(1, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			testUGraph(BiConnectedComponentsAlgo.newBuilder().build(), g);
		});
	}

	private static void testUGraph(BiConnectedComponentsAlgo algo, Graph g) {
		BiConnectedComponentsAlgo.Result res = algo.computeBiConnectivityComponents(g);

		/* Check that each vertex is contained in some BiCc */
		final int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			IntCollection vBiccs = res.getVertexBiCcs(v);
			assertFalse(vBiccs.isEmpty(), "a vertex is not contained in any BiConnected component: " + v);
			assertEquals(new IntOpenHashSet(vBiccs).size(), vBiccs.size(),
					"vertex BiCcs list contains duplications" + vBiccs);
		}

		/* Check that each edge is contained in exactly one BiCc (unless its a self loop) */
		Weights<IntSet> edgeToBiccs = Weights.createExternalEdgesWeights(g, IntSet.class, null);
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			IntCollection biccEdges = res.getBiCcEdges(bccIdx);
			for (IntIterator eit = biccEdges.iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				IntSet eBiccs = edgeToBiccs.get(e);
				if (eBiccs == null)
					edgeToBiccs.set(e, eBiccs = new IntOpenHashSet());
				eBiccs.add(bccIdx);
			}
			assertEquals(new IntOpenHashSet(biccEdges).size(), biccEdges.size(),
					"BiCc edges list contains duplications" + biccEdges);
		}
		for (IntIterator eit = g.edges().iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			IntSet eBiccs = edgeToBiccs.get(e);
			if (u != v) {
				assertEquals(1, eBiccs.size(), "we expect each edge to be in exactly one BiCc: " + eBiccs);
			} else {
				assertEquals(res.getVertexBiCcs(u).size(), eBiccs.size(),
						"self edge should be included in any BiCc that contains the vertex" + eBiccs);
			}
		}

		final ConnectedComponentsAlgo ccAlgo = ConnectedComponentsAlgo.newBuilder().build();
		final ConnectedComponentsAlgo.Result gCcs = ccAlgo.computeConnectivityComponents(g);

		/* Check that each bicc is actually a BiConnected component */
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			IntCollection vertices = res.getBiCcVertices(bccIdx);
			assertFalse(vertices.isEmpty(), "BiConnected component can't be empty");
			assertEquals(new IntOpenHashSet(vertices).size(), vertices.size(),
					"BiCc vertices list contains duplications" + vertices);
			if (vertices.size() == 1)
				continue;

			IntSet ccIdxs = new IntOpenHashSet();
			for (IntIterator it = vertices.iterator(); it.hasNext();)
				ccIdxs.add(gCcs.getVertexCc(it.nextInt()));
			assertTrue(ccIdxs.size() == 1, "BiConnected component vertices are not in a the connected component");

			for (IntIterator vit = vertices.iterator(); vit.hasNext();) {
				final int vToRemove = vit.nextInt();
				Graph gWithoutV = g.copy();
				gWithoutV.removeEdgesOf(vToRemove);

				ConnectedComponentsAlgo.Result ccsWithoutV = ccAlgo.computeConnectivityComponents(gWithoutV);
				ccIdxs.clear();
				for (IntIterator uit = vertices.iterator(); uit.hasNext();) {
					int u = uit.nextInt();
					if (u != vToRemove)
						ccIdxs.add(ccsWithoutV.getVertexCc(u));
				}
				assertEquals(1, ccIdxs.size(),
						"BiConnected component vertices are not in a the connected component after remove a single vertex: "
								+ vToRemove);
			}
		}

		/* Check that we couldn't merge two biccs into one */
		for (int i = 0; i < res.getNumberOfBiCcs(); i++) {
			for (int j = i + 1; j < res.getNumberOfBiCcs(); j++) {
				IntCollection vs1 = res.getBiCcVertices(i);
				IntCollection vs2 = res.getBiCcVertices(j);
				if (gCcs.getVertexCc(vs1.iterator().nextInt()) != gCcs.getVertexCc(vs2.iterator().nextInt()))
					continue; /* not connected at all */

				boolean sameCcForAllV = true;
				for (IntIterator vit = IntIterators.concat(vs1.iterator(), vs2.iterator()); vit.hasNext();) {
					final int vToRemove = vit.nextInt();
					Graph gWithoutV = g.copy();
					gWithoutV.removeEdgesOf(vToRemove);

					ConnectedComponentsAlgo.Result ccsWithoutV = ccAlgo.computeConnectivityComponents(gWithoutV);
					IntSet ccIdxs = new IntOpenHashSet();
					for (IntIterator uit = IntIterators.concat(vs1.iterator(), vs2.iterator()); uit.hasNext();) {
						int u = uit.nextInt();
						if (u != vToRemove)
							ccIdxs.add(ccsWithoutV.getVertexCc(u));
					}
					if (ccIdxs.size() > 1)
						sameCcForAllV = false;
				}
				assertFalse(sameCcForAllV,
						"two biccs were connected after removing any vertex. Should be the same bicc");
			}
		}
	}

}