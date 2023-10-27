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

/**
 * Static Lowest Common Ancestor (LCA) algorithm.
 * <p>
 * The lowest common ancestor of two vertices in a tree is the vertex that appear in both vertices paths to the root
 * (common ancestor), and its farthest from the root (lowest). Given a tree \(G=(V,E)\), we would like to pre process it
 * and then answer queries of the type "what is the lower common ancestor of two vertices \(u\) and \(v\)?".
 * <p>
 * Most implementation of this interface achieve linear or near linear preprocessing time and constant or logarithmic
 * query time.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Graph tree = Graph.newUndirected();
 * int rt = tree.addVertex();
 * int v1 = tree.addVertex();
 * int v2 = tree.addVertex();
 * int v3 = tree.addVertex();
 * int v4 = tree.addVertex();
 * int v5 = tree.addVertex();
 *
 * tree.addEdge(rt, v1);
 * tree.addEdge(v1, v2);
 * tree.addEdge(v1, v3);
 * tree.addEdge(rt, v4);
 * tree.addEdge(v4, v5);
 *
 * LowestCommonAncestorStatic lca = LowestCommonAncestorStatic.newInstance();
 * LowestCommonAncestorStatic.DataStructure lcaDS = lca.preProcessTree(tree, rt);
 * assert lcaDS.findLowestCommonAncestor(v1, v4) == rt;
 * assert lcaDS.findLowestCommonAncestor(v2, v3) == v1;
 * assert lcaDS.findLowestCommonAncestor(v4, v5) == v4;
 * assert lcaDS.findLowestCommonAncestor(v2, v5) == rt;
 * }</pre>
 *
 * @see    LowestCommonAncestorDynamic
 * @see    LowestCommonAncestorOffline
 * @author Barak Ugav
 */
public interface LowestCommonAncestorStatic {

	/**
	 * Perform a static pre processing of a tree for future LCA (Lowest common ancestor) queries.
	 *
	 * @param  tree a tree
	 * @param  root root of the tree
	 * @return      a data structure built from the preprocessing, that can answer LCA queries efficiently
	 */
	public LowestCommonAncestorStatic.DataStructure preProcessTree(Graph tree, int root);

	/**
	 * Data structure result created from a static LCA pre-processing.
	 *
	 * @author Barak Ugav
	 */
	interface DataStructure {

		/**
		 * Find the lowest common ancestor of two vertices in the tree.
		 *
		 * @param  u the first vertex
		 * @param  v the second vertex
		 * @return   the lowest common ancestor of \(u\) and \(v\)
		 */
		public int findLowestCommonAncestor(int u, int v);
	}

	/**
	 * Create a new algorithm for static LCA queries.
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorStatic} object. The
	 * {@link LowestCommonAncestorStatic.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorStatic}
	 */
	static LowestCommonAncestorStatic newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new static LCA algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link LowestCommonAncestorStatic} objects
	 */
	static LowestCommonAncestorStatic.Builder newBuilder() {
		return LowestCommonAncestorStaticRMQ::new;
	}

	/**
	 * A builder for {@link LowestCommonAncestorStatic} objects.
	 *
	 * @see    LowestCommonAncestorStatic#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new static LCA algorithm.
		 *
		 * @return a new static LCA algorithm
		 */
		LowestCommonAncestorStatic build();

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
		default LowestCommonAncestorStatic.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
