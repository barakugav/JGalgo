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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Goldberg's SSSP algorithm for integer (positive and negative) weights on directed graphs.
 * <p>
 * The algorithm operate on integer weights and uses the scaling approach. During the scaling iterations, a potential
 * function is maintained, which gives a equivalent weight function with values \(-1,0,1,2,3,\ldots\). The potential is
 * updated from iteration to iteration, until the full representation of the integer numbers is used, and the real
 * shortest paths and distances are computed. Let \(N\) be the absolute value of the minimum negative number. The
 * algorithm perform \(O(\log N)\) iteration, and each iteration is performed in time \(O(m \sqrt{n})\) time. In total,
 * the running time is \(O(m \sqrt{n} \log N)\).
 * <p>
 * This algorithm is great in practice, and should be used for weights function with integer negative values.
 * <p>
 * Based on 'Scaling algorithms for the shortest paths problem' by Goldberg, A.V. (1995).
 *
 * @author Barak Ugav
 */
class ShortestPathSingleSourceGoldberg extends ShortestPathSingleSourceUtils.AbstractImpl
		implements AlgorithmWithDiagnostics {

	private ShortestPathSingleSource positiveSsspAlgo = ShortestPathSingleSource.newInstance();
	private final ShortestPathSingleSourceDial ssspDial = new ShortestPathSingleSourceDial();
	private final ShortestPathSingleSource dagSssp = ShortestPathSingleSource.newBuilder().setDag(true).build();
	private final StronglyConnectedComponentsAlgo ccAlg = StronglyConnectedComponentsAlgo.newInstance();

	private final Diagnostics diagnostics = new Diagnostics();

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceGoldberg() {}

	/**
	 * Set the algorithm used for positive weights graphs.
	 * <p>
	 * The algorithm first calculate a potential for each vertex and construct an equivalent positive weight function
	 * which is used by an SSSP algorithm for positive weights to compute the final shortest paths.
	 *
	 * @param algo a SSSP implementation for graphs with positive weight function
	 */
	void setPositiveSsspAlgo(ShortestPathSingleSource algo) {
		positiveSsspAlgo = Objects.requireNonNull(algo);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed or the edge weights function is not of type
	 *                                      {@link WeightFunction.Int}
	 */
	@Override
	ShortestPathSingleSource.Result computeShortestPaths(IndexGraph g, WeightFunction w, int source) {
		Assertions.Graphs.onlyDirected(g);
		if (w == null)
			w = WeightFunction.CardinalityWeightFunction;
		if (!(w instanceof WeightFunction.Int))
			throw new IllegalArgumentException("Only integer weights are supported");
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		return computeShortestPaths0(g, (WeightFunction.Int) w, source);
	}

	private ShortestPathSingleSource.Result computeShortestPaths0(IndexGraph g, WeightFunction.Int w, int source) {
		int minWeight = Integer.MAX_VALUE;

		for (int m = g.edges().size(), e = 0; e < m; e++)
			minWeight = Math.min(minWeight, w.weightInt(e));
		if (minWeight >= 0)
			// All weights are positive, use Dijkstra
			return positiveSsspAlgo.computeShortestPaths(g, w, source);

		/* calculate a potential function (or find a negative cycle) */
		Pair<int[], Path> p = calcPotential(g, w, minWeight);
		if (p.second() != null)
			return Result.ofNegCycle(source, p.second());

		/* create a (positive) weight function using the potential */
		int[] potential = p.first();
		WeightFunction.Int pw = JGAlgoUtils.potentialWeightFunc(g, w, potential);

		/* run positive SSSP */
		ShortestPathSingleSource.Result res = positiveSsspAlgo.computeShortestPaths(g, pw, source);
		return Result.ofSuccess(source, potential, res);
	}

	private Pair<int[], Path> calcPotential(IndexGraph g, WeightFunction.Int w0, int minWeight) {
		diagnostics.runBegin();
		final int n = g.vertices().size();
		final int m = g.edges().size();
		w0 = WeightFunctions.localEdgeWeightFunction(g, w0);
		int[] potential = new int[n];

		BitSet connected = new BitSet(n);
		int[] layerSize = new int[n + 1];

		/* updated weight function including the potential */
		int[] w = new int[m];

		/* gNeg is the graph g with only 0,-1 edges */
		IndexGraph gNeg = IndexGraphFactory.newDirected().expectedVerticesNum(n).newGraph();
		for (int v = 0; v < n; v++)
			gNeg.addVertex();
		int[] gNegEdgeRefs = new int[m];

		/* G is the graph of strong connected components of gNeg, each vertex is a super vertex of gNeg */
		IndexGraph G = IndexGraphFactory.newDirected().expectedVerticesNum(n + 2).newGraph();
		Weights.Int GWeights = G.addEdgesWeights("weights", int.class, Integer.valueOf(-1));
		/* Two fake vertices used to add 0-edges and (r-i)-edges to all other (super) vertices */

		/**
		 * In sparse (random) graphs, the running time seems very slow, as the algorithm require a lot of iterations to
		 * find the potential values, and most of the time is spent in the long-path flow. In these cases, we prefer the
		 * big-layer flow.
		 */
		final double density = (double) g.edges().size() / n * (n + 1) / 2;
		final double alpha = Math.max(0.25, Math.min(3 / -Math.log(density), 2));

		/* Run log(-minWeight) scaling iterations */
		final int minWeightWordsize = JGAlgoUtils.log2(-minWeight);
		for (int weightMask = minWeightWordsize; weightMask >= 0; weightMask--) {
			if (weightMask != minWeightWordsize)
				for (int v = 0; v < n; v++)
					potential[v] *= 2;
			diagnostics.scalingIteration();

			/* updated potential function until there are no more negative vertices with current weight function */
			/* we do at most \sqrt{n} such iterations */
			for (;;) {
				diagnostics.potentialIteration();
				/* update current weight function according to latest potential */
				for (int e = 0; e < m; e++)
					w[e] = calcWeightWithPotential(g, e, w0, potential, weightMask);

				/* populate gNeg with all 0,-1 edges */
				gNeg.clearEdges();
				for (int e = 0; e < m; e++) {
					if (w[e] <= 0) {
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						gNegEdgeRefs[gNeg.addEdge(u, v)] = e;
					}
				}

				/* Find all strong connected components in the graph */
				VertexPartition connectivityRes = ccAlg.findStronglyConnectedComponents(gNeg);
				final int N = connectivityRes.numberOfBlocks();

				/*
				 * Contract each strong connected component and search for a negative edge within it, if found -
				 * negative cycle found
				 */
				G.clear();
				for (int U = 0; U < N; U++)
					G.addVertex();
				for (int u = 0; u < n; u++) {
					int U = connectivityRes.vertexBlock(u);
					for (EdgeIter eit = gNeg.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						int V = connectivityRes.vertexBlock(v);
						int weight = w[gNegEdgeRefs[e]];
						if (U != V) {
							GWeights.set(G.addEdge(U, V), weight);

						} else if (weight < 0) {
							// negative cycle
							Path negCycle0 = Path.findPath(gNeg, v, u);
							IntList negCycle = new IntArrayList(negCycle0.size() + 1);
							for (int e2 : negCycle0)
								negCycle.add(gNegEdgeRefs[e2]);
							negCycle.add(gNegEdgeRefs[e]);
							return Pair.of(null, new PathImpl(g, v, v, negCycle));
						}
					}
				}

				// Create a fake vertex S, connect with 0 edges to all and calc distances
				int fakeS1 = G.addVertex();
				for (int U = 0; U < N; U++)
					GWeights.set(G.addEdge(fakeS1, U), 0);
				ShortestPathSingleSource.Result ssspRes = dagSssp.computeShortestPaths(G, GWeights, fakeS1);

				// Divide super vertices into layers by distance
				int layerNum = 0;
				int vertexInMaxLayer = -1;
				Arrays.fill(layerSize, 0, N, 0);
				for (int V = 0; V < N; V++) {
					int l = -(int) ssspRes.distance(V);
					if (l + 1 > layerNum) {
						layerNum = l + 1;
						vertexInMaxLayer = V;
					}
					layerSize[l]++;
				}
				if (layerNum == 1)
					break; // no negative vertices, done

				// Find biggest layer
				int biggestLayer = -1;
				for (int l = layerNum - 1; l > 0; l--)
					if (biggestLayer == -1 || layerSize[l] > layerSize[biggestLayer])
						biggestLayer = l;
				if (layerSize[biggestLayer] >= Math.sqrt(N) * alpha) {
					diagnostics.bigLayer();
					// A layer with sqrt(|V|) was found, decrease potential of layers l,l+1,l+2,...
					for (int v = 0; v < n; v++) {
						int V = connectivityRes.vertexBlock(v), l = -(int) ssspRes.distance(V);
						if (l >= biggestLayer)
							potential[v]--;
					}
				} else {
					diagnostics.longPath();
					/*
					 * No big layer is found, use path which has at least sqrt(|V|) vertices. Connect a fake vertex to
					 * all vertices, with edge r-i to negative vertex v_i on the path and with edge r to all other
					 * vertices
					 */
					int fakeS2 = G.addVertex();
					connected.clear();
					int assignedWeight = layerNum - 2;
					for (EdgeIter it = ssspRes.getPath(vertexInMaxLayer).edgeIter(); it.hasNext();) {
						int e = it.nextInt();
						int ew = GWeights.weightInt(e);
						if (ew < 0) {
							int V = it.target();
							GWeights.set(G.addEdge(fakeS2, V), assignedWeight--);
							connected.set(V);
						}
					}
					for (int V = 0; V < N; V++)
						if (!connected.get(V))
							GWeights.set(G.addEdge(fakeS2, V), layerNum - 1);

					// Add the remaining edges to the graph, not only 0,-1 edges
					for (int e = 0; e < m; e++) {
						int weight = w[e];
						if (weight > 0) {
							int U = connectivityRes.vertexBlock(g.edgeSource(e));
							int V = connectivityRes.vertexBlock(g.edgeTarget(e));
							if (U != V)
								GWeights.set(G.addEdge(U, V), weight);
						}
					}

					// Calc distance with abs weight function to update potential function
					for (int weight, mG = G.edges().size(), e = 0; e < mG; e++)
						if ((weight = GWeights.get(e)) < 0)
							GWeights.set(e, -weight);
					ssspRes = ssspDial.computeShortestPaths(G, GWeights, fakeS2, layerNum);
					for (int v = 0; v < n; v++)
						potential[v] += ssspRes.distance(connectivityRes.vertexBlock(v));
				}
			}
		}

		return Pair.of(potential, null);
	}

	private static int calcWeightWithPotential(IndexGraph g, int e, WeightFunction.Int w, int[] potential,
			int weightMask) {
		int weight = w.weightInt(e);
		// weight = ceil(weight / 2^weightMask)
		if (weightMask != 0) {
			if (weight <= 0) {
				weight = -((-weight) >> weightMask);
			} else {
				weight += 1 << (weightMask - 1);
				weight = weight >> weightMask;
				if (weight == 0)
					weight = 1;
			}
		}
		return weight + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	private static class Result implements ShortestPathSingleSource.Result {

		private final int sourcePotential;
		private final int[] potential;
		private final ShortestPathSingleSource.Result dijkstraRes;
		private final Path cycle;

		Result(int source, int[] potential, ShortestPathSingleSource.Result dijkstraRes, Path cycle) {
			this.sourcePotential = potential != null ? potential[source] : 0;
			this.potential = potential;
			this.dijkstraRes = dijkstraRes;
			this.cycle = cycle;
		}

		static Result ofSuccess(int source, int[] potential, ShortestPathSingleSource.Result dijkstraRes) {
			return new Result(source, potential, dijkstraRes, null);
		}

		static Result ofNegCycle(int source, Path cycle) {
			return new Result(source, null, null, cycle);
		}

		@Override
		public double distance(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException("negative cycle found, no shortest path exists");
			return dijkstraRes.distance(target) - sourcePotential + potential[target];
		}

		@Override
		public Path getPath(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException("negative cycle found, no shortest path exists");
			return dijkstraRes.getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return cycle != null;
		}

		@Override
		public Path getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException("no negative cycle found");
			return cycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + cycle + "]" : dijkstraRes.toString();
		}

	}

	@Override
	public Object getDiagnostic(String key) {
		return Long.valueOf(diagnostics.get(key));
	}

	private static class Diagnostics {

		private static final boolean Enable = false;

		private long runCount;
		private long scalingIterations;
		private long potentialIterations;
		private long bigLayer;
		private long longPath;

		void runBegin() {
			if (Enable)
				runCount++;
		}

		void scalingIteration() {
			if (Enable)
				scalingIterations++;
		}

		void potentialIteration() {
			if (Enable)
				potentialIterations++;
		}

		void bigLayer() {
			if (Enable)
				bigLayer++;
		}

		void longPath() {
			if (Enable)
				longPath++;
		}

		long get(String key) {
			if ("runCount".equals(key))
				return runCount;
			if ("scalingIterations".equals(key))
				return scalingIterations;
			if ("potentialIterations".equals(key))
				return potentialIterations;
			if ("bigLayer".equals(key))
				return bigLayer;
			if ("longPath".equals(key))
				return longPath;
			throw new IllegalArgumentException("unknown diagnostic key: " + key);
		}

	}

}