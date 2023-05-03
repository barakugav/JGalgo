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

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum spanning tree algorithm for directed graphs.
 * <p>
 * A spanning tree in directed graph is defined similarly to a spanning tree in undirected graph, but the 'spanning
 * tree' does not yield a strongly connected graph, but a weakly connected tree rooted at some vertex.
 *
 * @author Barak Ugav
 */
public interface MDST extends MST {

	/**
	 * {@inheritDoc}
	 * <p>
	 * The result tree will be rooted at some vertex chosen by the algorithm.
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w);

	/**
	 * Compute a minimum directed spanning tree (MDST) in a directed graph, rooted at the given vertex
	 *
	 * @param  g    a directed graph
	 * @param  w    an edge weight function
	 * @param  root vertex in the graph the spanning tree will be rooted from
	 * @return      all edges composing the spanning tree
	 */
	public IntCollection computeMinimumSpanningTree(DiGraph g, EdgeWeightFunc w, int root);

	/**
	 * Create a new minimum directed spanning tree algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MDST} object.
	 *
	 * @return a new builder that can build {@link MDST} objects
	 */
	static MDST.Builder newBuilder() {
		return MDSTTarjan::new;
	}

	/**
	 * A builder for {@link MDST} objects.
	 *
	 * @see    MDST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum directed spanning tree computation.
		 *
		 * @return a new minimum directed spanning tree algorithm
		 */
		MDST build();
	}

}
