package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
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

import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.UnionFind;
import com.jgalgo.UnionFindArray;
import com.jgalgo.UnionFindPtr;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class UnionFindBench {

	@Param
	public GraphSize graphSize;

	public static enum GraphSize {
		v64_e256, v512_e4096, v4096_e16384, v20000_e50000;

		final int n, m;

		GraphSize() {
			String[] strs = toString().split("_");
			assert strs.length == 2;
			this.n = Integer.parseInt(strs[0].substring(1));
			this.m = Integer.parseInt(strs[1].substring(1));
		}
	}

	private List<Pair<Graph, int[]>> graphs;

	@Setup(Level.Iteration)
	public void setup() {
		final SeedGenerator seedGen = new SeedGenerator(0xecbc984604fcd0afL);
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Graph g = GraphsTestUtils.randGraph(graphSize.n, graphSize.m, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			/*
			 * sort the edges in setup instead of using standard Kruskal MST implementation
			 * during benchmark to isolate union find operations
			 */
			int[] edges = g.edges().toIntArray();
			IntArrays.parallelQuickSort(edges, w);

			graphs.add(Pair.of(g, edges));
		}
	}

	private void benchUnionFind(IntFunction<? extends UnionFind> builder, Blackhole blackhole) {
		for (Pair<Graph, int[]> graph : graphs) {
			IntCollection mst = calcMSTKruskal(graph.first(), graph.second(), builder);
			blackhole.consume(mst);
		}
	}

	private static IntCollection calcMSTKruskal(Graph g, int[] edges, IntFunction<? extends UnionFind> ufBuilder) {
		/* !! assume the edge array is sorted by weight !! */
		int n = g.vertices().size();

		/* create union find data structure for each vertex */
		UnionFind uf = ufBuilder.apply(n);

		/* iterate over the edges and build the MST */
		IntCollection mst = new IntArrayList(n - 1);
		for (int e : edges) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst.add(e);
			}
		}
		return mst;
	}

	@Benchmark
	public void benchUnionFindArray(Blackhole blackhole) {
		benchUnionFind(UnionFindArray::new, blackhole);
	}

	@Benchmark
	public void benchUnionFindPtr(Blackhole blackhole) {
		benchUnionFind(UnionFindPtr::new, blackhole);
	}

}