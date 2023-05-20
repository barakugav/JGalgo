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

import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Johnson's algorithm for all pairs shortest path.
 * <p>
 * Calculate the shortest path between each pair of vertices in a graph in \(O(n m + n^2 \log n)\) time using \(O(n^2)\)
 * space. Negative weights are supported.
 * <p>
 * The algorithm is faster than using {@link SSSPBellmanFord} \(n\) times, as it uses {@link SSSPBellmanFord} once to
 * compute a potential for each vertex, resulting in an equivalent positive weight function, allowing us to use
 * {@link SSSPDijkstra} from each vertex as a source.
 *
 * @author Barak Ugav
 */
public class APSPJohnson implements APSP {

	private SSSP negativeSssp = new SSSPBellmanFord();
	private boolean parallel = Config.parallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a new APSP algorithm object.
	 */
	public APSPJohnson() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public APSP.Result computeAllShortestPaths(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyDirected(g);
		int n = g.vertices().size();

		boolean negWeight = false;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (w.weight(e) < 0) {
				negWeight = true;
				break;
			}
		}

		if (!negWeight) {
			/* No negative weights, no need for potential */
			SuccessRes res = computeAPSPPositive(g, w);
			res.potential = new double[n];
			return res;
		}

		Pair<double[], Path> potential0 = calcPotential(g, w);
		if (potential0.second() != null)
			return new NegCycleRes(potential0.second());
		double[] potential = potential0.first();

		EdgeWeightFunc wPotential;
		if (w instanceof EdgeWeightFunc.Int) {
			EdgeWeightFunc.Int wInt = (EdgeWeightFunc.Int) w;
			EdgeWeightFunc.Int wPotentialInt =
					e -> wInt.weightInt(e) + (int) potential[g.edgeSource(e)] - (int) potential[g.edgeTarget(e)];
			wPotential = wPotentialInt;
		} else {
			wPotential = e -> w.weight(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
		}
		SuccessRes res = computeAPSPPositive(g, wPotential);
		res.potential = potential;
		return res;
	}

	private SuccessRes computeAPSPPositive(Graph g, EdgeWeightFunc w) {
		final int n = g.vertices().size();
		SuccessRes res = new SuccessRes(n);

		RecursiveAction[] tasks = new RecursiveAction[n];
		for (int source = 0; source < n; source++) {
			final int source0 = source;
			tasks[source] = Utils.recursiveAction(
					() -> res.ssspResults[source0] = new SSSPDijkstra().computeShortestPaths(g, w, source0));
		}

		ForkJoinPool pool = Utils.getPool();
		if (n < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			for (int source = 0; source < n; source++)
				tasks[source].invoke();

		} else {
			/* parallel */
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (int source = 0; source < n; source++)
				tasks[source].join();
		}
		return res;
	}

	private Pair<double[], Path> calcPotential(Graph g, EdgeWeightFunc w) {
		int n = g.vertices().size();
		Graph refG = GraphBuilder.newDirected().setVerticesNum(n + 1).build();
		Weights.Int edgeEef = refG.addEdgesWeights("edgeEef", int.class, Integer.valueOf(-1));
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				int refE = refG.addEdge(u, v);
				edgeEef.set(refE, e);
			}
		}

		/* Add fake vertex */
		final int fakeV = n;
		final int fakeEdge = -1;
		for (int v = 0; v < n; v++)
			edgeEef.set(refG.addEdge(fakeV, v), fakeEdge);

		EdgeWeightFunc refW = e -> {
			int ref = edgeEef.getInt(e);
			return ref != fakeEdge ? w.weight(ref) : 0;
		};
		SSSP.Result res = negativeSssp.computeShortestPaths(refG, refW, fakeV);
		if (!res.foundNegativeCycle()) {
			double[] potential = new double[n];
			for (int v = 0; v < n; v++)
				potential[v] = res.distance(v);
			return Pair.of(potential, null);
		} else {
			Path negCycleRef = res.getNegativeCycle();
			IntList negCycle = new IntArrayList(negCycleRef.size());
			for (IntIterator it = negCycleRef.iterator(); it.hasNext();)
				negCycle.add(edgeEef.getInt(it.nextInt()));
			return Pair.of(null, new Path(g, negCycleRef.source(), negCycleRef.target(), negCycle));
		}
	}

	/**
	 * Set the algorithm used for negative weights graphs.
	 * <p>
	 * The algorithm first calculate a potential for each vertex using an SSSP algorithm for negative weights, than
	 * construct an equivalent positive weight function which is used by an SSSP algorithm for positive weights to
	 * compute all shortest paths.
	 *
	 * @param algo a SSSP implementation for graphs with negative weight function
	 */
	void setNegativeSsspAlgo(SSSP algo) {
		negativeSssp = Objects.requireNonNull(algo);
	}

	private static class NegCycleRes implements APSP.Result {

		private final Path negCycle;

		public NegCycleRes(Path negCycle) {
			Objects.requireNonNull(negCycle);
			this.negCycle = negCycle;
		}

		@Override
		public double distance(int source, int target) {
			throw new IllegalStateException();
		}

		@Override
		public Path getPath(int source, int target) {
			throw new IllegalStateException();
		}

		@Override
		public boolean foundNegativeCycle() {
			return true;
		}

		@Override
		public Path getNegativeCycle() {
			return negCycle;
		}

	}

	private static class SuccessRes implements APSP.Result {

		final SSSP.Result[] ssspResults;
		double[] potential;

		SuccessRes(int n) {
			ssspResults = new SSSP.Result[n];
		}

		@Override
		public double distance(int source, int target) {
			return ssspResults[source].distance(target) + potential[target] - potential[source];
		}

		@Override
		public Path getPath(int source, int target) {
			return ssspResults[source].getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException();
		}

	}

}
