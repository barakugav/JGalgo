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

import java.util.Arrays;
import java.util.Random;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Karger, Klein and Tarjan randomized linear minimum spanning tree algorithm
 * <p>
 * The algorithm runs in \(O(n + m)\) expected time, and uses linear space in expectation. In practice, this algorithm
 * is out-performed by almost all simpler algorithms. Note that only undirected graphs are supported.
 * <p>
 * Based on "A randomized linear-time algorithm to find minimum spanning trees" by Karger, David R.; Klein, Philip N.;
 * Tarjan, Robert E. (1995).
 *
 * @author Barak Ugav
 */
public class MSTKargerKleinTarjan implements MST {

	private final Random rand;
	private final ConnectivityAlgorithm ccAlg = ConnectivityAlgorithm.newBuilder().build();
	private final MSTBoruvka boruvka = new MSTBoruvka();
	private final TreePathMaxima tpm = new TreePathMaximaHagerup();

	private final AllocatedMemory allocatedMem = new AllocatedMemory();

	/**
	 * Create a new MST algorithm with random seed.
	 */
	public MSTKargerKleinTarjan() {
		this(System.nanoTime() ^ 0x905a1dad25b30034L);
	}

	/**
	 * Create a new MST algorithm with the given seed.
	 *
	 * @param seed a seed used for all random generators
	 */
	public MSTKargerKleinTarjan(long seed) {
		rand = new Random(seed ^ 0x1af7babf9783fd8bL);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	public IntCollection computeMinimumSpanningTree(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyUndirected(g);
		return computeMST(g, w);
	}

	private IntCollection computeMST(Graph g, EdgeWeightFunc w) {
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return IntLists.emptyList();

		/* Run Boruvka to reduce the number of vertices by a factor of 4 by contraction */
		Pair<Graph, IntCollection> r = boruvka.runBoruvka(g, w, 2, "edgeRef_g0");
		Graph g0 = r.first();
		IntCollection f0 = r.second();
		Weights.Int g0Ref = g0.getEdgesWeights("edgeRef_g0");

		/* Find a random subgraph G1 in the contracted graph G0, by choosing each edge with probability 0.5 */
		Graph g1 = randSubgraph(g0, "edgeRef_g1", g0Ref);
		Weights.Int g1Ref = g1.getEdgesWeights("edgeRef_g1");
		Weights.Double g1W = assignWeightsFromEdgeRef(g1, w, "w_g1", g1Ref);

		/* Compute an MST (actually a forest) F1 in the random subgraph G1 */
		IntCollection f1Edges = computeMST(g1, g1W);
		Graph f1 = subGraph(g1, f1Edges, "edgeRef_f1", g1Ref);
		Weights.Int f1Ref = f1.getEdgesWeights("edgeRef_f1");

		/* Find all the light edges in G0 with respect to the computed forest F1 */
		IntCollection e2 = lightEdges(g0, e -> w.weight(g0Ref.getInt(e)), f1, e -> w.weight(f1Ref.getInt(e)));
		Graph g2 = subGraph(g0, e2, "edgeRef_g2", g0Ref);
		Weights.Int g2Ref = g2.getEdgesWeights("edgeRef_g2");
		Weights.Double g2W = assignWeightsFromEdgeRef(g2, w, "w_g2", g2Ref);

		/* The result is F0 and F2 */
		IntCollection f2 = computeMST(g2, g2W);
		for (IntIterator it = f2.iterator(); it.hasNext();) {
			int eRef = it.nextInt();
			int e = g2Ref.getInt(eRef);
			f0.add(e);
		}
		return f0;
	}

	static Graph subGraph(Graph g, IntCollection edgeSet, Object edgeDataKey, Weights.Int edgeRef) {
		Graph subG = new GraphArrayUndirected(g.vertices().size());
		Weights.Int edgeRefSub = subG.addEdgesWeights(edgeDataKey, int.class);
		for (IntIterator it = edgeSet.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int eSub = subG.addEdge(u, v);
			edgeRefSub.set(eSub, edgeRef.getInt(e));
		}
		return subG;
	}

	static Weights.Double assignWeightsFromEdgeRef(Graph g, EdgeWeightFunc w, Object weightKey, Weights.Int edgeRef) {
		Weights.Double w2 = g.addEdgesWeights(weightKey, double.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			w2.set(e, w.weight(edgeRef.getInt(e)));
		}
		return w2;
	}

	private Graph randSubgraph(Graph g, Object edgeRefKey, Weights.Int edgeRef) {
		allocatedMem.allocateForRandSubGraph();
		IntCollection edgeSet = allocatedMem.edgeList;
		edgeSet.clear();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			if (rand.nextBoolean())
				edgeSet.add(e);
		}

		return subGraph(g, edgeSet, edgeRefKey, edgeRef);
	}

	private IntCollection lightEdges(Graph g, Int2DoubleFunction gw, Graph f, Int2DoubleFunction fw) {
		final int n = f.vertices().size();
		/* find connectivity components in the forest, each one of them is a tree */
		ConnectivityAlgorithm.Result connectivityRes = ccAlg.computeConnectivityComponents(f);
		final int treeCount = connectivityRes.getNumberOfCC();
		Int2IntFunction vToTree = connectivityRes::getVertexCc;

		allocatedMem.allocateForLightEdges(n, treeCount);

		Graph[] trees = allocatedMem.trees;
		Weights.Double[] treeData = allocatedMem.treeData;
		for (int t = 0; t < treeCount; t++)
			treeData[t] = trees[t].getEdgesWeights("weight");

		int[] vToVnew = allocatedMem.vToVnew;
		for (int u = 0; u < n; u++) {
			int ut = vToTree.applyAsInt(u);
			vToVnew[u] = trees[ut].addVertex();
		}

		for (IntIterator it = f.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = f.edgeSource(e), v = f.edgeTarget(e);
			int un = vToVnew[u], vn = vToVnew[v];
			int treeIdx = vToTree.applyAsInt(u);
			int en = trees[treeIdx].addEdge(un, vn);
			treeData[treeIdx].set(en, fw.get(e));
		}

		// use the tree path maxima to find the heaviest edge in the path connecting u v for each edge in g
		TreePathMaxima.Queries[] tpmQueries = allocatedMem.tpmQueries;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v))
				continue;
			tpmQueries[ut].addQuery(vToVnew[u], vToVnew[v]);
		}

		int[][] tpmResults = allocatedMem.tpmResults;
		for (int t = 0; t < treeCount; t++) {
			tpmResults[t] = tpm.computeHeaviestEdgeInTreePaths(trees[t], treeData[t], tpmQueries[t]);
			tpmQueries[t].clear();
		}

		// Find all light edge by comparing each edge in g to the heaviest edge on the path from u to v in f
		IntCollection lightEdges = allocatedMem.edgeList;
		lightEdges.clear();
		int[] tpmIdx = allocatedMem.vToVnew;
		Arrays.fill(tpmIdx, 0, treeCount, 0);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ut = vToTree.applyAsInt(u);
			if (ut != vToTree.applyAsInt(v) || gw.get(e) <= treeData[ut].weight(tpmResults[ut][tpmIdx[ut]++]))
				lightEdges.add(e);
		}
		for (int t = 0; t < treeCount; t++)
			trees[t].clear();
		return lightEdges;
	}

	private static class AllocatedMemory {
		IntList edgeList;

		Graph[] trees = MemoryReuse.EmptyGraphArr;
		int[] vToVnew = IntArrays.EMPTY_ARRAY;
		Weights.Double[] treeData = MemoryReuse.EmptyWeightsDoubleArr;

		TreePathMaxima.Queries[] tpmQueries = MemoryReuse.EmptyTpmQueriesArr;
		int[][] tpmResults = IntBigArrays.EMPTY_BIG_ARRAY;

		void allocateForRandSubGraph() {
			edgeList = MemoryReuse.ensureAllocated(edgeList, IntArrayList::new);
		}

		void allocateForLightEdges(int n, int treeCount) {
			edgeList = MemoryReuse.ensureAllocated(edgeList, IntArrayList::new);

			trees = MemoryReuse.ensureLength(trees, treeCount);

			treeData = MemoryReuse.ensureLength(treeData, treeCount);
			vToVnew = MemoryReuse.ensureLength(vToVnew, n);

			tpmQueries = MemoryReuse.ensureLength(tpmQueries, treeCount);
			tpmResults = MemoryReuse.ensureLength(tpmResults, treeCount);

			for (int tIdx = 0; tIdx < treeCount; tIdx++) {
				Graph tree = trees[tIdx] = MemoryReuse.ensureAllocated(trees[tIdx], () -> new GraphArrayUndirected());
				treeData[tIdx] =
						MemoryReuse.ensureAllocated(treeData[tIdx], () -> tree.addEdgesWeights("weight", double.class));

				tpmQueries[tIdx] = MemoryReuse.ensureAllocated(tpmQueries[tIdx], () -> new TreePathMaxima.Queries());
			}
		}
	}

}