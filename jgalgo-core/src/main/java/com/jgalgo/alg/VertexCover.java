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

import java.util.BitSet;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

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
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
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
		IntSet vertices();

		/**
		 * Check whether a vertex is included in the cover.
		 *
		 * @param  vertex a graph vertex identifier
		 * @return        {@code true} if {@code vertex} is included in the cover
		 */
		boolean isInCover(int vertex);

	}

	/**
	 * Check whether a set of vertices is a vertex cover of a graph.
	 * <p>
	 * A set of vertices is a vertex cover of a graph if for every edge in the graph at least one of its vertices is in
	 * the set. In addition, the collection of the vertices must not contain duplicates.
	 *
	 * @param  g        a graph
	 * @param  vertices a collection of vertices that should cover all the edges in the graph
	 * @return          {@code true} if {@code vertices} is a vertex cover of {@code g}
	 */
	static boolean isCover(Graph g, IntCollection vertices) {
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			vertices = IndexIdMaps.idToIndexCollection(vertices, g.indexGraphVerticesMap());
		}
		final int n = ig.vertices().size();
		final int m = ig.edges().size();
		BitSet visited = new BitSet(n);
		for (int v : vertices) {
			if (!ig.vertices().contains(v))
				throw new IllegalArgumentException("invalid vertex index " + v);
			if (visited.get(v))
				throw new IllegalArgumentException(
						"vertex with index " + v + " is included more than once in the cover");
			visited.set(v);
		}
		for (int e = 0; e < m; e++)
			if (!visited.get(ig.edgeSource(e)) && !visited.get(ig.edgeTarget(e)))
				return false;
		return true;
	}

	/**
	 * Create a new vertex cover algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link VertexCover} object. The {@link VertexCover.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link VertexCover}
	 */
	static VertexCover newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new vertex cover algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
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
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum vertex cover computation.
		 *
		 * @return a new minimum vertex cover algorithm
		 */
		VertexCover build();

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
		default VertexCover.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
