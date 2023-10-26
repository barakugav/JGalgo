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
 * A partition of the vertices of a graph into two blocks.
 * <p>
 * This interface is a specific case of {@link VertexPartition} where the number of blocks is 2. It can be used to
 * represent a <a href= "https://en.wikipedia.org/wiki/Cut_(graph_theory)">cut</a>, or a
 * <a href= "https://en.wikipedia.org/wiki/Bipartite_graph">bipartite</a> partition of a graph.
 * <p>
 * The two blocks (or sets) are called left and right. The left block is the block with index 0, and the right block is
 * the block with index 1, and few methods with 'left/right' names are provided for convenience.
 *
 * @author Barak Ugav
 */
public interface VertexBiPartition extends VertexPartition {

	@Override
	default int numberOfBlocks() {
		return 2;
	}

	/**
	 * Get the vertices in the 'left' block.
	 * <p>
	 * The left block is the block with index 0.
	 *
	 * @return the vertices in the left block
	 */
	default IntSet leftVertices() {
		return blockEdges(0);
	}

	/**
	 * Get the vertices in the 'right' block.
	 * <p>
	 * The right block is the block with index 1.
	 *
	 * @return the vertices in the right block
	 */
	default IntSet rightVertices() {
		return blockEdges(1);
	}

	/**
	 * Get the edges that are contained in the left block.
	 * <p>
	 * The left block is the block with index 0, and edges contained in it are edges with both endpoints in the left
	 * block.
	 *
	 * @return the edges that are contained in the left block
	 */
	default IntSet leftEdges() {
		return blockEdges(0);
	}

	/**
	 * Get the edges that are contained in the right block.
	 * <p>
	 * The right block is the block with index 1, and edges contained in it are edges with both endpoints in the right
	 * block.
	 *
	 * @return the edges that are contained in the right block
	 */
	default IntSet rightEdges() {
		return blockEdges(1);
	}

	/**
	 * Get the edges that cross between the left and right blocks.
	 * <p>
	 * An edge \((u,v)\) is said to cross between two blocks \(b_1\) and \(b_2\) if \(u\) is contained in \(b_1\) and
	 * \(v\) is contained in \(b_2\). Note that if the graph is directed, the cross edges of \((b_1,b_2)\) are different
	 * that \((b_2,b_1)\), since the direction of the edge matters. In that case, the edges returned by this functions
	 * are edges sourced in the left block and targeted in the right block. To get the edges sourced in the right block
	 * and targeted in the left block, use {@code crossEdges(1, 0)}.
	 *
	 * @return the edges that cross between the left and right blocks
	 */
	default IntSet crossEdges() {
		return crossEdges(0, 1);
	}

}
