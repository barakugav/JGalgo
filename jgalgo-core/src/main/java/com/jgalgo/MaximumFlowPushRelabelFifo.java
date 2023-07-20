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

import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push-relabel maximum flow algorithm with FIFO ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in the order the vertices with excess (more
 * in-going than out-going flow) are examined. This implementation order these vertices in a first-in-first-out (FIFO)
 * order, and achieve a running time of \(O(n^3)\) using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling' and 'gap' heuristics.
 * <p>
 * This algorithm can be implemented with better time theoretical bound using dynamic trees, but in practice it has
 * little to non advantages. See {@link MaximumFlowPushRelabelDynamicTrees}.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see    MaximumFlowPushRelabelToFront
 * @see    MaximumFlowPushRelabelHighestFirst
 * @see    MaximumFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
class MaximumFlowPushRelabelFifo extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowPushRelabelFifo() {}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		final IntPriorityQueue activeQueue = new FIFOQueueIntNoReduce();

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		void activate(int v) {
			super.activate(v);
			if (v != source && v != sink)
				activeQueue.enqueue(v);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !activeQueue.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return activeQueue.dequeueInt();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		final IntPriorityQueue activeQueue = new FIFOQueueIntNoReduce();

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		void activate(int v) {
			super.activate(v);
			if (v != source && v != sink)
				activeQueue.enqueue(v);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !activeQueue.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return activeQueue.dequeueInt();
		}
	}

}
