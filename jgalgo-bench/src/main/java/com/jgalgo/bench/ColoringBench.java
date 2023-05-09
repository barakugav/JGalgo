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

package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
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
import com.jgalgo.Coloring;
import com.jgalgo.ColoringDSatur;
import com.jgalgo.ColoringGreedy;
import com.jgalgo.ColoringGreedyRandom;
import com.jgalgo.ColoringRecursiveLargestFirst;
import com.jgalgo.Graph;
import com.jgalgo.bench.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class ColoringBench {

	@Param({ "|V|=100 |E|=100", "|V|=200 |E|=1000", "|V|=1600 |E|=10000" })
	public String args;

	private List<Graph> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x566c25f996355cb4L);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			graphs.add(g);
		}
	}

	private void benchColoring(Supplier<? extends Coloring> builder, Blackhole blackhole) {
		Graph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Coloring algo = builder.get();
		Coloring.Result res = algo.computeColoring(g);
		blackhole.consume(res);
	}

	@Benchmark
	public void Greedy(Blackhole blackhole) {
		benchColoring(ColoringGreedy::new, blackhole);
	}

	@Benchmark
	public void GreedyRandom(Blackhole blackhole) {
		final SeedGenerator seedGen = new SeedGenerator(0xefeae78aba502d4aL);
		benchColoring(() -> new ColoringGreedyRandom(seedGen.nextSeed()), blackhole);
	}

	@Benchmark
	public void DSatur(Blackhole blackhole) {
		benchColoring(ColoringDSatur::new, blackhole);
	}

	@Benchmark
	public void RecursiveLargestFirst(Blackhole blackhole) {
		benchColoring(ColoringRecursiveLargestFirst::new, blackhole);
	}

}
