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
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum weighted vertex cover algorithm.
 * <p>
 * Given a graph \(G=(V,E)\) a <i>vertex cover</i> is a set \(S \subseteq V\) for which for any edge \((u,v) \in E\) at
 * least one of \(u\) or \(v\) are in \(S\). Given a vertex weight function \(w:V \rightarrow R\), the weight of a
 * vertex cover is the weight sum of the vertices in the cover. The minimum vertex cover is the vertex cover with the
 * minimum weight.
 * <p>
 * Note that finding the actual minimum vertex cover is an NP-hard problem, even for a weight function that assign \(1\)
 * to each vertex. Therefore, algorithms implementing this interface provide an approximation for the actual optimal
 * solution.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Vertex_cover">Wikipedia</a>
 * @author Barak Ugav
 */
public interface VertexCover {

	/**
	 * Compute a minimum vertex cover of a graph with respect to a vertex weight function.
	 * <p>
	 * Note that finding the minimum vertex cover is an NP-hard problem, therefore the result of this function is an
	 * approximation of the optimal solution.
	 *
	 * @param  g a graph
	 * @param  w a vertex weight function
	 * @return   a minimum vertex cover
	 */
	VertexCover.Result computeMinimumVertexCover(Graph g, WeightFunction w);

	/**
	 * A result object of {@link VertexCover} computation.
	 * <p>
	 * The result object is basically the set of vertices that form the cover.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the vertices which are included in the cover.
		 *
		 * @return the vertices that are included in the cover
		 */
		IntCollection vertices();

		/**
		 * Check whether a vertex is included in the cover.
		 *
		 * @param  vertex a graph vertex identifier
		 * @return        {@code true} if {@code vertex} is included in the cover
		 */
		boolean isInCover(int vertex);

		/**
		 * Get the weight of the cover with respect to a vertex weight function.
		 *
		 * @param  w a vertex weight function
		 * @return   the weight sum of the vertices of the cover
		 */
		double weight(WeightFunction w);

	}

	/**
	 * Create a new vertex cover algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link VertexCover} object.
	 *
	 * @return a new builder that can build {@link VertexCover} objects
	 */
	static VertexCover.Builder newBuilder() {
		return VertexCoverBarYehuda::new;
	}

	/**
	 * A builder for {@link VertexCover} algorithms.
	 *
	 * @see    VertexCover#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<VertexCover.Builder> {

		/**
		 * Create a new algorithm object for minimum vertex cover computation.
		 *
		 * @return a new minimum vertex cover algorithm
		 */
		VertexCover build();

	}

}