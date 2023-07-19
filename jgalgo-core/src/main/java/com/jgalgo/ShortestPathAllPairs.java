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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.BuilderAbstract;

/**
 * An algorithm that compute all pairs shortest path (APSP) in a graph.
 * <p>
 * The regular {@link ShortestPathSingleSource} can be used \(n\) times to achieve the same result, but it may be more
 * efficient to use a APSP algorithm in the first place.
 *
 * @author Barak Ugav
 */
public interface ShortestPathAllPairs {

	/**
	 * Compute the shortest path between each pair of vertices in a graph.
	 * <p>
	 * Given an edge weight function, the length of a path is the weight sum of all edges of the path. The shortest path
	 * from a source vertex to some other vertex is the path with the minimum weight.
	 *
	 * @param  g a graph
	 * @param  w an edge weight function
	 * @return   a result object containing information on the shortest path between each pair of vertices
	 */
	public ShortestPathAllPairs.Result computeAllShortestPaths(Graph g, WeightFunction w);

	/**
	 * Compute the cardinality shortest path between each pair of vertices in a graph.
	 * <p>
	 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex
	 * to some other vertex is the path with the minimum number of edges.
	 *
	 * @param  g a graph
	 * @return   a result object containing information on the cardinality shortest path between each pair of vertices
	 */
	default ShortestPathAllPairs.Result computeAllCardinalityShortestPaths(Graph g) {
		return computeAllShortestPaths(g, WeightFunction.CardinalityWeightFunction);
	}

	/**
	 * A result object for an {@link ShortestPathAllPairs} algorithm.
	 *
	 * @author Barak Ugav
	 */
	interface Result {

		/**
		 * Get the distance of the shortest path between two vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the sum of weights of edges in the shortest path from the source to target,
		 *                                  or {@code Double.POSITIVE_INFINITY} if no such path exists
		 * @throws IllegalArgumentException if a negative cycle found. See {@link foundNegativeCycle}
		 */
		public double distance(int source, int target);

		/**
		 * Get the shortest path between vertices.
		 *
		 * @param  source                   the source vertex
		 * @param  target                   the target vertex
		 * @return                          the shortest path from the source to target, or {@code null} if no such path
		 *                                  exists
		 * @throws IllegalArgumentException if a negative cycle found. See {@link foundNegativeCycle}
		 */
		public Path getPath(int source, int target);

		/**
		 * Check whether a negative cycle was found.
		 * <p>
		 * If a negative cycle was found, there is no unique shortest paths, as the paths weight could be arbitrary
		 * small by going through the cycle multiple times.
		 *
		 * @return {@code true} if a negative cycle was found
		 */
		public boolean foundNegativeCycle();

		/**
		 * Get the negative cycle that was found.
		 *
		 * @return                          the negative cycle that was found.
		 * @throws IllegalArgumentException if a negative cycle was found. See {@link foundNegativeCycle}
		 */
		public Path getNegativeCycle();
	}

	/**
	 * Create a new all pairs shortest paths algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathAllPairs} object.
	 *
	 * @return a new builder that can build {@link ShortestPathAllPairs} objects
	 */
	static ShortestPathAllPairs.Builder newBuilder() {
		return new ShortestPathAllPairs.Builder() {
			private boolean cardinalityWeight;
			String impl;

			@Override
			public ShortestPathAllPairs build() {
				if (impl != null) {
					switch (impl) {
						case "cardinality":
							return new ShortestPathAllPairsCardinality();
						case "floyd-warshall":
							return new ShortestPathAllPairsFloydWarshall();
						case "johnson":
							return new ShortestPathAllPairsJohnson();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return cardinalityWeight ? new ShortestPathAllPairsCardinality() : new ShortestPathAllPairsJohnson();
			}

			@Override
			public ShortestPathAllPairs.Builder setCardinality(boolean cardinalityWeight) {
				this.cardinalityWeight = cardinalityWeight;
				return this;
			}

			@Override
			public ShortestPathAllPairs.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link ShortestPathAllPairs} objects.
	 *
	 * @see    ShortestPathAllPairs#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<ShortestPathAllPairs.Builder> {

		/**
		 * Create a new algorithm object for all pairs shortest paths computation.
		 *
		 * @return a new all pairs shortest paths algorithm
		 */
		ShortestPathAllPairs build();

		/**
		 * Enable/disable the support for cardinality shortest paths only.
		 * <p>
		 * More efficient algorithm may exists for cardinality shortest paths. Note that if this option is enabled, ONLY
		 * cardinality shortest paths will be supported.
		 *
		 * @param  cardinalityWeight if {@code true}, only cardinality shortest paths will be supported by algorithms
		 *                               built by this builder
		 * @return                   this builder
		 */
		ShortestPathAllPairs.Builder setCardinality(boolean cardinalityWeight);
	}

}
