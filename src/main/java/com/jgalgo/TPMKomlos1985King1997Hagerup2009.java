package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;

import com.jgalgo.Utils.BiInt2IntFunction;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class TPMKomlos1985King1997Hagerup2009 implements TPM {

	/*
	 * O(m + n) where m is the number of queries
	 */

	private boolean useBitsLookupTables = false;

	private static final Object EdgeRefWeightKey = new Object();

	public TPMKomlos1985King1997Hagerup2009() {
	}

	public void useBitsLookupTables(boolean enable) {
		useBitsLookupTables = enable;
	}

	@Override
	public int[] calcTPM(Graph t, EdgeWeightFunc w, int[] queries, int queriesNum) {
		if (!(t instanceof UGraph))
			throw new IllegalArgumentException("only undirected graphs are supported");
		if (queries.length / 2 < queriesNum)
			throw new IllegalArgumentException("queries should be in format [u0, v0, u1, v1, ...]");
		if (!Trees.isTree((UGraph) t))
			throw new IllegalArgumentException("only trees are supported");
		if (t.vertices().size() == 0)
			return new int[queriesNum];
		return new Worker((UGraph) t, w, useBitsLookupTables).calcTPM(queries, queriesNum);
	}

	private static class Worker {

		/*
		 * Original tree, in other functions 't' refers to the Boruvka fully branching
		 * tree
		 */
		final UGraph tOrig;
		final EdgeWeightFunc w;
		private final Int2IntFunction getBitCount;
		private final BiInt2IntFunction getIthbit;
		private final Int2IntFunction getNumberOfTrailingZeros;

		Worker(UGraph t, EdgeWeightFunc w, boolean useBitsLookupTables) {
			this.tOrig = t;
			this.w = w;

			if (useBitsLookupTables) {
				int n = t.vertices().size();
				int wordsize = n > 1 ? Utils.log2ceil(n) : 1;
				BitsLookupTable.Count count = new BitsLookupTable.Count(wordsize);
				BitsLookupTable.Ith ith = new BitsLookupTable.Ith(wordsize, count);

				getBitCount = count::bitCount;
				getIthbit = ith::ithBit;
				getNumberOfTrailingZeros = ith::numberOfTrailingZeros;
			} else {
				getBitCount = Integer::bitCount;
				getIthbit = (x, i) -> {
					if (i < 0 || i >= getBitCount.applyAsInt(x))
						throw new IndexOutOfBoundsException(Integer.toBinaryString(x) + "[" + i + "]");
					for (; i > 0; i--) {
						int z = Integer.numberOfTrailingZeros(x);
						x &= ~(1 << z);
					}
					return Integer.numberOfTrailingZeros(x);
				};
				getNumberOfTrailingZeros = Integer::numberOfTrailingZeros;
			}
		}

		int[] calcTPM(int[] queries, int queriesNum) {
			Pair<UGraph, Integer> r = buildBoruvkaFullyBranchingTree();
			UGraph t = r.e1;
			int root = r.e2.intValue();

			int[] lcaQueries = splitQueriesIntoLCAQueries(t, root, queries, queriesNum);

			Pair<int[], int[]> r2 = getEdgeToParentsAndDepth(t, root);
			int[] edgeToParent = r2.e1;
			int[] depths = r2.e2;

			int[] q = calcQueriesPerVertex(t, lcaQueries, depths, edgeToParent);
			int[][] a = calcAnswersPerVertex(t, root, q, edgeToParent);
			return extractEdgesFromAnswers(a, q, lcaQueries, depths, t.edgesWeight("edgeData"));
		}

		private int[] extractEdgesFromAnswers(int[][] a, int[] q, int[] lcaQueries, int[] depths,
				Weights.Int edgeData) {
			int queriesNum = lcaQueries.length / 4;
			int[] res = new int[queriesNum];

			for (int i = 0; i < queriesNum; i++) {
				int u = lcaQueries[i * 4];
				int v = lcaQueries[i * 4 + 2];
				int lca = lcaQueries[i * 4 + 1];
				int lcaDepth = depths[lca];

				int ua = -1, va = -1;

				int qusize = getBitCount.applyAsInt(q[u]);
				for (int j = 0; j < qusize; j++) {
					if (getIthbit.apply(q[u], j) == lcaDepth) {
						ua = a[u][j];
						break;
					}
				}
				int qvsize = getBitCount.applyAsInt(q[v]);
				for (int j = 0; j < qvsize; j++) {
					if (getIthbit.apply(q[v], j) == lcaDepth) {
						va = a[v][j];
						break;
					}
				}

				res[i] = (va == -1 || (ua != -1 && w.weight(edgeData.getInt(ua)) >= w.weight(edgeData.getInt(va))))
						? (ua != -1 ? edgeData.getInt(ua) : -1)
						: (va != -1 ? edgeData.getInt(va) : -1);
			}

			return res;
		}

		private int[][] calcAnswersPerVertex(UGraph t, int root, int[] q, int[] edgeToParent) {
			int n = t.vertices().size();
			int[] a = new int[n];

			int leavesDepth = Graphs.getFullyBranchingTreeDepth(t, root);

			Weights.Int tData = t.edgesWeight("edgeData");
			int[][] res = new int[tOrig.vertices().size()][];

			for (DFSIter it = new DFSIter(t, root); it.hasNext();) {
				int v = it.nextInt();
				IntList edgesFromRoot = it.edgePath();
				if (edgesFromRoot.isEmpty())
					continue;
				int depth = edgesFromRoot.size();
				int edgeToChild = edgesFromRoot.getInt(depth - 1);
				int u = t.edgeEndpoint(edgeToChild, v);

				a[v] = subseq(a[u], q[u], q[v]);
				int j = binarySearch(a[v], w.weight(tData.getInt(edgeToChild)), edgesFromRoot, tData);
				a[v] = repSuf(a[v], depth, j);

				if (depth == leavesDepth) {
					int qvsize = getBitCount.applyAsInt(q[v]);
					int[] resv = new int[qvsize];
					for (int i = 0; i < qvsize; i++) {
						int b = getIthbit.apply(q[v], i);
						int s = getNumberOfTrailingZeros.applyAsInt(successor(a[v], 1 << b) >> 1);
						resv[i] = edgesFromRoot.getInt(s);
					}
					res[v] = resv;
				}
			}
			return res;
		}

		private static int successor(int a, int b) {
			// int r = 0, bsize = Integer.bitCount(b);
			// for (int i = 0; i < bsize; i++)
			// for (int bit = getIthOneBit(b, i) + 1; bit < Integer.SIZE; bit++)
			// if ((a & (1 << bit)) != 0) {
			// r |= 1 << bit;
			// break;
			// }
			// return r;

			/*
			 * Don't even ask why the commented code above is equivalent to the bit tricks
			 * below. Hagerup 2009.
			 */
			return a & (~(a | b) ^ ((~a | b) + b));
		}

		private static int subseq(int au, int qu, int qv) {
			return successor(au, qv);
		}

		private int binarySearch(int av, double weight, IntList edgesToRoot, Weights.Int edgeData) {
			int avsize = getBitCount.applyAsInt(av);
			if (avsize == 0 || w.weight(edgeData.getInt(edgesToRoot.getInt(getIthbit.apply(av, 0) - 1))) < weight)
				return 0;

			for (int from = 0, to = avsize;;) {
				if (from == to - 1)
					return getIthbit.apply(av, from) + 1;
				int mid = (from + to) / 2;
				int avi = getIthbit.apply(av, mid);
				if (w.weight(edgeData.getInt(edgesToRoot.getInt(avi - 1))) >= weight)
					from = mid;
				else
					to = mid;
			}
		}

		private static int repSuf(int av, int depth, int j) {
			av &= (1 << j) - 1;
			av |= 1 << depth;
			return av;
		}

		private Pair<UGraph, Integer> buildBoruvkaFullyBranchingTree() {
			int n = tOrig.vertices().size();
			int[] minEdges = new int[n];
			double[] minGraphWeights = new double[n];
			int[] vNext = new int[n];
			int[] path = new int[n];
			int[] vTv = new int[n];
			int[] vTvNext = new int[n];

			for (int v = 0; v < n; v++)
				vTv[v] = v;

			UGraph t = new GraphArrayUndirected(n);
			Weights.Int tData = t.addEdgesWeights("edgeData", int.class, Integer.valueOf(-1));
			for (UGraph G = Graphs.referenceGraph(tOrig, EdgeRefWeightKey); (n = G.vertices().size()) > 1;) {
				Weights.Int GData = G.edgesWeight(EdgeRefWeightKey);

				// Find minimum edge of each vertex
				Arrays.fill(minEdges, 0, n, -1);
				Arrays.fill(minGraphWeights, 0, n, Double.MAX_VALUE);
				for (int u = 0; u < n; u++) {
					for (EdgeIter eit = G.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						double eWeight = w.weight(GData.getInt(e));
						if (eWeight < minGraphWeights[u]) {
							minEdges[u] = e;
							minGraphWeights[u] = eWeight;
						}
					}
				}

				// find connectivity components, and label each vertex with new super vertex
				final int UNVISITED = -1;
				final int IN_PATH = -2;
				Arrays.fill(vNext, 0, n, UNVISITED);
				int nNext = 0;
				for (int u = 0; u < n; u++) {
					int pathLength = 0;
					// find all reachable vertices from u
					for (int p = u;;) {
						if (vNext[p] == UNVISITED) {
							// another vertex on the path, continue
							path[pathLength++] = p;
							vNext[p] = IN_PATH;

							p = G.edgeEndpoint(minEdges[p], p);
							continue;
						}

						// if found label use it label, else - add new label
						int V = vNext[p] >= 0 ? vNext[p] : nNext++;
						// assign the new label to all trees on path
						while (pathLength-- > 0)
							vNext[path[pathLength]] = V;
						break;
					}
				}

				// construct new layer in the output tree graph
				for (int V = 0; V < nNext; V++)
					vTvNext[V] = t.addVertex();
				for (int u = 0; u < n; u++) {
					int e = t.addEdge(vTv[u], vTvNext[vNext[u]]);
					tData.set(e, GData.getInt(minEdges[u]));
				}
				int[] temp = vTv;
				vTv = vTvNext;
				vTvNext = temp;

				// contract G to new graph with the super vertices
				UGraph gNext = new GraphArrayUndirected(nNext);
				Weights.Int gNextData = gNext.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
				for (int u = 0; u < n; u++) {
					int U = vNext[u];
					for (EdgeIter eit = G.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int V = vNext[eit.v()];
						if (U != V) {
							int E = gNext.addEdge(U, V);
							gNextData.set(E, GData.getInt(e));
						}
					}
				}

				G.clear();
				G = gNext;
			}
			return Pair.of(t, Integer.valueOf(vTv[0]));
		}

		private static int[] splitQueriesIntoLCAQueries(UGraph t, int root, int[] queries, int queriesNum) {
			int[] lcaQueries = new int[queriesNum * 4];

			LCAStatic lcaAlgo = new LCARMQBenderFarachColton2000();
			LCAStatic.DataStructure lcaDS = lcaAlgo.preProcessTree(t, root);
			for (int q = 0; q < queriesNum; q++) {
				int u = queries[q * 2], v = queries[q * 2 + 1];
				int lca = lcaDS.findLowestCommonAncestor(u, v);
				lcaQueries[q * 4] = u;
				lcaQueries[q * 4 + 1] = lca;
				lcaQueries[q * 4 + 2] = v;
				lcaQueries[q * 4 + 3] = lca;
			}
			return lcaQueries;
		}

		private static Pair<int[], int[]> getEdgeToParentsAndDepth(UGraph t, int root) {
			int n = t.vertices().size();
			int[] edgeToParent = new int[n];
			Arrays.fill(edgeToParent, -1);
			int[] depths = new int[n];

			for (BFSIter it = new BFSIter(t, root); it.hasNext();) {
				int v = it.nextInt();
				int e = it.inEdge();
				if (e != -1) {
					edgeToParent[v] = e;
					depths[v] = depths[t.edgeEndpoint(e, v)] + 1;
				}
			}

			return Pair.of(edgeToParent, depths);
		}

		private static int[] calcQueriesPerVertex(UGraph g, int[] lcaQueries, int[] depths, int[] edgeToParent) {
			final int n = g.vertices().size();

			int[] q = new int[n];
			Arrays.fill(q, 0);

			int queriesNum = lcaQueries.length / 2;
			for (int query = 0; query < queriesNum; query++) {
				int u = lcaQueries[query * 2];
				int ancestor = lcaQueries[query * 2 + 1];
				if (u == ancestor)
					continue;
				q[u] |= 1 << depths[ancestor];
			}

			/* Start traversing the full branching tree from the leaves upwards */
			int maxDepth = -1;
			for (int u = 0; u < n; u++)
				if (depths[u] > maxDepth)
					maxDepth = depths[u];
			IntPriorityQueue queue = new IntArrayFIFOQueue();
			BitSet queued = new BitSet(n);
			for (int u = 0; u < n; u++) {
				if (depths[u] == maxDepth) {
					queue.enqueue(u);
					queued.set(u);
				}
			}

			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				int ep = edgeToParent[u];
				if (ep == -1)
					continue;
				int parent = g.edgeEndpoint(ep, u);
				q[parent] |= q[u] & ~(1 << depths[parent]);

				if (queued.get(parent))
					continue;
				queue.enqueue(parent);
				queued.set(parent);
			}

			return q;
		}

	}

}
