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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The Edmonds-Karp algorithm for maximum flow.
 * <p>
 * The most known implementation that solve the maximum flow problem. It does so by finding augmenting paths from the
 * source to the sink in the residual network, and saturating at least one edge in each path. This is a specification
 * Ford–Fulkerson method, which chooses the shortest augmenting path in each iteration. It runs in \(O(m^2 n)\) time and
 * linear space.
 * <p>
 * Based on the paper 'Theoretical improvements in algorithmic efficiency for network flow problems' by Jack Edmonds and
 * Richard M Karp.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MaximumFlowEdmondsKarp extends MaximumFlowAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowEdmondsKarp() {}

	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink) {
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, source, sink).computeMaxFlow();
		}
	}

	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, sources, sinks).computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, sources, sinks).computeMaxFlow();
		}
	}

	private abstract class Worker extends MaximumFlowAbstract.Worker {

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		Worker(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);
		}

		void computeMaxFlow0() {
			final int n = g.vertices().size();
			int[] backtrack = new int[n];
			BitSet visited = new BitSet(n);
			IntPriorityQueue queue = new FIFOQueueIntNoReduce();

			// perform BFS and find a path of non saturated edges from source to sink
			queue.enqueue(source);
			visited.set(source);
			bfs: for (;;) {
				if (queue.isEmpty())
					return; /* no path to sink, we are done */
				int u = queue.dequeueInt();
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (isSaturated(e))
						continue;
					int v = eit.target();
					if (visited.get(v))
						continue;
					backtrack[v] = e;
					if (v == sink) {
						/* found an augmenting path, push flow on it */
						pushAlongPath(backtrack);

						/* reset BFS */
						queue.clear();
						visited.clear();
						visited.set(source);
						queue.enqueue(source);
						continue bfs;
					}
					visited.set(v);
					queue.enqueue(v);
				}
			}
		}

		abstract void pushAlongPath(int[] backtrack);

		abstract boolean isSaturated(int e);

	}

	private class WorkerDouble extends Worker {

		final double[] flow;
		final double[] capacity;

		private static final double EPS = 0.0001;

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(flow);
		}

		@Override
		void pushAlongPath(int[] backtrack) {
			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, getResidualCapacity(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p], t = twin[e];
				flow[e] += f;
				flow[t] -= f;
				p = g.edgeSource(e);
			}
		}

		double getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}
	}

	private class WorkerInt extends Worker {

		final int[] flow;
		final int[] capacity;

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new int[g.edges().size()];
			capacity = new int[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);

			flow = new int[g.edges().size()];
			capacity = new int[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		int computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(flow);
		}

		@Override
		void pushAlongPath(int[] backtrack) {
			// find out what is the maximum flow we can pass
			int f = Integer.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, getResidualCapacity(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p], t = twin[e];
				flow[e] += f;
				flow[t] -= f;
				p = g.edgeSource(e);
			}
		}

		int getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= 0;
		}
	}

}
