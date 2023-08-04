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

package com.jgalgo.graph;

import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Iterator used to iterate over edges of a vertex.
 * <p>
 * Each {@code int} returned by {@link #nextInt()} is an ID of an edge iterated by the iterator. The source and target
 * of the last iterated edge are available by {@link #source()} and {@link #target()}.
 *
 * <pre> {@code
 * Graph g = ...;
 * int vertex = ...;
 * for (EdgeIter eit = g.outEdges(vertex).iterator(); eit.hasNext();) {
 * 	int e = eit.nextInt();
 * 	int u = eit.source();
 * 	int v = eit.target();
 * 	assert vertex == u;
 * 	System.out.println("Out edge of " + vertex + ": " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @see    EdgeSet
 * @author Barak Ugav
 */
public interface EdgeIter extends IntIterator {

	/**
	 * Peek at the next edge of the iterator without advancing it.
	 * <p>
	 * Similar to {@link #nextInt()} but without advancing the iterator.
	 *
	 * @return                        the next edge of the iterator
	 * @throws NoSuchElementException if there is no 'next' element
	 */
	int peekNext();

	/**
	 * Get the source vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the source vertex of the last returned edge
	 */
	int source();

	/**
	 * Get the target vertex of the last returned edge.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the target vertex of the last returned edge
	 */
	int target();

	/**
	 * Get an empty edge iterator.
	 *
	 * @return an empty edge iterator
	 */
	static EdgeIter emptyIterator() {
		return Edges.EmptyEdgeIter.Instance;
	}

}
