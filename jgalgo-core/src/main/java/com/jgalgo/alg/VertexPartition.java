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

import java.util.BitSet;
import java.util.function.IntUnaryOperator;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A partition of the vertices of a graph.
 * <p>
 * A partition of a set is a division of the set into a number of disjoint subsets, such that their union is the
 * original set. The sets may also be called 'components' or 'blocks'. We use the term 'block' instead of 'set' to avoid
 * confusion with the get/set convention.
 * <p>
 * The partition represent a mapping from the vertices of a graph to \(B\) blocks, each block is assigned a number in
 * range \([0,B)\). To check to which block a vertex is assigned use {@link #vertexBlock(int)}.
 *
 * @author Barak Ugav
 */
public interface VertexPartition {

	/**
	 * Get the block containing a vertex.
	 *
	 * @param  vertex a vertex in the graph
	 * @return        index of the block containing the vertex, in range \([0, blocksNum)\)
	 */
	int vertexBlock(int vertex);

	/**
	 * Get the number of blocks in the partition.
	 *
	 * @return the number of blocks in the partition, non negative number
	 */
	int numberOfBlocks();

	/**
	 * Get all the vertices that are part of a block.
	 *
	 * @param  block                     index of a block
	 * @return                           the vertices that are part of the blocks
	 * @throws IndexOutOfBoundsException if {@code block} is not in range \([0, blocksNum)\)
	 */
	IntSet blockVertices(int block);

	/**
	 * Get all the edges that are contained in a block.
	 * <p>
	 * An edge \((u,v)\) is contained in a block if both \(u\) and \(v\) are contained in the block.
	 *
	 * @param  block                     index of a block
	 * @return                           the edges that are contained in the blocks
	 * @throws IndexOutOfBoundsException if {@code block} is not in range \([0, blocksNum)\)
	 */
	IntSet blockEdges(int block);

	/**
	 * Get all the edges that cross between two different blocks.
	 * <p>
	 * An edge \((u,v)\) is said to cross between two blocks \(b_1\) and \(b_2\) if \(u\) is contained in \(b_1\) and
	 * \(v\) is contained in \(b_2\). Note that if the graph is directed, the cross edges of \((b_1,b_2)\) are different
	 * that \((b_2,b_1)\), since the direction of the edge matters.
	 *
	 * @param  block1 the first block
	 * @param  block2 the second block
	 * @return        the set of edges that cross between the two blocks
	 */
	IntSet crossEdges(int block1, int block2);

	/**
	 * Get the graph that the partition is defined on.
	 *
	 * @return the graph that the partition is defined on
	 */
	IntGraph graph();

	/**
	 * Create a new graph that contains only the vertices and edges that are contained in a block.
	 * <p>
	 * The returned graph is an induced subgraph of the original graph, namely it contains only the vertices of the
	 * block and edges between them.
	 * <p>
	 * The vertex and edge weights are not copied to the new sub graph. For more coping options see
	 * {@link #blockSubGraph(int, boolean, boolean)}.
	 *
	 * @param  block index of a block
	 * @return       a new graph that contains only the vertices and edges that are contained in the block
	 */
	default IntGraph blockSubGraph(int block) {
		return blockSubGraph(block, false, false);
	}

	/**
	 * Create a new graph that contains only the vertices and edges that are contained in a block with option to copy
	 * weights.
	 * <p>
	 * The returned graph is an induced subgraph of the original graph, namely it contains only the vertices of the
	 * block and edges between them.
	 *
	 * @param  block               index of a block
	 * @param  copyVerticesWeights if {@code true} the vertices weights are copied to the new graph
	 * @param  copyEdgesWeights    if {@code true} the edges weights are copied to the new graph
	 * @return                     a new graph that contains only the vertices and edges that are contained in the block
	 */
	default IntGraph blockSubGraph(int block, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return Graphs.subGraph(graph(), blockVertices(block), blockEdges(block), copyVerticesWeights, copyEdgesWeights);
	}

	/**
	 * Create a new graph representing the edges between the blocks.
	 * <p>
	 * Each vertex in the new graph represents a block, and there is an edge between two blocks if there is an edge
	 * between two original vertices, each in a different block. The vertices of the new graphs will be numbered from
	 * \(0\) to \(B-1\), where \(B\) is the number of blocks in the partition. The edges of the new graph will have the
	 * identifiers of the original edges.
	 * <p>
	 * If there are multiple edges between two blocks, multiple parallel edges will be created in the new graph.
	 * Original edges between vertices in the same block will be ignored, instead of copied as self edges in the new
	 * graph. For more options regarding self and parallel edges see {@link #blocksGraph(boolean, boolean)}.
	 *
	 * @return a new graph where each vertex is a block, and there is an edge between two blocks if there is an edge
	 *         between two original vertices, each in a different block
	 */
	default IntGraph blocksGraph() {
		return blocksGraph(true, false);
	}

	/**
	 * Create a new graph representing the edges between the blocks.
	 * <p>
	 * Each vertex in the new graph represents a block, and there is an edge between two blocks if there is an edge
	 * between two original vertices, each in a different block. The vertices of the new graphs will be numbered from
	 * \(0\) to \(B-1\), where \(B\) is the number of blocks in the partition. The edges of the new graph will have the
	 * identifiers of the original edges.
	 *
	 * @param  parallelEdges if {@code true} multiple parallel edges will be created between two blocks if there are
	 *                           multiple edges between them, if {@code false} only a single edge will be created, with
	 *                           identifier of one arbitrary original edge between the blocks. This is also relevant for
	 *                           self edges, if {@code selfEdges} is {@code true}.
	 * @param  selfEdges     if {@code true} for each original edge between two vertices in the same block, a self edge
	 *                           will be created in the new graph, if {@code false} such edges will be ignored
	 * @return               a new graph where each vertex is a block, and there is an edge between two blocks if there
	 *                       is an edge between two original vertices, each in a different block
	 */
	IntGraph blocksGraph(boolean parallelEdges, boolean selfEdges);

	/**
	 * Create a new vertex partition from a vertex-blockIndex map.
	 * <p>
	 * Note that this function does not validate the input, namely it does not check that the blocks number are all the
	 * range [0, maxBlockIndex], and that there are no 'empty' blocks.
	 *
	 * @param  g   the graph
	 * @param  map a map from vertex to block index
	 * @return     a new vertex partition
	 */
	static VertexPartition fromMap(IntGraph g, Int2IntMap map) {
		return fromMapping(g, map::get);
	}

	/**
	 * Create a new vertex partition from a vertex-blockIndex mapping function.
	 * <p>
	 * Note that this function does not validate the input, namely it does not check that the blocks number are all the
	 * range [0, maxBlockIndex], and that there are no 'empty' blocks.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to block index
	 * @return         a new vertex partition
	 */
	static VertexPartition fromMapping(IntGraph g, IntUnaryOperator mapping) {
		final int n = g.vertices().size();
		int[] vertexToBlock = new int[n];
		if (g instanceof IndexGraph) {
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(v);
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			return new VertexPartitions.Impl((IndexGraph) g, maxBlock + 1, vertexToBlock);
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			int maxBlock = -1;
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(viMap.indexToIdInt(v));
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
			VertexPartition indexPartition = new VertexPartitions.Impl(g.indexGraph(), maxBlock + 1, vertexToBlock);
			return new VertexPartitions.PartitionFromIndexPartition(g, indexPartition);
		}
	}

	/**
	 * Check if a mapping is a valid partition of the vertices of a graph.
	 * <p>
	 * A valid vertex partition is a mapping from each vertex to an integer number in range [0, numberOfBlocks), in
	 * which there are not 'empty blocks', namely at least one vertex is mapped to each block.
	 *
	 * @param  g       the graph
	 * @param  mapping a mapping function that maps from a vertex to block index
	 * @return         {@code true} if the mapping is a valid partition of the vertices of the graph, {@code false}
	 *                 otherwise
	 */
	static boolean isPartition(IntGraph g, IntUnaryOperator mapping) {
		final int n = g.vertices().size();
		int[] vertexToBlock = new int[n];
		int maxBlock = -1;
		if (g instanceof IndexGraph) {
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(v);
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
		} else {
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			for (int v = 0; v < n; v++) {
				vertexToBlock[v] = mapping.applyAsInt(viMap.indexToIdInt(v));
				maxBlock = Math.max(maxBlock, vertexToBlock[v]);
			}
		}
		final int blockNum = maxBlock + 1;
		if (maxBlock > n)
			return false;
		BitSet seenBlocks = new BitSet(blockNum);
		for (int b : vertexToBlock) {
			if (b < 0)
				return false;
			seenBlocks.set(b);
		}
		return seenBlocks.nextClearBit(0) == blockNum;
	}

}
