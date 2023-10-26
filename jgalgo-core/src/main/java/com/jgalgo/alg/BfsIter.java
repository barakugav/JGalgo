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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Bread first search (BFS) iterator.
 * <p>
 * The BFS iterator is used to iterate over the vertices of a graph in a bread first manner, namely by the cardinality
 * distance of the vertices from some source(s) vertex. The iterator will visit every vertex \(v\) for which there is a
 * path from the source(s) to \(v\). Each such vertex will be visited exactly once.
 * <p>
 * The graph should not be modified during the BFS iteration.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * for (BfsIter iter = BfsIter.newInstance(g, sourceVertex); iter.hasNext();) {
 *     int v = iter.nextInt();
 *     int e = iter.inEdge();
 *     int layer = iter.layer();
 *     System.out.println("Reached vertex " + v + " at layer " + layer + " using edge " + e);
 * }
 * }</pre>
 *
 * @see    DfsIter
 * @see    <a href= "https://en.wikipedia.org/wiki/Breadth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public interface BfsIter extends IntIterator {

	/**
	 * Create a BFS iterator rooted at a single source vertex.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph
	 */
	public static BfsIter newInstance(Graph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Forward((IndexGraph) g, source);
		IndexIdMap viMap = g.indexGraphVerticesMap(), eiMap = g.indexGraphEdgesMap();
		BfsIter indexBFS = new BfsIterImpl.Forward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.BFSFromIndexBFS(indexBFS, viMap, eiMap);
	}

	/**
	 * Create a backward BFS iterator rooted at a single source vertex.
	 * <p>
	 * The regular BFS uses the out-edges of each vertex to explore its neighbors, while the <i>backward</i> BFS uses
	 * the in-edges to do so.
	 *
	 * @param  g      a graph
	 * @param  source a vertex in the graph from which the search will start from
	 * @return        a BFS iterator that iterate over the vertices of the graph using the in-edges
	 */
	public static BfsIter newInstanceBackward(Graph g, int source) {
		if (g instanceof IndexGraph)
			return new BfsIterImpl.Backward((IndexGraph) g, source);
		IndexIdMap viMap = g.indexGraphVerticesMap(), eiMap = g.indexGraphEdgesMap();
		BfsIter indexBFS = new BfsIterImpl.Backward(g.indexGraph(), viMap.idToIndex(source));
		return new BfsIterImpl.BFSFromIndexBFS(indexBFS, viMap, eiMap);
	}

	/**
	 * Check whether there is more vertices to iterate over.
	 */
	@Override
	public boolean hasNext();

	/**
	 * Advance the iterator and return a vertex that was not visited by the iterator yet.
	 */
	@Override
	public int nextInt();

	/**
	 * Get the edge that led to the last vertex returned by {@link nextInt}.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the edge that led to the last vertex returned by {@link nextInt}
	 */
	public int lastEdge();

	/**
	 * Get the layer of the last vertex returned by {@link nextInt}.
	 * <p>
	 * The layer of a vertex is the cardinality distance, the number of edges in the path, from the source(s) to the
	 * vertex.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the layer of the last vertex returned by {@link nextInt}.
	 */
	public int layer();
}