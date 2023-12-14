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

import java.util.Collection;
import java.util.Optional;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A builder for {@linkplain IndexGraph Index graphs}.
 *
 * <p>
 * The builder is used to construct <b>non-empty</b> index graphs. Differing from {@link IndexGraphFactory} which create
 * new empty graphs, the builder is used to add vertices and edges before actually creating the graph. This capability
 * is required to create immutable graphs, but can also be used to build mutable graph and may gain a performance boost
 * compared to creating an empty graph and adding the same vertices and edges.
 *
 * <p>
 * To create a new builder, use one of the static methods {@link #undirected()}, {@link #directed()} or
 * {@link #newInstance(boolean)}. For more options, create a new {@link IndexGraphFactory} and use
 * {@link IndexGraphFactory#newBuilder()}, or use {@link IndexGraphFactory#newBuilderCopyOf(Graph)} to create a builder
 * initialized with an existing graph vertices and edges.
 *
 * <p>
 * This interface is a specific version of {@link IntGraphBuilder} for {@link IndexGraph}.
 *
 * @see    IndexGraphBuilder#undirected()
 * @see    IndexGraphBuilder#directed()
 * @see    IntGraphBuilder
 * @see    IndexGraphFactory
 * @author Barak Ugav
 */
public interface IndexGraphBuilder extends IntGraphBuilder {

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * As the built graph is an Index graph, the vertices must be {@code 0,1,2,...,verticesNum-1} and user-chosen IDs
	 * are not supported. A new vertex will be assigned ID of value {@code vertices().size()}.
	 */
	@Override
	int addVertex();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs vertices IDs are always {@code (0,1,2, ...,verticesNum-1)} therefore the only vertex ID that can be
	 * added is {@code verticesNum}. For any other vertex passed to this method, an exception will be thrown. If
	 * {@code verticesNum} is passed, this method is equivalent to {@link #addVertex()}.
	 *
	 * @throws     IllegalArgumentException if {@code vertex} is not {@code verticesNum}
	 * @deprecated                          use {@link #addVertex()} instead
	 */
	@Deprecated
	@Override
	default void addVertex(int vertex) {
		if (vertex != vertices().size())
			throw new IllegalArgumentException("Only vertex ID " + vertices().size() + " can be added");
		addVertex();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Index graphs vertices IDs are always {@code (0,1,2, ...,verticesNum-1)} therefore the only vertices that can be
	 * added are {@code (verticesNum,verticesNum+1,verticesNum+2, ...)}. For any other vertices passed to this method,
	 * an exception will be thrown.
	 */
	@Override
	void addVertices(Collection<? extends Integer> vertices);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * As the built graph is an Index graph, the edges must be {@code 0,1,2,...,edgesNum-1}. A new edge will be assigned
	 * ID of value {@code edges().size()}.
	 *
	 * <p>
	 * It is possible to construct a graph by inserting the edges in a different order than their indices (IDs), by
	 * using {@link #addEdge(int, int, int)} in which the ID of the inserted edge is specified along with the source and
	 * target vertices. If this method is used, the set of edges will be validated when a new graph is created, and it
	 * must be equal {@code 0,1,2,...,edgesNum-1}. But, it is not allowed to mix between the two methods, namely to use
	 * {@link #addEdge(int, int)} (or {@link #addEdgesReassignIds(IEdgeSet)}) and {@link #addEdge(int, int, int)} (or
	 * {@link #addEdges(EdgeSet)}) in the same builder without {@linkplain #clear() clearing} it.
	 *
	 * @throws IllegalStateException if {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}) was used to add
	 *                                   edges to the builder without {@linkplain #clear() clearing} it.
	 */
	@Override
	int addEdge(int source, int target);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * As the built graph is an Index graph, the edges must be {@code 0,1,2,...,edgesNum-1}. Nevertheless, the edges can
	 * be added in any order to the graph as long they form a valid sequence of indices at the time of constructing the
	 * graph. But, it is not allowed to mix between the two methods, namely to use {@link #addEdge(int, int)} (or
	 * {@link #addEdgesReassignIds(IEdgeSet)}) and {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}) in
	 * the same builder without {@linkplain #clear() clearing} it.
	 *
	 * @throws IllegalStateException if {@link #addEdge(int, int)} (or {@link #addEdgesReassignIds(IEdgeSet)}) was used
	 *                                   to add edges to the builder without {@linkplain #clear() clearing} it.
	 */
	@Override
	void addEdge(int source, int target, int edge);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * As the built graph is an Index graph, the edges must be {@code 0,1,2,...,edgesNum-1}. Nevertheless, the edges can
	 * be added in any order to the graph as long they form a valid sequence of indices at the time of constructing the
	 * graph. But, it is not allowed to mix between the two methods, namely to use {@link #addEdge(int, int)} (or
	 * {@link #addEdgesReassignIds(IEdgeSet)}) and {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}) in
	 * the same builder without {@linkplain #clear() clearing} it.
	 *
	 * @throws IllegalStateException if {@link #addEdge(int, int)} (or {@link #addEdgesReassignIds(IEdgeSet)}) was used
	 *                                   to add edges to the builder without {@linkplain #clear() clearing} it.
	 */
	@Override
	void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges);

	/**
	 * Add multiple edges to the built graph and re-assign ids for them.
	 *
	 * <p>
	 * The {@link IEdgeSet} passed to this method contains the endpoints (sources and targets) of the edges, see
	 * {@link EdgeSet#iterator()}, {@link EdgeIter#source()}, {@link EdgeIter#target()}. The identifiers of the edges,
	 * which are also accessible via {@link IEdgeSet} are ignored, and new identifiers (indices) are assigned to the
	 * added edges. An {@link IEdgeSet} can be obtained from one of the methods of an {@link IntGraph}, or using
	 * {@link IEdgeSet#of(IntSet, IntGraph)}.
	 *
	 * <p>
	 * The identifiers assigned to the newly added edges are {@code (edgesNum,edgesNum+1,edgesNum+2, ...)} matching the
	 * iteration order of the provided set. This method different than {@link #addEdges(EdgeSet)} in a similar way that
	 * {@link #addEdge(int, int)} is different than {@link #addEdge(int, int, int)}. It is not allowed to mix between
	 * the two methods, namely to use {@link #addEdge(int, int)} (or {@link #addEdgesReassignIds(IEdgeSet)}) and
	 * {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}) in the same builder without {@linkplain #clear()
	 * clearing} it.
	 *
	 * <p>
	 * In the following snippet, a maximum cardinality matching is computed on a graph, and a new graph containing only
	 * the matching edges is created. It would be wrong to use {@link #addEdges(EdgeSet)} in this example, as there is
	 * no guarantee that the added edges ids are {@code (0, 1, 2, ...)}, which is required to build an
	 * {@link IndexGraph}.
	 *
	 * <pre> {@code
	 * IndexGraph g = ...;
	 * IntSet matching = (IntSet) MatchingAlgo.newInstance().computeMaximumMatching(g, null).edges();
	 *
	 * IndexGraphBuilder matchingGraphBuilder = IndexGraphBuilder.undirected();
	 * matchingGraphBuilder.addVertices(g.vertices());
	 * matchingGraphBuilder.addEdgesReassignIds(IEdgeSet.of(matching, g));
	 * IndexGraph matchingGraph = matchingGraphBuilder.build();
	 * }</pre>
	 *
	 * @param  edges                 the set of edges to add. Only the endpoints of the edges is considered, while the
	 *                                   edges identifiers are ignored.
	 * @return                       the set of newly edge identifiers added to the graph,
	 *                               {@code (edgesNum,edgesNum+1,edgesNum+2, ...)}. The edges are assigned the indices
	 *                               in the order they are iterated in the given set
	 * @throws IllegalStateException if {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}) was used to add
	 *                                   edges to the builder without {@linkplain #clear() clearing} it.
	 */
	IntSet addEdgesReassignIds(IEdgeSet edges);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If edges were added to the builder using {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}), a
	 * validation step is performed to ensure that the edges identifiers are {@code (0,1,2,...,edgesNum-1)}. If the
	 * validation fails, an exception is thrown.
	 *
	 * @throws IllegalArgumentException if the edges in the builder are not {@code (0,1,2,...,edgesNum-1)} or any of the
	 *                                      reasons described in {@link GraphBuilder#build()}
	 */
	@Override
	IndexGraph build();

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * If edges were added to the builder using {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}), a
	 * validation step is performed to ensure that the edges identifiers are {@code (0,1,2,...,edgesNum-1)}. If the
	 * validation fails, an exception is thrown.
	 *
	 * @throws IllegalArgumentException if the edges in the builder are not {@code (0,1,2,...,edgesNum-1)} or any of the
	 *                                      reasons described in {@link GraphBuilder#buildMutable()}
	 */
	@Override
	IndexGraph buildMutable();

	/**
	 * Re-Index the vertices/edges and build a new immutable graph with the new indexing.
	 *
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 *
	 * <p>
	 * Note that this method is not <i>required</i> to re-index the vertices (edges) if {@code reIndexVertices}
	 * ({@code reIndexEdges}) is {@code true}, it is simply allowed to. Whether or not a re-indexing was performed can
	 * be checked via the {@link ReIndexedGraph} return value.
	 *
	 * <p>
	 * Before the graph is built, the edges are validated. If the graph does not support self or parallel edges and such
	 * edges were added to the builder, an exception will be thrown.
	 *
	 * <p>
	 * If edges were added to the builder using {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}), a
	 * validation step is performed to ensure that the edges identifiers are {@code (0,1,2,...,edgesNum-1)}. If the
	 * validation fails, an exception is thrown.
	 *
	 * @param  reIndexVertices          if {@code true}, the implementation is allowed to (note that it is not required)
	 *                                      to re-index the vertices of the graph. If {@code false}, the original
	 *                                      vertices identifiers are used. Whether or not re-indexing was performed can
	 *                                      be checked via {@link ReIndexedGraph#verticesReIndexing()}.
	 * @param  reIndexEdges             if {@code true}, the implementation is allowed to (note that it is not required)
	 *                                      to re-index the edges of the graph. If {@code false}, the original edges
	 *                                      identifiers are used. Whether or not re-indexing was performed can be
	 *                                      checked via {@link ReIndexedGraph#edgesReIndexing()}.
	 * @return                          the re-indexed immutable graph, along with the re-indexing mapping to the
	 *                                  original indices
	 * @throws IllegalArgumentException if the edges in the builder are not {@code (0,1,2,...,edgesNum-1)} or if the
	 *                                      built graph does not support self or parallel edges and such edges were
	 *                                      added to the builder
	 */
	IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges);

	/**
	 * Re-Index the vertices/edges and build a new mutable graph with the new indexing.
	 *
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 *
	 * <p>
	 * Note that this method is not <i>required</i> to re-index the vertices (edges) if {@code reIndexVertices}
	 * ({@code reIndexEdges}) is {@code true}, it is simply allowed to. Whether or not a re-indexing was performed can
	 * be checked via the {@link ReIndexedGraph} return value.
	 *
	 * <p>
	 * Before the graph is built, the edges are validated. If the graph does not support self or parallel edges and such
	 * edges were added to the builder, an exception will be thrown.
	 *
	 * <p>
	 * If edges were added to the builder using {@link #addEdge(int, int, int)} (or {@link #addEdges(EdgeSet)}), a
	 * validation step is performed to ensure that the edges identifiers are {@code (0,1,2,...,edgesNum-1)}. If the
	 * validation fails, an exception is thrown.
	 *
	 * @param  reIndexVertices          if {@code true}, the implementation is allowed to (note that it is not required)
	 *                                      to re-index the vertices of the graph. If {@code false}, the original
	 *                                      vertices identifiers are used. Whether or not re-indexing was performed can
	 *                                      be checked via {@link ReIndexedGraph#verticesReIndexing()}.
	 * @param  reIndexEdges             if {@code true}, the implementation is allowed to (note that it is not required)
	 *                                      to re-index the edges of the graph. If {@code false}, the original edges
	 *                                      identifiers are used. Whether or not re-indexing was performed can be
	 *                                      checked via {@link ReIndexedGraph#edgesReIndexing()}.
	 * @return                          the re-indexed mutable graph, along with the re-indexing mapping to the original
	 *                                  indices
	 * @throws IllegalArgumentException if the edges in the builder are not {@code (0,1,2,...,edgesNum-1)} or if the
	 *                                      built graph does not support self or parallel edges and such edges were
	 *                                      added to the builder
	 */
	IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges);

	/**
	 * A result object of re-indexing and building a graph operation.
	 *
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 *
	 * <p>
	 * During the lifetime of a {@link IndexGraphBuilder}, vertices and edges are added to it, each one of them has a
	 * unique {@code int} identifier which is also its index (see {@link IndexGraph}). The builder can re-index the
	 * vertices/edges and build a new graph, resulting in a re-indexed graph {@link #graph()}, the vertices re-indexing
	 * {@link #verticesReIndexing()} and the edges re-indexing {@link #edgesReIndexing()}.
	 *
	 * @see    IndexGraphBuilder#reIndexAndBuild(boolean, boolean)
	 * @see    ReIndexingMap
	 * @author Barak Ugav
	 */
	static interface ReIndexedGraph {

		/**
		 * Get the newly created re-indexed graph.
		 *
		 * @return the actual re-indexed graph
		 */
		IndexGraph graph();

		/**
		 * Get the re-indexing map of the vertices.
		 *
		 * <p>
		 * The returned object (if present) can map each original vertex index to its new index after re-indexing. If
		 * the returned is not present, the vertices were no re-indexed.
		 *
		 * @return the re-indexing map of the vertices
		 */
		Optional<IndexGraphBuilder.ReIndexingMap> verticesReIndexing();

		/**
		 * Get the re-indexing map of the edges.
		 *
		 * <p>
		 * The returned object (if present) can map each original edge index to its new index after re-indexing. If the
		 * returned is not present, the edges were no re-indexed.
		 *
		 * @return the re-indexing map of the edges
		 */
		Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing();
	}

	/**
	 * A map of indices, mapping an original index to a re-indexed index.
	 *
	 * <p>
	 * <i>Re-indexing</i> is the operation of assigning new indices to the vertices/edges. By re-indexing the
	 * vertices/edges, the performance of accessing/iterating over the graph vertices/edges may increase, for example if
	 * a more cache friendly indexing exists.
	 *
	 * <p>
	 * A 're-indexed' index is the index assigned to vertex/edge after a re-indexing operation on a graph. This
	 * interface is used to represent the mapping of both vertices and edges (a single instance map either vertices or
	 * edges), and it should be understood from the context which one is it. In the documentation we use the term
	 * <i>element</i> to refer to either vertex or edge.
	 *
	 * <p>
	 * Re-indexing of the vertices (or edges) is a mapping from {@code [0,1,2,...,verticesNum-1]} to
	 * {@code [0,1,2,...,verticesNum-1]}, namely its bijection function.
	 *
	 * @see    IndexGraphBuilder#reIndexAndBuild(boolean, boolean)
	 * @see    ReIndexedGraph
	 * @author Barak Ugav
	 */
	static interface ReIndexingMap {

		/**
		 * Map an element's original index to its re-indexed index.
		 *
		 * @param  orig an element's original index
		 * @return      the element's re-index index
		 */
		int origToReIndexed(int orig);

		/**
		 * Map an element's re-indexed index to its original index.
		 *
		 * @param  reindexed an element's re-indexed index
		 * @return           the element's original index
		 */
		int reIndexedToOrig(int reindexed);

	}

	/**
	 * Create a new builder that builds undirected graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IndexGraphFactory}, namely
	 * they will not support self edges and will support parallel edges. See the factory documentation for more
	 * information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IndexGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @return a new empty builder for undirected graphs
	 */
	static IndexGraphBuilder undirected() {
		return IndexGraphFactory.undirected().newBuilder();
	}

	/**
	 * Create a new builder that builds directed graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IndexGraphFactory}, namely
	 * they will not support self edges and will support parallel edges. See the factory documentation for more
	 * information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IndexGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @return a new empty builder for directed graphs
	 */
	static IndexGraphBuilder directed() {
		return IndexGraphFactory.directed().newBuilder();
	}

	/**
	 * Create a new builder that builds un/directed graphs.
	 *
	 * <p>
	 * The graphs built by this builder will have the same default capabilities as {@link IndexGraphFactory}, namely
	 * they will not support self edges and will support parallel edges. See the factory documentation for more
	 * information.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IndexGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  directed if {@code true}, the new builder will build directed graphs, otherwise it will build undirected
	 *                      graphs
	 * @return          a new empty builder for un/directed graphs
	 */
	static IndexGraphBuilder newInstance(boolean directed) {
		return IndexGraphFactory.newInstance(directed).newBuilder();
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IndexGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  g a graph
	 * @return   a builder initialized with the given graph vertices and edges, without the original graph
	 *           vertices/edges weights.
	 */
	static IndexGraphBuilder newCopyOf(IndexGraph g) {
		return newCopyOf(g, false, false);
	}

	/**
	 * Create a new builder initialized with an existing graph vertices and edges, with/without copying the weights.
	 *
	 * <p>
	 * If the given graph is directed, the new builder will build directed graphs, and similarly for undirected graphs.
	 *
	 * <p>
	 * For more options to instantiate a builder, create a new {@link IndexGraphFactory} and use one of its
	 * {@code newBuilder} methods.
	 *
	 * @param  g                   a graph
	 * @param  copyVerticesWeights if {@code true}, the weights of the vertices will be copied from the graph to the
	 *                                 builder
	 * @param  copyEdgesWeights    if {@code true}, the weights of the edges will be copied from the graph to the
	 *                                 builder
	 * @return                     a builder initialized with the given graph vertices and edges, with/without the
	 *                             original graph vertices/edges weights.
	 */
	static IndexGraphBuilder newCopyOf(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return IndexGraphFactory.newInstance(g.isDirected()).newBuilderCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

}
