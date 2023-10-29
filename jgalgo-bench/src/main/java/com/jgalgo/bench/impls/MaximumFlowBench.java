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

package com.jgalgo.bench.impls;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.alg.FlowNetworkInt;
import com.jgalgo.alg.MaximumFlow;
import com.jgalgo.alg.Path;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsInt;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MaximumFlowBench {

	List<MaxFlowTask> graphs;
	final int graphsNum = 31;
	final AtomicInteger graphIdx = new AtomicInteger();

	public void resetFlow() {
		for (MaxFlowTask graph : graphs)
			for (int e : graph.g.edges())
				graph.flow.setFlow(e, 0);
	}

	void benchMaxFlow(MaximumFlow.Builder builder, Blackhole blackhole) {
		MaxFlowTask graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		MaximumFlow algo = builder.build();
		double flow = algo.computeMaximumFlow(graph.g, graph.flow, graph.source, graph.sink);
		blackhole.consume(flow);
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends MaximumFlowBench {

		@Param({ "|V|=1000 Weights=Off", "|V|=1000 Weights=ON", "|V|=2000 Weights=Off", "|V|=2000 Weights=ON",
				"|V|=2500 Weights=Off", "|V|=2500 Weights=ON" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			boolean weighted = argsMap.get("Weights").equals("ON");
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0x94fc6ec413f60392L);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphGnp(n, directed, seedGen.nextSeed());
				FlowNetworkInt flow = weighted ? randNetworkInt(g, seedGen.nextSeed()) : unweightedNetwork(g);

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends MaximumFlowBench {

		@Param({ "|V|=3000 Weights=Off", "|V|=3000 Weights=ON", "|V|=4500 Weights=Off", "|V|=4500 Weights=ON",
				"|V|=6000 Weights=Off", "|V|=6000 Weights=ON" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			boolean weighted = argsMap.get("Weights").equals("ON");
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0xdc6c4cf7f4d3843cL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, directed, seedGen.nextSeed());
				FlowNetworkInt flow = weighted ? randNetworkInt(g, seedGen.nextSeed()) : unweightedNetwork(g);

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void DinicDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic-dynamic-trees"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}

		/* way too slow, isn't close to compete with the other implementations */
		// @Benchmark
		// public void PushRelabelDynamicTrees(Blackhole blackhole) {
		// benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo-dynamic-trees"), blackhole);
		// }
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 200, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends MaximumFlowBench {

		@Param({ "|V|=1500 |E|=5000 Weights=Off", "|V|=1500 |E|=5000 Weights=ON", "|V|=2500 |E|=8000 Weights=Off",
				"|V|=2500 |E|=8000 Weights=ON", "|V|=4000 |E|=16000 Weights=Off", "|V|=4000 |E|=16000 Weights=ON" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));
			boolean weighted = argsMap.get("Weights").equals("ON");
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0x9716aede5cfa6eabL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, directed, seedGen.nextSeed());
				FlowNetworkInt flow = weighted ? randNetworkInt(g, seedGen.nextSeed()) : unweightedNetwork(g);

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void DinicDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic-dynamic-trees"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}

		/* way too slow, isn't close to compete with the other implementations */
		// @Benchmark
		// public void PushRelabelDynamicTrees(Blackhole blackhole) {
		// benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo-dynamic-trees"), blackhole);
		// }
	}

	private static class MaxFlowTask {
		final IntGraph g;
		final FlowNetworkInt flow;
		final int source;
		final int sink;

		MaxFlowTask(IntGraph g, FlowNetworkInt flow, int source, int sink) {
			this.g = g;
			this.flow = flow;
			this.source = source;
			this.sink = sink;
		}
	}

	private static FlowNetworkInt randNetworkInt(IntGraph g, long seed) {
		Random rand = new Random(seed);
		IWeightsInt capacities = IWeights.createExternalEdgesWeights(g, int.class);
		IWeightsInt flows = IWeights.createExternalEdgesWeights(g, int.class);
		FlowNetworkInt net = FlowNetworkInt.createFromEdgeWeights(capacities, flows);
		for (int e : g.edges())
			net.setCapacity(e, 5000 + rand.nextInt(16384));
		return net;
	}

	private static FlowNetworkInt unweightedNetwork(IntGraph g) {
		IWeightsInt capacities = IWeights.createExternalEdgesWeights(g, int.class, Integer.valueOf(1));
		IWeightsInt flows = IWeights.createExternalEdgesWeights(g, int.class);
		return FlowNetworkInt.createFromEdgeWeights(capacities, flows);
	}

	private static IntIntPair chooseSourceSink(IntGraph g, Random rand) {
		int source, sink;
		for (int[] vs = g.vertices().toIntArray();;) {
			source = vs[rand.nextInt(vs.length)];
			sink = vs[rand.nextInt(vs.length)];
			if (source != sink && Path.findPath(g, source, sink) != null)
				return IntIntPair.of(source, sink);
		}
	}

	static Pair<IntCollection, IntCollection> chooseMultiSourceMultiSink(IntGraph g, Random rand) {
		final int n = g.vertices().size();
		final int sourcesNum;
		final int sinksNum;
		if (n < 2) {
			throw new IllegalArgumentException("too few vertices");
		} else if (n < 4) {
			sourcesNum = sinksNum = 1;
		} else if (n <= 6) {
			sourcesNum = sinksNum = 2;
		} else {
			sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
		}

		IntCollection sources = new IntOpenHashSet(sourcesNum);
		IntCollection sinks = new IntOpenHashSet(sinksNum);
		for (int[] vs = g.vertices().toIntArray();;) {
			if (sources.size() < sourcesNum) {
				int source = vs[rand.nextInt(vs.length)];
				if (!sinks.contains(source))
					sources.add(source);

			} else if (sinks.size() < sinksNum) {
				int sink = vs[rand.nextInt(vs.length)];
				if (!sources.contains(sink))
					sinks.add(sink);
			} else {
				break;
			}
		}
		return Pair.of(sources, sinks);
	}

}
