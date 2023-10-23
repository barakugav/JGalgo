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
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Connected components algorithm.
 *
 * @author Barak Ugav
 */
public interface ConnectedComponentsAlgo {

	/**
	 * Find all (strongly) connected components in a graph.
	 * <p>
	 * A (strongly) connected component is a maximal set of vertices for which for any pair of vertices \(u, v\) in the
	 * set there exist a path from \(u\) to \(v\) and from \(v\) to \(u\).
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into (strongly) connected components
	 */
	ConnectedComponentsAlgo.Result findConnectedComponents(Graph g);

	/**
	 * Compute all weakly connected components in a directed graph.
	 * <p>
	 * Given a directed graph, if we replace all the directed edges with undirected edges and compute the (strong)
	 * connected components in the result undirected graph.
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into weakly connected components
	 */
	ConnectedComponentsAlgo.Result findWeaklyConnectedComponents(Graph g);

	/**
	 * Result object for connected components calculation.
	 * <p>
	 * The result object contains the partition of the vertices into the connected components (strongly for directed
	 * graph). Each connected component (CC) is assigned a unique integer number in range [0, ccNum), and each vertex
	 * can be queried for its CC using {@link #getVertexCc(int)}.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the connected component containing a vertex.
		 *
		 * @param  vertex a vertex in the graph
		 * @return        index of the connected component containing the vertex, in range [0, ccNum)
		 */
		int getVertexCc(int vertex);

		/**
		 * Get the number of connected components in the graph.
		 *
		 * @return the number of connected components in the graph, non negative number
		 */
		int getNumberOfCcs();

		/**
		 * Get all the vertices that are part of a connected component.
		 *
		 * @param  ccIdx                     index of a connected component
		 * @return                           the vertices that are part of the connected components
		 * @throws IndexOutOfBoundsException if {@code ccIdx} is negative or greater than the number of connected
		 *                                       components
		 */
		IntCollection getCcVertices(int ccIdx);

		/**
		 * Get all the edges that are part of a connected component.
		 * <p>
		 * An edge \((u,v)\) is part of a connected component if both \(u\) and \(v\) are part of the connected
		 * component.
		 *
		 * @param  ccIdx                     index of a connected component
		 * @return                           the edges that are part of the connected components
		 * @throws IndexOutOfBoundsException if {@code ccIdx} is negative or greater than the number of connected
		 *                                       components
		 */
		IntCollection getCcEdges(int ccIdx);

	}

	/**
	 * Create a new connected algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ConnectedComponentsAlgo} object.
	 *
	 * @return a new builder that can build {@link ConnectedComponentsAlgo} objects
	 */
	static ConnectedComponentsAlgo.Builder newBuilder() {
		return ConnectedComponentsAlgoImpl::new;
	}

	/**
	 * A builder for {@link ConnectedComponentsAlgo} objects.
	 *
	 * @see    ConnectedComponentsAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for connected components computation.
		 *
		 * @return a new connected components algorithm
		 */
		ConnectedComponentsAlgo build();

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
		default ConnectedComponentsAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
