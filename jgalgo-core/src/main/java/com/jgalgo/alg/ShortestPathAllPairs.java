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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * An algorithm that compute all pairs shortest path (APSP) in a graph.
 * <p>
 * The regular {@link ShortestPathSingleSource} can be used \(n\) times to achieve the same result, but it may be more
 * efficient to use a APSP algorithm in the first place.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
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
	 * Compute the shortest path between each pair of vertices in a given subset of the vertices of the graph.
	 *
	 * @param  g              a graph
	 * @param  verticesSubset a subset of vertices of the graph. All shortest paths will be computed between each pair
	 *                            of vertices from the subset
	 * @param  w              as edge weight function
	 * @return                a result object containing information on the shortest path between each pair of vertices
	 *                        in the subset
	 */
	public ShortestPathAllPairs.Result computeSubsetShortestPaths(Graph g, IntCollection verticesSubset,
			WeightFunction w);

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
	 * Compute the cardinality shortest path between each pair of vertices in a given subset of the vertices of the
	 * graph.
	 * <p>
	 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex
	 * to some other vertex is the path with the minimum number of edges.
	 *
	 * @param  g              a graph
	 * @param  verticesSubset a subset of vertices of the graph. All shortest paths will be computed between each pair
	 *                            of vertices from the subset
	 * @return                a result object containing information on the cardinality shortest path between each pair
	 *                        of vertices in the subset
	 */
	default ShortestPathAllPairs.Result computeSubsetCardinalityShortestPaths(Graph g, IntCollection verticesSubset) {
		return computeSubsetShortestPaths(g, verticesSubset, WeightFunction.CardinalityWeightFunction);
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
	 * Create a new all-pairs-shortest-paths algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ShortestPathAllPairs} object. The
	 * {@link ShortestPathAllPairs.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ShortestPathAllPairs}
	 */
	static ShortestPathAllPairs newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new all pairs shortest paths algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
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
				ShortestPathAllPairs cardinalityAlgo = new ShortestPathAllPairsCardinality();
				if (cardinalityWeight)
					return cardinalityAlgo;
				return new ShortestPathAllPairs() {
					final ShortestPathAllPairs weightedAlgo = new ShortestPathAllPairsJohnson();

					@Override
					public ShortestPathAllPairs.Result computeAllShortestPaths(Graph g, WeightFunction w) {
						if (w == null || w == WeightFunction.CardinalityWeightFunction) {
							return cardinalityAlgo.computeAllCardinalityShortestPaths(g);
						} else {
							return weightedAlgo.computeAllShortestPaths(g, w);
						}
					}

					@Override
					public Result computeSubsetShortestPaths(Graph g, IntCollection verticesSubset, WeightFunction w) {
						if (w == null || w == WeightFunction.CardinalityWeightFunction) {
							return cardinalityAlgo.computeSubsetCardinalityShortestPaths(g, verticesSubset);
						} else {
							return weightedAlgo.computeSubsetShortestPaths(g, verticesSubset, w);
						}
					}
				};
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
	static interface Builder {

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

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default ShortestPathAllPairs.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}