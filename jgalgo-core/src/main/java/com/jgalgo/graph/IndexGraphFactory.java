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

/**
 * A factory for {@linkplain IndexGraph Index graphs}.
 *
 * <p>
 * The factory can be used to create new empty graphs, with different options and capabilities. Few methods are
 * available to optimize the graph implementation choice. The factory can also be used to create a copy of an existing
 * graphs, with the same vertices and edges, with/without copying the vertices/edges weights.
 *
 * <p>
 * Both the graph factory and {@link IndexGraphBuilder} are used to create new graphs. The difference is that vertices
 * and edges can be added to the builder, which is then used to construct non empty graphs, while the factory is only
 * used to choose a graph implementation and create an empty graph.
 *
 * <p>
 * This interface is a specific version of {@link IntGraphFactory} for {@link IndexGraph}.
 *
 * @see    IndexGraphFactory#newDirected()
 * @see    IndexGraphFactory#newUndirected()
 * @see    IndexGraph
 * @see    IndexGraphBuilder
 * @author Barak Ugav
 */
public interface IndexGraphFactory extends IntGraphFactory {

	@Override
	IndexGraph newGraph();

	@Override
	default IndexGraph newCopyOf(Graph<Integer, Integer> g) {
		return (IndexGraph) IntGraphFactory.super.newCopyOf(g);
	}

	@Override
	IndexGraph newCopyOf(Graph<Integer, Integer> g, boolean copyWeights);

	@Override
	IndexGraphBuilder newBuilder();

	@Override
	IndexGraphFactory setDirected(boolean directed);

	@Override
	IndexGraphFactory allowSelfEdges(boolean selfEdges);

	@Override
	IndexGraphFactory allowParallelEdges(boolean parallelEdges);

	@Override
	IndexGraphFactory expectedVerticesNum(int expectedVerticesNum);

	@Override
	IndexGraphFactory expectedEdgesNum(int expectedEdgesNum);

	@Override
	IndexGraphFactory addHint(GraphFactory.Hint hint);

	@Override
	IndexGraphFactory removeHint(GraphFactory.Hint hint);

	/**
	 * Create an undirected index graph factory.
	 *
	 * @return a new factory that can build undirected index graphs
	 */
	public static IndexGraphFactory newUndirected() {
		return new IndexGraphFactoryImpl(false);
	}

	/**
	 * Create a directed index graph factory.
	 *
	 * @return a new factory that can build directed index graphs
	 */
	public static IndexGraphFactory newDirected() {
		return new IndexGraphFactoryImpl(true);
	}

	/**
	 * Create a new index graph factory based on a given implementation.
	 *
	 * <p>
	 * The new factory will build graphs with the same capabilities as the given graph, possibly choosing to use a
	 * similar implementation. The factory will NOT copy the graph itself (the vertices, edges and weights), for such
	 * use case see {@link IndexGraph#copy()} or {@link IndexGraphFactory#newCopyOf(Graph)}.
	 *
	 * @param  g a graph from which the factory should copy its capabilities
	 * @return   a new graph factory that will create graphs with the same capabilities of the given graph
	 */
	public static IndexGraphFactory newFrom(IndexGraph g) {
		return new IndexGraphFactoryImpl(g);
	}

	@Override
	default IndexGraphFactory setOption(String key, Object value) {
		return (IndexGraphFactory) IntGraphFactory.super.setOption(key, value);
	}
}
