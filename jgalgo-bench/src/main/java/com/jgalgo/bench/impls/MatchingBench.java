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
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.Matching;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsBool;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MatchingBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumCardinality {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<IntGraph> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0x2c942284cf26134dL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			IntGraph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Matching matching = algo.computeMaximumCardinalityMatching(g);
			blackhole.consume(matching);
		}

		@Benchmark
		public void CardinalityGabow1976(Blackhole blackhole) {
			benchAlgo(getAlgo("cardinality-gabow-1976"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumCardinalityBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<IntGraph> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xacff2ce7f7ee4fc9L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			IntGraph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			Matching matching = algo.computeMaximumCardinalityMatching(g);
			blackhole.consume(matching);
		}

		@Benchmark
		public void CardinalityBipartiteHopcroftKarp(Blackhole blackhole) {
			benchAlgo(getAlgo("cardinality-bipartite-hopcroft-karp"), blackhole);
		}

		@Benchmark
		public void CardinalityGabow1976(Blackhole blackhole) {
			benchAlgo(getAlgo("cardinality-gabow-1976"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumWeighted {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<IntGraph, IWeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xd857250c5ffe0823L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			Pair<IntGraph, IWeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			IntGraph g = gw.first();
			IWeightFunction w = gw.second();
			Matching matching = algo.computeMaximumWeightedMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("gabow-1990-simpler"), blackhole);
		}

		@Benchmark
		public void Gabow1990(Blackhole blackhole) {
			benchAlgo(getAlgo("gabow-1990"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("blossom-v"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MaximumWeightedBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<IntGraph, IWeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0x39a998645277eca3L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			Pair<IntGraph, IWeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			IntGraph g = gw.first();
			IWeightFunction w = gw.second();
			Matching matching = algo.computeMaximumWeightedMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void BipartiteHungarianMethod(Blackhole blackhole) {
			benchAlgo(getAlgo("bipartite-hungarian-method"), blackhole);
		}

		@Benchmark
		public void BipartiteSSSP(Blackhole blackhole) {
			benchAlgo(getAlgo("bipartite-sssp"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("blossom-v"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MinimumPerfect {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<IntGraph, IWeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0xd15309f552f84f10L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());

				if (g.vertices().size() % 2 != 0)
					throw new IllegalArgumentException("there is no perfect matching");

				MatchingAlgo cardinalityAlgo = MatchingAlgo.newBuilder().setCardinality(true).build();
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

				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			Pair<IntGraph, IWeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			IntGraph g = gw.first();
			IWeightFunction w = gw.second();
			Matching matching = algo.computeMinimumWeightedPerfectMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("gabow-1990-simpler"), blackhole);
		}

		@Benchmark
		public void Gabow1990(Blackhole blackhole) {
			benchAlgo(getAlgo("gabow-1990"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("blossom-v"), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class MinimumPerfectBipartite {

		@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
		public String args;

		private List<Pair<IntGraph, IWeightFunction>> graphs;
		private final int graphsNum = 31;
		private final AtomicInteger graphIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0x6afda59c8a3dee81L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randGraphBipartite(n / 2, n / 2, m, seedGen.nextSeed());
				IWeightsBool partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);

				MatchingAlgo cardinalityAlgo =
						MatchingAlgo.newBuilder().setCardinality(true).setBipartite(true).build();
				Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
				IntList unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
				IntList unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
				unmatchedVerticesS.removeIf(v -> partition.get(v));
				unmatchedVerticesT.removeIf(v -> !partition.get(v));
				assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
				IntLists.shuffle(unmatchedVerticesS, new Random(seedGen.nextSeed()));
				IntLists.shuffle(unmatchedVerticesT, new Random(seedGen.nextSeed()));
				for (int i = 0; i < unmatchedVerticesS.size(); i++) {
					int u = unmatchedVerticesS.getInt(i);
					int v = unmatchedVerticesT.getInt(i);
					g.addEdge(u, v);
				}
				assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
				graphs.add(Pair.of(g, w));
			}
		}

		private void benchAlgo(MatchingAlgo algo, Blackhole blackhole) {
			Pair<IntGraph, IWeightFunction> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
			IntGraph g = gw.first();
			IWeightFunction w = gw.second();
			Matching matching = algo.computeMinimumWeightedPerfectMatching(g, w);
			blackhole.consume(matching);
		}

		@Benchmark
		public void BipartiteHungarianMethod(Blackhole blackhole) {
			benchAlgo(getAlgo("bipartite-hungarian-method"), blackhole);
		}

		@Benchmark
		public void Gabow1990Simpler(Blackhole blackhole) {
			benchAlgo(getAlgo("gabow-1990-simpler"), blackhole);
		}

		@Benchmark
		public void BlossomV(Blackhole blackhole) {
			benchAlgo(getAlgo("blossom-v"), blackhole);
		}

	}

	private static MatchingAlgo getAlgo(String name) {
		return MatchingAlgo.newBuilder().setOption("impl", name).build();
	}

}
