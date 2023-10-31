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
import java.util.Random;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class FlowCirculationTestUtils extends TestUtils {

	static void testRandCirculationInt(FlowCirculation algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();

			IFlowNetworkInt net = randNetworkInt(g, rand);
			IWeightFunctionInt supply = randSupplyInt(g, net, rand);

			testRandCirculationInt(g, net, supply, algo);
		});
	}

	private static void testRandCirculationInt(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt supply,
			FlowCirculation algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeCirculation(g, net, supply);

		MaximumFlowTestUtils.assertValidFlow(g, net, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));
		assertSupplySatisfied(g, net, supply);
	}

	static void testRandCirculation(FlowCirculation algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();

			IFlowNetwork net = randNetwork(g, rand);
			IWeightFunction supply = randSupply(g, net, rand);

			testRandCirculation(g, net, supply, algo);
		});
	}

	private static void testRandCirculation(IntGraph g, IFlowNetwork net, IWeightFunction supply,
			FlowCirculation algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeCirculation(g, net, supply);

		MaximumFlowTestUtils.assertValidFlow(g, net, verticesWithPositiveSupply(g.vertices(), supply),
				verticesWithNegativeSupply(g.vertices(), supply));
		assertSupplySatisfied(g, net, supply);
	}

	private static IFlowNetworkInt randNetworkInt(IntGraph g, Random rand) {
		IFlowNetworkInt net = IFlowNetworkInt.createFromEdgeWeights(g);
		for (int e : g.edges())
			net.setCapacity(e, 400 + rand.nextInt(1024));
		return net;
	}

	private static IFlowNetwork randNetwork(IntGraph g, Random rand) {
		IFlowNetwork net = IFlowNetwork.createFromEdgeWeights(g);
		for (int e : g.edges())
			net.setCapacity(e, 400 + rand.nextDouble() * 1024);
		return net;
	}

	static IWeightFunctionInt randSupplyInt(IntGraph g, IFlowNetworkInt net, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		IntList suppliers = new IntArrayList();
		IntList demanders = new IntArrayList();
		int[] vertices = g.vertices().toIntArray();
		IntArrays.shuffle(vertices, rand);
		assert vertices.length >= 2;
		suppliers.add(vertices[0]);
		demanders.add(vertices[1]);
		for (int i = 2; i < vertices.length; i++) {
			int r = rand.nextInt(3);
			if (r == 0) {
				suppliers.add(vertices[i]);
			} else if (r == 1) {
				demanders.add(vertices[i]);
			} else {
				/* do nothing */
			}
		}

		Int2IntMap capacity = new Int2IntOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		IntSet demandersSet = new IntOpenHashSet(demanders);
		IntList suppliersList = new IntArrayList(suppliers);

		IWeightsInt supply = IWeights.createExternalVerticesWeights(g, int.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();
		suppliersLoop: for (;;) {
			if (suppliersList.isEmpty()) {
				assert g.vertices().intStream().map(supply::weightInt).sum() == 0;
				return supply;
			}

			path.clear();
			visited.clear();

			int supplierIdx = rand.nextInt(suppliersList.size());
			int supplier = suppliersList.getInt(supplierIdx);
			visited.add(supplier);
			dfs: for (int u = supplier;;) {

				/* Find a random edge to deepen the DFS */
				int[] es = g.outEdges(u).toIntArray();
				IntArrays.shuffle(es, rand);
				for (int e : es) {
					if (capacity.get(e) == 0)
						continue;
					assert u == g.edgeSource(e);
					int v = g.edgeTarget(e);
					if (visited.contains(v))
						continue;
					path.add(e);

					if (demandersSet.contains(v))
						/* found an residual path from a supplier to a demander */
						break dfs;

					/* Continue down in the DFS */
					visited.add(v);
					u = v;
					continue dfs;
				}

				/* No more edges to explore */
				if (path.isEmpty()) {
					/* No more residual paths from supplier to any demander, remove supplier from suppliers list */
					suppliersList.set(supplierIdx, suppliersList.getInt(suppliersList.size() - 1));
					suppliersList.removeInt(suppliersList.size() - 1);
					continue suppliersLoop;
				}

				/* Back up in the DFS path one vertex */
				int lastEdge = path.popInt();
				assert u == g.edgeTarget(lastEdge);
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from a supplier to a demander */
			int delta = path.intStream().map(capacity::get).min().getAsInt();
			assert delta > 0;
			for (int e : path)
				capacity.put(e, capacity.get(e) - delta);

			/* Add lower bounds to some of the edges */
			int source = g.edgeSource(path.getInt(0));
			int sink = g.edgeTarget(path.getInt(path.size() - 1));
			if (rand.nextBoolean()) {
				int s = rand.nextInt((delta + 1) / 2);
				supply.set(source, supply.weightInt(source) + s);
				supply.set(sink, supply.weightInt(sink) - s);
			}
		}
	}

	private static IWeightFunction randSupply(IntGraph g, IFlowNetwork net, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		IntList suppliers = new IntArrayList();
		IntList demanders = new IntArrayList();
		int[] vertices = g.vertices().toIntArray();
		IntArrays.shuffle(vertices, rand);
		assert vertices.length >= 2;
		suppliers.add(vertices[0]);
		demanders.add(vertices[1]);
		for (int i = 2; i < vertices.length; i++) {
			int r = rand.nextInt(3);
			if (r == 0) {
				suppliers.add(vertices[i]);
			} else if (r == 1) {
				demanders.add(vertices[i]);
			} else {
				/* do nothing */
			}
		}

		Int2DoubleMap capacity = new Int2DoubleOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacity(e));

		IntSet demandersSet = new IntOpenHashSet(demanders);
		IntList suppliersList = new IntArrayList(suppliers);

		IWeightsDouble supply = IWeights.createExternalVerticesWeights(g, double.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();
		suppliersLoop: for (;;) {
			if (suppliersList.isEmpty()) {
				assert Math.abs(g.vertices().intStream().mapToDouble(supply::weight).sum()) < 1e-3;
				return supply;
			}

			path.clear();
			visited.clear();

			int supplierIdx = rand.nextInt(suppliersList.size());
			int supplier = suppliersList.getInt(supplierIdx);
			visited.add(supplier);
			dfs: for (int u = supplier;;) {

				/* Find a random edge to deepen the DFS */
				int[] es = g.outEdges(u).toIntArray();
				IntArrays.shuffle(es, rand);
				for (int e : es) {
					if (capacity.get(e) == 0)
						continue;
					assert u == g.edgeSource(e);
					int v = g.edgeTarget(e);
					if (visited.contains(v))
						continue;
					path.add(e);

					if (demandersSet.contains(v))
						/* found an residual path from a supplier to a demander */
						break dfs;

					/* Continue down in the DFS */
					visited.add(v);
					u = v;
					continue dfs;
				}

				/* No more edges to explore */
				if (path.isEmpty()) {
					/* No more residual paths from supplier to any demander, remove supplier from suppliers list */
					suppliersList.set(supplierIdx, suppliersList.getInt(suppliersList.size() - 1));
					suppliersList.removeInt(suppliersList.size() - 1);
					continue suppliersLoop;
				}

				/* Back up in the DFS path one vertex */
				int lastEdge = path.popInt();
				assert u == g.edgeTarget(lastEdge);
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from a supplier to a demander */
			double delta = path.intStream().mapToDouble(capacity::get).min().getAsDouble();
			assert delta > 0;
			for (int e : path)
				capacity.put(e, capacity.get(e) - delta);

			/* Add lower bounds to some of the edges */
			int source = g.edgeSource(path.getInt(0));
			int sink = g.edgeTarget(path.getInt(path.size() - 1));
			if (rand.nextBoolean()) {
				double s = rand.nextDouble() * ((delta + 1) / 2);
				supply.set(source, supply.weight(source) + s);
				supply.set(sink, supply.weight(sink) - s);
			}
		}
	}

	static void assertSupplySatisfied(IntGraph g, IFlowNetwork net, IWeightFunction supply) {
		for (int v : g.vertices()) {
			double supplySum = 0;
			for (int e : g.outEdges(v))
				supplySum += net.getFlow(e);
			for (int e : g.inEdges(v))
				supplySum -= net.getFlow(e);
			assertEquals(supply.weight(v), supplySum, 1e-9);
		}
	}

	static IntSet verticesWithPositiveSupply(IntCollection vertices, IWeightFunction supply) {
		IntSet sources = new IntOpenHashSet();
		for (int v : vertices)
			if (supply.weight(v) > 0)
				sources.add(v);
		return sources;
	}

	static IntSet verticesWithNegativeSupply(IntCollection vertices, IWeightFunction supply) {
		IntSet sources = new IntOpenHashSet();
		for (int v : vertices)
			if (supply.weight(v) < 0)
				sources.add(v);
		return sources;
	}

}
