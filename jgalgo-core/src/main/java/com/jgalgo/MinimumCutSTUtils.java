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

import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MinimumCutSTUtils {

	private MinimumCutSTUtils() {}

	static interface AbstractImpl extends MinimumCutST {

		@Override
		default Cut computeMinimumCut(Graph g, WeightFunction w, int source, int sink) {
			if (g instanceof IndexGraph)
				return computeMinimumCut((IndexGraph) g, w, source, sink);

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();

			w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			Cut indexCut = computeMinimumCut(iGraph, w, iSource, iSink);
			return new CutImpl.CutFromIndexCut(indexCut, viMap, eiMap);
		}

		abstract Cut computeMinimumCut(IndexGraph g, WeightFunction w, int source, int sink);

	}

	static MinimumCutST buildFromMaxFlow(MaximumFlow maxFlowAlg) {
		return new AbstractImpl() {

			@Override
			public Cut computeMinimumCut(IndexGraph g, WeightFunction w, int source, int sink) {
				final int n = g.vertices().size();
				BitSet visited = new BitSet(n);
				IntPriorityQueue queue = new IntArrayFIFOQueue();

				/* create a flow network with weights as capacities */
				FlowNetwork net = createFlowNetworkFromEdgeWeightFunc(g, w);

				/* compute max flow */
				maxFlowAlg.computeMaximumFlow(g, net, source, sink);

				/* perform a BFS from source and use only non saturated edges */
				IntList cut = new IntArrayList();
				final double eps = 0.00001;
				cut.add(source);
				visited.set(source);
				queue.enqueue(source);

				if (g.getCapabilities().directed()) {
					while (!queue.isEmpty()) {
						int u = queue.dequeueInt();

						for (EdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.target();
							if (visited.get(v))
								continue;
							if (Math.abs(net.getCapacity(e) - net.getFlow(e)) < eps)
								continue; // saturated edge
							cut.add(v);
							visited.set(v);
							queue.enqueue(v);
						}
						/*
						 * We don't have any guarantee that the graph has a twin edge for each edge, so we iterate over
						 * the in-edges and search for edges with non zero flow which imply an existent of an out edge
						 * in the residual network
						 */
						for (EdgeIter it = g.inEdges(u).iterator(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.source();
							if (visited.get(v))
								continue;
							if (net.getFlow(e) < eps)
								continue; // saturated edge
							cut.add(v);
							visited.set(v);
							queue.enqueue(v);
						}
					}
				} else {
					while (!queue.isEmpty()) {
						int u = queue.dequeueInt();

						for (EdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.target();
							if (visited.get(v))
								continue;
							double directedFlow = net.getFlow(e) * (g.edgeSource(e) == u ? +1 : -1);
							if (Math.abs(net.getCapacity(e) - directedFlow) < eps)
								continue; // saturated edge
							cut.add(v);
							visited.set(v);
							queue.enqueue(v);
						}
					}
				}

				return new CutImpl(g, cut);
			}

		};
	}

	static FlowNetwork createFlowNetworkFromEdgeWeightFunc(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyPositiveEdgesWeights(g, w);
		double[] flow = new double[g.edges().size()];
		FlowNetwork net = new FlowNetwork() {
			@Override
			public double getCapacity(int edge) {
				return w.weight(edge);
			}

			@Override
			public void setCapacity(int edge, double cap) {
				throw new UnsupportedOperationException();
			}

			@Override
			public double getFlow(int edge) {
				return flow[edge];
			}

			@Override
			public void setFlow(int edge, double f) {
				flow[edge] = f;
			}
		};
		return net;
	}

	static MinimumCutGlobal globalMinCutFromStMinCut(MinimumCutST stMinCut) {
		return new MinimumCutGlobalAbstract() {
			@Override
			Cut computeMinimumCut(IndexGraph g, WeightFunction w) {
				final int n = g.vertices().size();
				if (n < 2)
					throw new IllegalArgumentException("no valid cut in graphs with less than two vertices");
				w = WeightFunctions.localEdgeWeightFunction(g, w);

				Cut bestCut = null;
				double bestCutWeight = Double.MAX_VALUE;
				final int source = 0;
				for (int sink = 1; sink < n; sink++) {
					Cut cut = stMinCut.computeMinimumCut(g, w, source, sink);
					double cutWeight = cut.weight(w);
					if (bestCutWeight > cutWeight) {
						bestCutWeight = cutWeight;
						bestCut = cut;
					}
				}
				assert bestCut != null;
				return bestCut;
			}
		};
	}

}
