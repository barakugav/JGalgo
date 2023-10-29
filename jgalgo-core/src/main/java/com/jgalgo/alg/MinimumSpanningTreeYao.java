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

import java.util.Arrays;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Yao's buckets minimum spanning tree algorithm.
 * <p>
 * The algorithm runs in \(O(m \log \log n + n \log n)\) and uses linear space. Its running time in practice is not the
 * best compared to {@link MinimumSpanningTreeKruskal} and {@link MinimumSpanningTreePrim}. Note that only undirected
 * graphs are supported.
 * <p>
 * Based on "An 0(|E|loglog|V|) algorithm for finding minimum spanning trees" by Andrew Chi-chih Yao (1976).
 *
 * @author Barak Ugav
 */
class MinimumSpanningTreeYao extends MinimumSpanningTreeUtils.AbstractUndirected {

	private boolean parallel = JGAlgoConfigImpl.ParallelByDefault;

	/**
	 * Construct a new MST algorithm object.
	 */
	MinimumSpanningTreeYao() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	MinimumSpanningTree.Result computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		int n = g.vertices().size();

		int[][][] edges = partitionEdgesToBuckets(g, w);
		int[] firstValidBucketIdxs = new int[n];

		int treeNum = n;
		int[] vTree = new int[n];
		int[] vTreeNext = new int[n];
		for (int v = 0; v < n; v++)
			vTree[v] = v;

		int[] minEdges = new int[n];
		Arrays.fill(minEdges, -1);
		double[] minEdgeWeights = new double[n];
		int[] path = new int[n];

		IntCollection mst = new IntArrayList();
		for (;;) {
			Arrays.fill(minEdgeWeights, 0, treeNum, Double.MAX_VALUE);

			/* find minimum edge going out of each tree */
			for (int u = 0; u < n; u++) {
				int tree = vTree[u];

				int[][] vertexBuckets = edges[u];
				int b;
				for (b = firstValidBucketIdxs[u]; b < vertexBuckets.length; b++) {
					boolean foundEdge = false;
					for (int i = 0; i < vertexBuckets[b].length; i++) {
						int e = vertexBuckets[b][i];
						if (tree == vTree[g.edgeSource(e)] && tree == vTree[g.edgeTarget(e)])
							continue;
						foundEdge = true;

						double eWeight = w.weight(e);
						if (eWeight < minEdgeWeights[tree]) {
							minEdges[tree] = e;
							minEdgeWeights[tree] = eWeight;
						}
					}
					if (foundEdge)
						break;
				}
				firstValidBucketIdxs[u] = b;
			}

			/* add min edges to MST */
			for (int tree = 0; tree < treeNum; tree++) {
				if (minEdges[tree] != -1) {
					int e = minEdges[tree];
					int ut = vTree[g.edgeSource(e)], vt = vTree[g.edgeTarget(e)];
					if (tree == vt) {
						int temp = ut;
						ut = vt;
						vt = temp;
					} else {
						assert tree == ut;
					}
					if (minEdges[vt] != e || ut < vt)
						mst.add(minEdges[tree]);
				}
			}

			/*
			 * the graph of the trees (vertex per tree, minimum out edges of the trees) is a graph where each vertex has
			 * one out edge at most we want to find all the connectivity components between the trees and label the
			 * vertices of G with new trees indices
			 */
			final int UNVISITED = -1;
			final int IN_PATH = -2;
			Arrays.fill(vTreeNext, 0, treeNum, UNVISITED);
			int treeNumNext = 0;
			for (int t = 0; t < treeNum; t++) {
				int pathLength = 0;
				/* find all reachable trees from t */
				for (int tPtr = t;;) {
					if (vTreeNext[tPtr] == UNVISITED) {
						/* another vertex on the path, continue */
						path[pathLength++] = tPtr;
						vTreeNext[tPtr] = IN_PATH;

						if (minEdges[tPtr] != -1) {
							int nextTPtr;
							if ((nextTPtr = vTree[g.edgeSource(minEdges[tPtr])]) == tPtr)
								nextTPtr = vTree[g.edgeTarget(minEdges[tPtr])];
							assert nextTPtr != tPtr;
							tPtr = nextTPtr;
							continue;
						}
					}

					/* if found label tree use it label, else - add new label */
					int newTree = vTreeNext[tPtr] >= 0 ? vTreeNext[tPtr] : treeNumNext++;
					/* assign the new label to all trees on path */
					while (pathLength-- > 0)
						vTreeNext[path[pathLength]] = newTree;
					break;
				}
			}

			if (treeNum == treeNumNext)
				break;
			treeNum = treeNumNext;
			Arrays.fill(minEdges, 0, treeNum, -1);

			/* assign new tree indices to G's vertices */
			for (int v = 0; v < n; v++)
				vTree[v] = vTreeNext[vTree[v]];
		}

		return new MinimumSpanningTreeUtils.ResultImpl(mst);
	}

	private int[][][] partitionEdgesToBuckets(IndexGraph g, IWeightFunction w) {
		int n = g.vertices().size(), k = JGAlgoUtils.log2ceil(n);

		int[][][] edges = new int[n][][];
		int[] edgesTemp = new int[n];

		for (int u = 0; u < n; u++) {
			int edgesCount = 0;
			for (int e : g.outEdges(u))
				edgesTemp[edgesCount++] = e;

			if (edgesCount <= k) {
				JGAlgoUtils.sort(edgesTemp, 0, edgesCount, w, parallel);
				edges[u] = new int[edgesCount][];
				for (int i = 0; i < edgesCount; i++)
					edges[u][i] = new int[] { edgesTemp[i] };

			} else {
				int bucketSize = (edgesCount - 1) / k + 1;
				int bucketNum = (edgesCount - 1) / bucketSize + 1;
				ArraysUtils.bucketPartition(edgesTemp, 0, edgesCount, w, bucketSize);
				edges[u] = new int[bucketNum][];

				for (int b = 0; b < bucketNum; b++) {
					int bucketBegin = b * bucketSize;
					int bucketEnd = Math.min(bucketBegin + bucketSize, edgesCount);
					int[] bucket = new int[bucketEnd - bucketBegin];
					System.arraycopy(edgesTemp, bucketBegin, bucket, 0, bucketEnd - bucketBegin);
					edges[u][b] = bucket;
				}
			}
		}

		return edges;
	}

}
