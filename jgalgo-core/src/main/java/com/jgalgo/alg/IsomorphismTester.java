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

import java.util.Iterator;
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;

/**
 * Tester that check whether two graphs are isomorphic.
 *
 * <p>
 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function exists,
 * then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the direction of
 * the edges. There may be more than one isomorphism (mapping) between two graphs, and there may be none.
 *
 * <p>
 * The isomorphism problem which asks whether two graphs are isomorphic is one of few standard problems in computational
 * complexity theory belonging to NP, but not known to belong to either of its well-known subsets: P and NP-complete.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @author Barak Ugav
 */
interface IsomorphismTester {

	/**
	 * Check whether two graphs are isomorphic.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @param  g1   the first graph
	 * @param  g2   the second graph
	 * @return      {@code true} if the two graphs are isomorphic, {@code false} otherwise
	 */
	<V1, E1, V2, E2> boolean isIsomorphic(Graph<V1, E1> g1, Graph<V2, E2> g2);

	/**
	 * Get an isomorphism mapping between two graphs if one exists.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges. There may be more than one isomorphism (mapping) between two graphs, in which case one of
	 * them is returned.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @param  g1   the first graph
	 * @param  g2   the second graph
	 * @return      an isomorphism mapping between the two graphs if one exists, {@code Optional.empty()} otherwise. The
	 *              returned mapping maps vertices and edges from the first graph to vertices and edges of the second
	 *              graph. The inverse mapping can be obtained by calling {@link IsomorphismTester.Mapping#inverse()}.
	 */
	<V1, E1, V2, E2> Optional<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2);

	/**
	 * Get an iterator over all isomorphism mappings between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @param  g1   the first graph
	 * @param  g2   the second graph
	 * @return      an iterator over all isomorphism mappings between the two graphs. The returned mappings maps
	 *              vertices and edges from the first graph to vertices and edges of the second graph. The inverse
	 *              mapping can be obtained by calling {@link IsomorphismTester.Mapping#inverse()}.
	 */
	<V1, E1, V2, E2> Iterator<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2);

	/**
	 * A mapping between two graphs that preserves the structure of the graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges.
	 *
	 * <p>
	 * Although an isomorphism mapping is a bijective function, the interface only maps vertices and edges from the
	 * first graph to vertices and edges of the second graph. The inverse mapping can be obtained by calling
	 * {@link #inverse()}.
	 *
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @author      Barak Ugav
	 */
	static interface Mapping<V1, E1, V2, E2> {

		/**
		 * Map a vertex from the first graph to a vertex of the second graph.
		 *
		 * @param  vertex                the vertex to map
		 * @return                       the mapped vertex
		 * @throws NoSuchVertexException if the vertex does not exist in the first graph
		 */
		V2 mapVertex(V1 vertex);

		/**
		 * Map an edge from the first graph to an edge of the second graph.
		 *
		 * @param  edge                the edge to map
		 * @return                     the mapped edge
		 * @throws NoSuchEdgeException if the edge does not exist in the first graph
		 */
		E2 mapEdge(E1 edge);

		/**
		 * Get the inverse mapping.
		 *
		 * @return the inverse mapping
		 */
		IsomorphismTester.Mapping<V2, E2, V1, E1> inverse();
	}

	/**
	 * A mapping between two graphs that preserves the structure of the graphs for {@link IntGraph}.
	 *
	 * <p>
	 * This is a specialization of {@link IsomorphismTester.Mapping} for {@link IntGraph}. See the generic interface for
	 * more details.
	 *
	 * @author Barak Ugav
	 */
	static interface IMapping extends IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> {

		/**
		 * Map a vertex from the first graph to a vertex of the second graph.
		 *
		 * @param  vertex                the vertex to map
		 * @return                       the mapped vertex
		 * @throws NoSuchVertexException if the vertex does not exist in the first graph
		 */
		int mapVertex(int vertex);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #mapVertex(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer mapVertex(Integer vertex) {
			return Integer.valueOf(mapVertex(vertex.intValue()));
		}

		/**
		 * Map an edge from the first graph to an edge of the second graph.
		 *
		 * @param  edge                the edge to map
		 * @return                     the mapped edge
		 * @throws NoSuchEdgeException if the edge does not exist in the first graph
		 */
		int mapEdge(int edge);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #mapEdge(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer mapEdge(Integer edge) {
			return Integer.valueOf(mapEdge(edge.intValue()));
		}

		@Override
		IsomorphismTester.IMapping inverse();
	}

	/**
	 * Create a new isomorphism tester.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link IsomorphismTester} object.
	 *
	 * @return a default implementation of {@link IsomorphismTester}
	 */
	static IsomorphismTester newInstance() {
		return new IsomorphismTesterVf2();
	}

}