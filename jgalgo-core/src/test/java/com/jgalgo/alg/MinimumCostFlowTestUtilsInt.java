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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class MinimumCostFlowTestUtilsInt extends TestUtils {

	static void testMinCostMaxFlowWithSourceSink(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			IWeightFunctionInt cost = randCost(g, rand);
			IntIntPair sourceSink = MinimumCutSTTestUtils.chooseSourceSink(g, rand);

			testMinCostMaxFlowWithSourceSink(g, net, cost, sourceSink.firstInt(), sourceSink.secondInt(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourceSinkLowerBound(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			IntIntPair sourceSink = MinimumCutSTTestUtils.chooseSourceSink(g, rand);
			int source = sourceSink.firstInt();
			int sink = sourceSink.secondInt();
			IWeightFunctionInt cost = randCost(g, rand);
			IWeightFunctionInt lowerBound = randLowerBound(g, net, source, sink, rand);

			testMinCostMaxFlowWithSourceSinkLowerBound(g, net, cost, lowerBound, source, sink, algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinks(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			IWeightFunctionInt cost = randCost(g, rand);
			Pair<IntCollection, IntCollection> sourcesSinks = MinimumCutSTTestUtils.chooseMultiSourceMultiSink(g, rand);

			testMinCostMaxFlowWithSourcesSinks(g, net, cost, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testMinCostMaxFlowWithSourcesSinksLowerBound(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			Pair<IntCollection, IntCollection> sourcesSinks = MinimumCutSTTestUtils.chooseMultiSourceMultiSink(g, rand);
			IntCollection sources = sourcesSinks.first();
			IntCollection sinks = sourcesSinks.second();
			IWeightFunctionInt cost = randCost(g, rand);
			IWeightFunctionInt lowerBound = randLowerBound(g, net, sources, sinks, rand);

			testMinCostMaxFlowWithSourcesSinksLowerBound(g, net, cost, lowerBound, sources, sinks, algo);
		});
	}

	static void testMinCostFlowWithSupply(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			IWeightFunctionInt cost = randCost(g, rand);
			IWeightFunctionInt supply = FlowCirculationTestUtils.randSupplyInt(g, net, rand);

			testMinCostFlowWithSupply(g, net, cost, supply, algo);
		});
	}

	static void testMinCostFlowWithSupplyLowerBound(MinimumCostFlow algo, long seed) {
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

			IFlowNetworkInt net = randNetwork(g, rand);
			IWeightFunctionInt supply = FlowCirculationTestUtils.randSupplyInt(g, net, rand);

			/* build a 'random' lower bound by solving min-cost flow with a different cost function and use the flows */
			IWeightFunctionInt cost1 = randCost(g, rand);
			MinimumCostFlow.newInstance().computeMinCostFlow(g, net, cost1, supply);
			IWeightsInt lowerBound = IWeights.createExternalEdgesWeights(g, int.class);
			for (int e : g.edges()) {
				lowerBound.set(e, (int) (net.getFlowInt(e) * 0.4 * rand.nextDouble()));
				net.setFlow(e, 0);
			}

			IWeightFunctionInt cost = randCost(g, rand);

			testMinCostFlowWithSupplyLowerBound(g, net, cost, lowerBound, supply, algo);
		});
	}

	private static IFlowNetworkInt randNetwork(IntGraph g, Random rand) {
		IFlowNetworkInt net = IFlowNetworkInt.createFromEdgeWeights(g);
		for (int e : g.edges())
			net.setCapacity(e, 400 + rand.nextInt(1024));
		return net;
	}

	private static IWeightFunctionInt randCost(IntGraph g, Random rand) {
		IWeightsInt cost = IWeights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges())
			cost.set(e, rand.nextInt(2424) - 600);
		return cost;
	}

	private static IWeightFunctionInt randLowerBound(IntGraph g, IFlowNetworkInt net, int source, int sink,
			Random rand) {
		return randLowerBound(g, net, IntSets.singleton(source), IntSets.singleton(sink), rand);
	}

	private static IWeightFunctionInt randLowerBound(IntGraph g, IFlowNetworkInt net, IntCollection sources,
			IntCollection sinks, Random rand) {
		Assertions.Graphs.onlyDirected(g);

		Int2IntMap capacity = new Int2IntOpenHashMap();
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));

		IntSet sinksSet = new IntOpenHashSet(sinks);
		IntList sourcesList = new IntArrayList(sources);

		IWeightsInt lowerBound = IWeights.createExternalEdgesWeights(g, int.class);
		IntArrayList path = new IntArrayList();
		IntSet visited = new IntOpenHashSet();
		sourcesLoop: for (;;) {
			if (sourcesList.isEmpty())
				return lowerBound;

			path.clear();
			visited.clear();

			int sourceIdx = rand.nextInt(sourcesList.size());
			int source = sourcesList.getInt(sourceIdx);
			visited.add(source);
			dfs: for (int u = source;;) {

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

					if (sinksSet.contains(v))
						/* found an residual path from source to sink */
						break dfs;

					/* Continue down in the DFS */
					visited.add(v);
					u = v;
					continue dfs;
				}

				/* No more edges to explore */
				if (path.isEmpty()) {
					/* No more residual paths from source to any sink, remove source from sources list */
					sourcesList.set(sourceIdx, sourcesList.getInt(sourcesList.size() - 1));
					sourcesList.removeInt(sourcesList.size() - 1);
					continue sourcesLoop;
				}

				/* Back up in the DFS path one vertex */
				int lastEdge = path.popInt();
				assert u == g.edgeTarget(lastEdge);
				u = g.edgeSource(lastEdge);
			}

			/* Found a residual path from source to sink */
			int delta = path.intStream().map(capacity::get).min().getAsInt();
			assert delta > 0;
			for (int e : path)
				capacity.put(e, capacity.get(e) - delta);

			/* Add lower bounds to some of the edges */
			IntList lowerBoundEdges = new IntArrayList(path);
			IntLists.shuffle(lowerBoundEdges, rand);
			if (lowerBoundEdges.size() == 2) {
				lowerBoundEdges.removeInt(1);
			} else if (lowerBoundEdges.size() > 2) {
				lowerBoundEdges.removeElements(2, lowerBoundEdges.size());
			}
			for (int e : lowerBoundEdges) {
				int boundDelta = delta / 2 + rand.nextInt((delta + 1) / 2);
				lowerBound.set(e, lowerBound.weightInt(e) + boundDelta);
			}
		}
	}

	private static void testMinCostMaxFlowWithSourceSink(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt cost,
			int source, int sink, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);

		assertMaximumFlow(g, net, null, IntList.of(source), IntList.of(sink));
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostMaxFlowWithSourceSinkLowerBound(IntGraph g, IFlowNetworkInt net,
			IWeightFunctionInt cost, IWeightFunctionInt lowerBound, int source, int sink, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, IntList.of(source), IntList.of(sink));
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void testMinCostMaxFlowWithSourcesSinks(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt cost,
			IntCollection sources, IntCollection sinks, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);

		assertMaximumFlow(g, net, null, sources, sinks);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostMaxFlowWithSourcesSinksLowerBound(IntGraph g, IFlowNetworkInt net,
			IWeightFunctionInt cost, IWeightFunctionInt lowerBound, IntCollection sources, IntCollection sinks,
			MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
		double totalFlow = net.getFlowSum(g, sources);
		MaximumFlowTestUtils.assertValidFlow(g, net, sources, sinks, totalFlow);
		assertLowerBound(g, net, lowerBound);

		assertMaximumFlow(g, net, lowerBound, sources, sinks);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void testMinCostFlowWithSupply(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt cost,
			IWeightFunctionInt supply, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, net, supply);
		assertOptimalCirculation(g, net, cost, null);
	}

	private static void testMinCostFlowWithSupplyLowerBound(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt cost,
			IWeightFunctionInt lowerBound, IWeightFunctionInt supply, MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostFlow(g, net, cost, lowerBound, supply);
		MaximumFlowTestUtils.assertValidFlow(g, net,
				FlowCirculationTestUtils.verticesWithPositiveSupply(g.vertices(), supply),
				FlowCirculationTestUtils.verticesWithNegativeSupply(g.vertices(), supply));

		FlowCirculationTestUtils.assertSupplySatisfied(g, net, supply);
		assertLowerBound(g, net, lowerBound);
		assertOptimalCirculation(g, net, cost, lowerBound);
	}

	private static void assertLowerBound(IntGraph g, IFlowNetwork net, IWeightFunction lowerBound) {
		for (int e : g.edges())
			assertTrue(net.getFlow(e) >= lowerBound.weight(e));
	}

	private static void assertMaximumFlow(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt lowerBound,
			IntCollection sources, IntCollection sinks) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a flow is a maximum flow if no augmenting path can be found in the residual network. Search for one to verify
		 * the given flow is maximum.
		 */

		Int2IntMap capacity = new Int2IntOpenHashMap(g.edges().size());
		Int2IntMap flow = new Int2IntOpenHashMap(g.edges().size());
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (int e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (int e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.get(e) >= l);
				capacity.put(e, capacity.get(e) - l);
				flow.put(e, flow.get(e) - l);
			}
		}

		// perform BFS and find a path of non saturated edges from the sources to the sinks
		IntSet visited = new IntOpenHashSet();
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		for (int source : sources) {
			queue.enqueue(source);
			visited.add(source);
		}
		IntSet sinksSet = new IntOpenHashSet(sinks);
		for (;;) {
			if (queue.isEmpty())
				return; /* no path to sink, flow is maximum */
			int u = queue.dequeueInt();
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (flow.get(e) == capacity.get(e))
					continue; /* saturated */
				int v = eit.targetInt();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
			for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (flow.get(e) == 0)
					continue; /* saturated */
				int v = eit.sourceInt();
				if (visited.contains(v))
					continue;

				/* if we found an augmenting path, we can push on it, flow is not maximum */
				assertFalse(sinksSet.contains(v));

				visited.add(v);
				queue.enqueue(v);
			}
		}
	}

	private static void assertOptimalCirculation(IntGraph g, IFlowNetworkInt net, IWeightFunctionInt cost,
			IWeightFunctionInt lowerBound) {
		Assertions.Graphs.onlyDirected(g);

		/*
		 * a circulation is optimal with respect to a cost function if no circle with negative mean cost exists in the
		 * graph residual network. Search for one to verify the circulation is optimal.
		 */

		Int2IntMap capacity = new Int2IntOpenHashMap(g.edges().size());
		Int2IntMap flow = new Int2IntOpenHashMap(g.edges().size());
		for (int e : g.edges())
			capacity.put(e, net.getCapacityInt(e));
		for (int e : g.edges())
			flow.put(e, net.getFlowInt(e));
		if (lowerBound != null) {
			for (int e : g.edges()) {
				int l = lowerBound.weightInt(e);
				assertTrue(flow.get(e) >= l);
				capacity.put(e, capacity.get(e) - l);
				flow.put(e, flow.get(e) - l);
			}
		}

		/* build the residual graph */
		IntGraphBuilder b = IntGraphBuilder.newDirected();
		for (int v : g.vertices())
			b.addVertex(v);
		IWeightsInt residualWeights = b.addEdgesWeights("cost", int.class);
		for (int e : g.edges()) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int cap = capacity.get(e);
			int f = flow.get(e);
			boolean isResidual = f < cap;
			boolean isRevResidual = f > 0;
			int c = cost.weightInt(e);
			if (isResidual) {
				int residualE = b.addEdge(u, v);
				residualWeights.set(residualE, c);
			}
			if (isRevResidual) {
				int residualE = b.addEdge(v, u);
				residualWeights.set(residualE, -c);
			}
		}
		IntGraph residualGraph = b.build();
		residualWeights = residualGraph.getEdgesWeights("cost");

		/* the circulation is optimal if no circle with negative mean cost exists */
		IPath cycle = MinimumMeanCycle.newInstance().computeMinimumMeanCycle(residualGraph, residualWeights);
		assertTrue(cycle == null || residualWeights.weightSum(cycle.edges()) >= 0,
				"Negative cycle found in residual graph, the circulation is not optimal");
	}

}
