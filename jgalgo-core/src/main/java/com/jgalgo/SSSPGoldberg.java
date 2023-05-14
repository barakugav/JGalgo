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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Goldberg's algorithm for SSSP for integer (positive and negative) weights on directed graphs.
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
public class SSSPGoldberg implements SSSP {

	private SSSP positiveSsspAlgo = new SSSPDijkstra();
	private final ConnectivityAlgorithm ccAlg = ConnectivityAlgorithm.newBuilder().build();
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Construct a new SSSP algorithm object.
	 */
	public SSSPGoldberg() {}

	/**
	 * Set the algorithm used for positive weights graphs.
	 * <p>
	 * The algorithm first calculate a potential for each vertex and construct an equivalent positive weight function
	 * which is used by an SSSP algorithm for positive weights to compute the final shortest paths.
	 *
	 * @param algo a SSSP implementation for graphs with positive weight function
	 */
	public void setPositiveSsspAlgo(SSSP algo) {
		positiveSsspAlgo = Objects.requireNonNull(algo);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed or the edge weights function is not of type
	 *                                      {@link EdgeWeightFunc.Int}
	 */
	@Override
	public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		ArgumentCheck.onlyDirected(g);
		if (!(w instanceof EdgeWeightFunc.Int))
			throw new IllegalArgumentException("Only integer weights are supported");
		return computeShortestPaths0(g, (EdgeWeightFunc.Int) w, source);
	}

	private SSSP.Result computeShortestPaths0(Graph g, EdgeWeightFunc.Int w, int source) {
		int minWeight = Integer.MAX_VALUE;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			minWeight = Math.min(minWeight, w.weightInt(e));
		}
		if (minWeight >= 0)
			// All weights are positive, use Dijkstra
			return positiveSsspAlgo.computeShortestPaths(g, w, source);

		Pair<int[], Path> p = calcPotential(g, w, minWeight);
		if (p.second() != null)
			return Result.ofNegCycle(source, p.second());
		int[] potential = p.first();
		PotentialWeightFunction pw = new PotentialWeightFunction(g, w, potential);
		SSSP.Result res = positiveSsspAlgo.computeShortestPaths(g, pw, source);
		return Result.ofSuccess(source, potential, res);
	}

	private Pair<int[], Path> calcPotential(Graph g, EdgeWeightFunc.Int w, int minWeight) {
		int n = g.vertices().size();
		int[] potential = new int[n];

		BitSet connected = new BitSet(n);
		int[] layerSize = new int[n + 1];

		SSSPDial ssspDial = new SSSPDial();
		SSSP dagSssp = new SSSPDag();

		Graph gNeg = GraphBuilder.newDirected().setVerticesNum(n).build();
		Weights.Int gNegEdgeRefs = gNeg.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		Graph G = GraphBuilder.newDirected().setVerticesNum(n).build();
		Weights.Int GWeights = G.addEdgesWeights("weights", int.class, Integer.valueOf(-1));
		int fakeS1 = G.addVertex(), fakeS2 = G.addVertex();

		int minWeightWordsize = Utils.log2(-minWeight);
		for (int weightMask = minWeightWordsize; weightMask >= 0; weightMask--) {
			if (weightMask != minWeightWordsize)
				for (int v = 0; v < n; v++)
					potential[v] *= 2;

			// updated potential function until there are no more negative vertices with
			// current weight function
			for (;;) {
				// Create a graph with edges with weight <= 0
				gNeg.clearEdges();
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (weight(g, e, w, potential, weightMask) <= 0)
						gNegEdgeRefs.set(gNeg.addEdge(u, v), e);
				}

				// Find all strong connectivity components in the graph
				ConnectivityAlgorithm.Result connectivityRes = ccAlg.computeConnectivityComponents(gNeg);
				int N = connectivityRes.getNumberOfCC();

				// Contract each strong connectivity component and search for a negative edge
				// within it, if found - negative cycle found
				G.clearEdges();
				for (int u = 0; u < n; u++) {
					int U = connectivityRes.getVertexCc(u);
					for (EdgeIter eit = gNeg.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						int V = connectivityRes.getVertexCc(v);
						int weight = weight(g, gNegEdgeRefs.getInt(e), w, potential, weightMask);
						if (U != V) {
							GWeights.set(G.addEdge(U, V), weight);

						} else if (weight < 0) {
							// negative cycle
							Path negCycle0 = Path.findPath(gNeg, v, u);
							IntList negCycle = new IntArrayList(negCycle0.size() + 1);
							for (IntIterator it = negCycle0.iterator(); it.hasNext();)
								negCycle.add(gNegEdgeRefs.getInt(it.nextInt()));
							negCycle.add(gNegEdgeRefs.getInt(e));
							return Pair.of(null, new Path(g, v, v, negCycle));
						}
					}
				}

				// Create a fake vertex S, connect with 0 edges to all and calc distances
				for (int U = 0; U < N; U++)
					GWeights.set(G.addEdge(fakeS1, U), 0);
				SSSP.Result ssspRes = dagSssp.computeShortestPaths(G, GWeights, fakeS1);

				// Divide super vertices into layers by distance
				int layerNum = 0;
				int vertexInMaxLayer = -1;
				Arrays.fill(layerSize, 0);
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
				if (layerSize[biggestLayer] >= Math.sqrt(N)) {
					// A layer with sqrt(|V|) was found, decrease potential of layers l,l+1,l+2,...
					for (int v = 0; v < n; v++) {
						int V = connectivityRes.getVertexCc(v), l = -(int) ssspRes.distance(V);
						if (l >= biggestLayer)
							potential[v]--;
					}
				} else {
					// No big layer is found, use path which has at least sqrt(|V|) vetices.
					// Connected a fake vertex to all vertices, with edge r-i to negative vertex vi
					// on the path and with edge r to all other vertices
					connected.clear();
					int assignedWeight = layerNum - 2;
					for (IntIterator it = ssspRes.getPath(vertexInMaxLayer).iterator(); it.hasNext();) {
						int e = it.nextInt();
						int ew = GWeights.getInt(e);
						if (ew < 0) {
							int V = G.edgeTarget(e);
							GWeights.set(G.addEdge(fakeS2, V), assignedWeight--);
							connected.set(V);
						}
					}
					for (int V = 0; V < N; V++)
						if (!connected.get(V))
							GWeights.set(G.addEdge(fakeS2, V), layerNum - 1);

					// Add the remaining edges to the graph, not only 0,-1 edges
					for (IntIterator it = g.edges().iterator(); it.hasNext();) {
						int e = it.nextInt();
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						int weight = weight(g, e, w, potential, weightMask);
						if (weight > 0)
							GWeights.set(G.addEdge(connectivityRes.getVertexCc(u), connectivityRes.getVertexCc(v)),
									weight);
					}

					// Calc distance with abs weight function to update potential function
					ssspRes = ssspDial.computeShortestPaths(G, e -> Math.abs(GWeights.getInt(e)), fakeS2, layerNum);
					for (int v = 0; v < n; v++)
						potential[v] += ssspRes.distance(connectivityRes.getVertexCc(v));
				}
			}
		}

		return Pair.of(potential, null);
	}

	private static int weight(Graph g, int e, EdgeWeightFunc.Int w, int[] potential, int weightMask) {
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

	private static class Result implements SSSP.Result {

		private final int sourcePotential;
		private final int[] potential;
		private final SSSP.Result dijkstraRes;
		private final Path cycle;

		Result(int source, int[] potential, SSSP.Result dijkstraRes, Path cycle) {
			this.sourcePotential = potential != null ? potential[source] : 0;
			this.potential = potential;
			this.dijkstraRes = dijkstraRes;
			this.cycle = cycle;
		}

		static Result ofSuccess(int source, int[] potential, SSSP.Result dijkstraRes) {
			return new Result(source, potential, dijkstraRes, null);
		}

		static Result ofNegCycle(int source, Path cycle) {
			return new Result(source, null, null, cycle);
		}

		@Override
		public double distance(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.distance(target) - sourcePotential + potential[target];
		}

		@Override
		public Path getPath(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return dijkstraRes.getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return cycle != null;
		}

		@Override
		public Path getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException();
			return cycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + cycle + "]" : dijkstraRes.toString();
		}

	}

	private static class PotentialWeightFunction implements EdgeWeightFunc.Int {

		private final Graph g;
		private final EdgeWeightFunc.Int w;
		private final int[] potential;

		PotentialWeightFunction(Graph g, EdgeWeightFunc.Int w, int[] potential) {
			this.g = g;
			this.w = w;
			this.potential = potential;
		}

		@Override
		public int weightInt(int e) {
			return w.weightInt(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
		}

	}

}
