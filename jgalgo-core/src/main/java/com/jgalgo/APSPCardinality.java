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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import com.jgalgo.APSPResultImpl.ResFromSSSP;

/**
 * All pairs cardinality shortest path algorithm.
 * <p>
 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex to
 * some other vertex is the path with the minimum number of edges. This algorithm compute the cardinality shortest path
 * between each pair of vertices in a graph. The algorithm simple perform {@link SSSPCardinality} \(n\) times.
 * <p>
 * This algorithm runs in \(O(n(n+m))\).
 *
 * @see    SSSPCardinality
 * @author Barak Ugav
 */
class APSPCardinality implements APSP {

	private boolean parallel = Config.parallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a new APSP algorithm object.
	 */
	APSPCardinality() {}

	@Override
	public APSP.Result computeAllCardinalityShortestPaths(Graph g) {
		final int n = g.vertices().size();
		ResFromSSSP res = new ResFromSSSP(n);

		ForkJoinPool pool = Utils.getPool();
		if (n < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			SSSP sssp = new SSSPCardinality();
			for (int source = 0; source < n; source++)
				res.ssspResults[source] = sssp.computeCardinalityShortestPaths(g, source);

		} else {
			/* parallel */
			List<RecursiveAction> tasks = new ArrayList<>(n);
			ThreadLocal<SSSP> sssp = ThreadLocal.withInitial(SSSPCardinality::new);
			for (int source = 0; source < n; source++) {
				final int source0 = source;
				tasks.add(Utils.recursiveAction(
						() -> res.ssspResults[source0] = sssp.get().computeCardinalityShortestPaths(g, source0)));
			}
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (RecursiveAction task : tasks)
				task.join();
		}
		return res;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the weight function {@code w} is not {@code null} or
	 *                                      {@link EdgeWeightFunc#CardinalityEdgeWeightFunction}
	 */
	@Override
	public Result computeAllShortestPaths(Graph g, EdgeWeightFunc w) {
		if (!(w == null || w == EdgeWeightFunc.CardinalityEdgeWeightFunction))
			throw new IllegalArgumentException("only cardinality shortest paths are supported");
		return computeAllCardinalityShortestPaths(g);
	}

}