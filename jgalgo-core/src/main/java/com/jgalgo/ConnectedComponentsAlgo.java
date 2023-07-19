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
import com.jgalgo.internal.util.BuilderAbstract;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Connectivity components algorithm.
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
	ConnectedComponentsAlgo.Result computeConnectivityComponents(Graph g);

	/**
	 * Result object for connectivity components calculation.
	 * <p>
	 * The result object contains the partition of the vertices into the connectivity components (strongly for directed
	 * graph). Each connectivity component (CC) is assigned a unique integer number in range [0, ccNum), and each vertex
	 * can be queried for its CC using {@link #getVertexCc(int)}.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the connectivity component containing a vertex.
		 *
		 * @param  vertex a vertex in the graph
		 * @return        index of the connectivity component containing the vertex, in range [0, ccNum)
		 */
		int getVertexCc(int vertex);

		/**
		 * Get the number of connectivity components in the graph.
		 *
		 * @return the number of connectivity components in the graph, non negative number
		 */
		int getNumberOfCcs();

		/**
		 * Get all the vertices that are part of a connectivity component.
		 *
		 * @param  ccIdx                     index of a connectivity component
		 * @return                           the vertices that are part of the connectivity components
		 * @throws IndexOutOfBoundsException if {@code ccIdx} is negative or greater than the number of connectivity
		 *                                       components
		 */
		IntCollection getCcVertices(int ccIdx);

		/**
		 * Get all the edges that are part of a connectivity component.
		 * <p>
		 * An edge \((u,v)\) is part of a connectivity component if both \(u\) and \(v\) are part of the connectivity
		 * component.
		 *
		 * @param  ccIdx                     index of a connectivity component
		 * @return                           the edges that are part of the connectivity components
		 * @throws IndexOutOfBoundsException if {@code ccIdx} is negative or greater than the number of connectivity
		 *                                       components
		 */
		IntCollection getCcEdges(int ccIdx);

	}

	/**
	 * Create a new connectivity algorithm builder.
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
	static interface Builder extends BuilderAbstract<ConnectedComponentsAlgo.Builder> {

		/**
		 * Create a new algorithm object for connectivity components computation.
		 *
		 * @return a new connectivity components algorithm
		 */
		ConnectedComponentsAlgo build();
	}

}
