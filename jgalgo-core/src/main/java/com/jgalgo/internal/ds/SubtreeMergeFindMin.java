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

package com.jgalgo.internal.ds;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Subtree Merge Find Min data structure.
 * <p>
 * Subtree Merge Find min is a data structure used in maximum weighted matching in general graphs. At any moment, a tree
 * is maintain, divided into sub trees of continues nodes. AddLeaf operation is supported to add leaves to the tree.
 * Merge operation can be used to union two adjacent sub trees into one, which doesn't change the actual tree structure,
 * only the subtrees groups in it. The last two supported operations are addNonTreeEdge(u,v,weight) and
 * findMinNonTreeEdge(), which add a edge with some weight without affecting the tree structure, and the findMin
 * operation query for the non tree edge with minimum weight that connects two different subtrees.
 *
 * @param  <E> the edges element type
 * @author     Barak Ugav
 */
public interface SubtreeMergeFindMin<E> {

	/**
	 * Init the tree and create the root node.
	 *
	 * @return                       the root node
	 * @throws IllegalStateException if the tree is not empty
	 */
	Node initTree();

	/**
	 * Add a new node to the tree as leaf.
	 *
	 * @param  parent the parent node
	 * @return        the new node
	 */
	Node addLeaf(Node parent);

	/**
	 * Check if two nodes are in the same sub tree.
	 *
	 * @param  u the first node
	 * @param  v the second node
	 * @return   {@code true} if both of the nodes are in the same sub tree
	 */
	boolean isSameSubTree(Node u, Node v);

	/**
	 * Merge two adjacent sub tree.
	 * <p>
	 * If the two nodes are already in the same sub tree, this operation has no effect.
	 *
	 * @param  u                        a node from the first subtree
	 * @param  v                        a node from the second subtree
	 * @throws IllegalArgumentException if the two nodes are from different subtrees which are not adjacent
	 */
	void mergeSubTrees(Node u, Node v);

	/**
	 * Add a non tree edge to the data structure.
	 *
	 * @param u        source node
	 * @param v        target node
	 * @param edgeData data of the new edge
	 */
	void addNonTreeEdge(Node u, Node v, E edgeData);

	/**
	 * Check if the data structure contains any edge between two different sub trees.
	 *
	 * @return {@code true} if an edge exists between two different sub tress
	 */
	boolean hasNonTreeEdge();

	/**
	 * Get the edge between two different sub trees with minimum weight.
	 *
	 * @return                        minimum weight edge between two different sub trees
	 * @throws NoSuchElementException if there is no such edge
	 */
	MinEdge<E> findMinNonTreeEdge();

	/**
	 * Get the number of nodes in the tree.
	 *
	 * @return number of nodes
	 */
	int size();

	/**
	 * Clear the data structure.
	 */
	void clear();

	/**
	 * A result of {@link SubtreeMergeFindMin#findMinNonTreeEdge()} query.
	 *
	 * @param  <E> the edge element type
	 * @author     Barak Ugav
	 */
	static interface MinEdge<E> {

		/**
		 * The source node of the edge.
		 *
		 * @return the edge source node
		 */
		Node source();

		/**
		 * The target node of the edge.
		 *
		 * @return the edge target node
		 */
		Node target();

		/**
		 * Get the edge data.
		 *
		 * @return the edge data
		 */
		E edgeData();

	}

	/**
	 * A tree node in an {@link SubtreeMergeFindMin} data structure.
	 *
	 * @author Barak Ugav
	 */
	static interface Node {

		/**
		 * Get the parent node of this node.
		 *
		 * @return the parent of this node or {@code null} if this node is the root of the tree.
		 */
		Node getParent();

		/**
		 * Get the user data of this node.
		 * <p>
		 * Note that the conversion of the data stored in the implementation to the user type is unsafe.
		 *
		 * @param  <V> the data type
		 * @return     the user data of this node
		 */
		<V> V getNodeData();

		/**
		 * Set the user data of this node.
		 *
		 * @param data new value for this node
		 */
		void setNodeData(Object data);

	}

	/**
	 * Create a new subtree-merge-findMin algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SubtreeMergeFindMin} object.
	 *
	 * @return a new builder that can build {@link SubtreeMergeFindMin} objects
	 */
	static SubtreeMergeFindMin.Builder newBuilder() {
		return SubtreeMergeFindMinImpl::new;
	}

	/**
	 * A builder for {@link SubtreeMergeFindMin} objects.
	 *
	 * @see    SubtreeMergeFindMin#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {
		/**
		 * Build a new subtree-merge-findMin data structure with the given comparator.
		 *
		 * @param  <E> the edges weights type
		 * @param  cmp the comparator that will be used to order the edges weights
		 * @return     the newly constructed subtree-merge-findMin data structure
		 */
		<E> SubtreeMergeFindMin<E> build(Comparator<? super E> cmp);

		/**
		 * Build a new subtree-merge-findMin data structure with {@linkplain Comparable natural ordering}.
		 *
		 * @param  <E> the edges weights type
		 * @return     the newly constructed subtree-merge-findMin data structure
		 */
		default <E> SubtreeMergeFindMin<E> build() {
			return build(null);
		}

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
		default SubtreeMergeFindMin.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
