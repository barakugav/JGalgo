package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingWeightedBipartiteHungarianMethod implements MatchingWeighted {

	/*
	 * O(m n + n^2 log n)
	 */

	public MatchingWeightedBipartiteHungarianMethod() {
	}

	@Override
	public IntCollection calcMaxMatching(Graph g0, WeightFunction w) {
		if (!(g0 instanceof GraphBipartite.Undirected))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite.Undirected g = (GraphBipartite.Undirected) g0;
		return new Worker(g, w).calcMaxMatching(false);
	}

	@Override
	public IntCollection calcPerfectMaxMatching(Graph g0, WeightFunction w) {
		if (!(g0 instanceof GraphBipartite.Undirected))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite.Undirected g = (GraphBipartite.Undirected) g0;
		return new Worker(g, w).calcMaxMatching(true);
	}

	private static class Worker {

		private final GraphBipartite.Undirected g;
		private final WeightFunction w;

		private final boolean[] inTree;

		private final IntComparator edgeSlackComparator;
		private final HeapDirectAccessed<Integer> nextTightEdge;
		private final HeapDirectAccessed.Handle<Integer>[] nextTightEdgePerOutV;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		@SuppressWarnings("unchecked")
		Worker(GraphBipartite.Undirected g, WeightFunction w) {
			this.g = g;
			this.w = w;
			int n = g.vertices();

			inTree = new boolean[n];

			edgeSlackComparator = (e1, e2) -> Utils.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = new HeapFibonacci<>(edgeSlackComparator);
			nextTightEdgePerOutV = new HeapDirectAccessed.Handle[n];

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		IntCollection calcMaxMatching(boolean perfect) {
			final int n = g.vertices(), m = g.edges();
			final int EdgeNone = -1;

			int[] parent = new int[n];
			int[] matched = new int[n];
			Arrays.fill(matched, EdgeNone);

			double maxWeight = Double.MIN_VALUE;
			for (int e = 0; e < m; e++)
				maxWeight = Math.max(maxWeight, w.weight(e));
			final double delta1Threshold = maxWeight;
			for (int u = 0; u < n; u++)
				if (g.isVertexInS(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				Arrays.fill(parent, EdgeNone);

				// Start growing tree from all unmatched vertices in S
				for (int u = 0; u < n; u++) {
					if (!g.isVertexInS(u) || matched[u] != EdgeNone)
						continue;
					vertexAddedToTree(u);
					for (EdgeIter eit = g.edges(u); eit.hasNext();) {
						int e = eit.nextInt();
						nextTightEdgeAdd(u, e);
					}
				}

				currentTree: for (;;) {
					while (!nextTightEdge.isEmpty()) {
						int e = nextTightEdge.findMin();
						int u0 = g.getEdgeSource(e), v0 = g.getEdgeTarget(e);

						if (inTree[u0] && inTree[v0]) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdge.extractMin();
							continue;
						}
						int v = inTree[u0] ? v0 : u0;

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdge.extractMin();
						parent[v] = e;
						vertexAddedToTree(v);

						int matchedEdge = matched[v];
						if (matchedEdge == EdgeNone) {
							for (;;) {
								// Augmenting path
								e = parent[v];
								matched[v] = matched[v = g.getEdgeEndpoint(e, v)] = e;
								// TODO don't set parent[odd vertex]
								e = parent[v];
								if (e == EdgeNone)
									break currentTree;
								v = g.getEdgeEndpoint(e, v);
							}
						}

						// Added odd vertex, immediately add it's matched edge and even vertex
						v = g.getEdgeEndpoint(matchedEdge, v);
						parent[v] = matchedEdge;
						vertexAddedToTree(v);

						for (EdgeIter eit = g.edges(v); eit.hasNext();) {
							int e1 = eit.nextInt();
							nextTightEdgeAdd(v, e1);
						}
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 = nextTightEdge.isEmpty() ? -1 : edgeSlack(nextTightEdge.findMin());
					if ((!perfect && delta1 <= delta2) || delta2 == -1)
						break mainLoop;
					deltaTotal += delta2;
				}

				// Update dual values base
				for (int u = 0; u < n; u++)
					if (inTree[u])
						dualValBase[u] = dualVal(u);
				Arrays.fill(dualVal0, 0);

				// Reset tree
				Arrays.fill(inTree, false);

				// Reset heap
				nextTightEdge.clear();
				Arrays.fill(nextTightEdgePerOutV, null);
			}

			IntList res = new IntArrayList();
			for (int u = 0; u < n; u++)
				if (g.isVertexInS(u) && matched[u] != EdgeNone)
					res.add(matched[u]);
			return res;
		}

		private void nextTightEdgeAdd(int u, int e) {
			int v = g.getEdgeEndpoint(e, u);
			HeapDirectAccessed.Handle<Integer> handle = nextTightEdgePerOutV[v];
			if (handle == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(e);
			else if (edgeSlackComparator.compare(e, handle.get().intValue()) < 0)
				nextTightEdge.decreaseKey(handle, e);
		}

		private double dualVal(int v) {
			return inTree[v] ? dualVal0[v] + (g.isVertexInS(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		private double edgeSlack(int e) {
			return dualVal(g.getEdgeSource(e)) + dualVal(g.getEdgeTarget(e)) - w.weight(e);
		}

		private void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (g.isVertexInS(v) ? deltaTotal : -deltaTotal);
			inTree[v] = true;
		}

	}

}
