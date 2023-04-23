package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import it.unimi.dsi.fastutil.ints.IntArrays;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class UnionFindBench {

	@Param({ "|V|=64 |E|=256", "|V|=512 |E|=4096", "|V|=4096 |E|=16384", "|V|=20000 |E|=50000" })
	public String graphSize;
	private int n, m;

	private List<Pair<Graph, int[]>> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xecbc984604fcd0afL);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			/*
			 * sort the edges in setup instead of using standard Kruskal MST implementation
			 * during benchmark to isolate union find operations
			 */
			int[] edges = g.edges().toIntArray();
			IntArrays.parallelQuickSort(edges, w);

			int[] edgesWithEndpoint = new int[edges.length * 3];
			for (int i = 0; i < edges.length; i++) {
				int e = edges[i];
				int u = g.edgeSource(e);
				int v = g.edgeTarget(e);
				edgesWithEndpoint[i * 3 + 0] = e;
				edgesWithEndpoint[i * 3 + 1] = u;
				edgesWithEndpoint[i * 3 + 2] = v;
			}

			graphs.add(Pair.of(g, edgesWithEndpoint));
		}
	}

	private void benchUnionFindByRunningMSTKruskal(IntFunction<? extends UnionFind> builder, Blackhole blackhole) {
		Pair<Graph, int[]> graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		int[] mst = calcMSTKruskal(graph.first(), graph.second(), builder);
		blackhole.consume(mst);
	}

	private static int[] calcMSTKruskal(Graph g, int[] edges, IntFunction<? extends UnionFind> ufBuilder) {
		/* !! assume the edge array is sorted by weight !! */
		int n = g.vertices().size();

		/* create union find data structure for each vertex */
		UnionFind uf = ufBuilder.apply(n);

		/* iterate over the edges and build the MST */
		int[] mst = new int[n - 1];
		int mstSize = 0;
		for (int i = 0; i < edges.length / 3; i++) {
			int e = edges[i * 3 + 0];
			int u = edges[i * 3 + 1];
			int v = edges[i * 3 + 2];

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst[mstSize++] = e;
			}
		}
		return mst;
	}

	@Benchmark
	public void benchUnionFindArray(Blackhole blackhole) {
		benchUnionFindByRunningMSTKruskal(UnionFindArray::new, blackhole);
	}

	@Benchmark
	public void benchUnionFindPtr(Blackhole blackhole) {
		benchUnionFindByRunningMSTKruskal(UnionFindPtr::new, blackhole);
	}

}
