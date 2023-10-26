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

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A matching in a graph.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) has at most one
 * adjacent edge in \(M\). Its a common problem to compute the maximum (cardinality) matching, namely the matching with
 * the greatest number of edges. Another variant is to compute the maximum weighted matching with respect to some weight
 * function.
 *
 * @see    MatchingAlgo
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Matching {

	/**
	 * Check whether a vertex is matched by the matching.
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @param  vertex a vertex
	 * @return        {@code true} if {@code vertex} has an adjacent edge in the matching, else {@code false}
	 */
	boolean isVertexMatched(int vertex);

	/**
	 * Get the only matched edge adjacent to a given vertex.
	 *
	 * @param  vertex a vertex
	 * @return        the edge adjacent to {@code vertex} in the matching, or {@code -1} if {@code vertex} is not
	 *                matched
	 */
	int getMatchedEdge(int vertex);

	/**
	 * Get all the vertices matched by the matching.
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @return all the matched vertices
	 */
	IntSet matchedVertices();

	/**
	 * Get all the vertices that are not matched by the matching.
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @return all the unmatched vertices
	 */
	IntSet unmatchedVertices();

	/**
	 * Check whether an edge is part of the matching.
	 * <p>
	 * A matching \(M\) is a sub set of \(E\), the edge set of the graph. This method check whether a given edge is in
	 * \(M\).
	 *
	 * @param  edge an edge
	 * @return      {@code true} if the edge is part of the matching, else {@code false}
	 */
	boolean containsEdge(int edge);

	/**
	 * The collection of edges forming this matching.
	 *
	 * @return collection containing all the edges that are part of this matching
	 */
	IntSet edges();

	/**
	 * Check whether this matching is perfect.
	 * <p>
	 * A perfect matching is a matching in which all the vertices are matched.
	 *
	 * @return {@code true} if this matching is perfect, else {@code false}.
	 */
	boolean isPerfect();

}