package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * TSP 3/2-approximation using maximum matching.
 * <p>
 * The running of this algorithm is {@code O(n^3)} and it achieve
 * 3/2-approximation to the optimal TSP solution.
 *
 * @author Barak Ugav
 */
public class TSPMetricMatchingAppx implements TSPMetric {

	private static final Object EdgeWeightKey = new Object();
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new TSP 3/2-approximation algorithm.
	 */
	public TSPMetricMatchingAppx() {
	}

	@Override
	public int[] computeShortestTour(double[][] distances) {
		int n = distances.length;
		if (n == 0)
			return IntArrays.EMPTY_ARRAY;
		TSPMetricUtils.checkArgDistanceTableSymmetric(distances);
		TSPMetricUtils.checkArgDistanceTableIsMetric(distances);

		/* Build graph from the distances table */
		UGraph g = new GraphTableUndirected(n);
		Weights.Double weights = g.addEdgesWeights(EdgeWeightKey, double.class);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				weights.set(g.addEdge(u, v), distances[u][v]);

		/* Calculate MST */
		IntCollection mst = new MSTPrim().computeMinimumSpanningTree(g, weights);

		/*
		 * Build graph for the matching calculation, containing only vertices with odd
		 * degree from the MST
		 */
		int[] degree = Graphs.calcDegree(g, mst);
		UGraph mG = new GraphArrayUndirected();
		int[] mVtoV = new int[n];
		for (int u = 0; u < n; u++)
			if (degree[u] % 2 == 1)
				mVtoV[mG.addVertex()] = u;
		int mGn = mG.vertices().size();
		Weights.Double mGWeightsNeg = mG.addEdgesWeights(EdgeWeightKey, double.class);
		Weights.Int mGEdgeRef = mG.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		for (int u = 0; u < mGn; u++) {
			for (int v = u + 1; v < mGn; v++) {
				int e = g.getEdge(mVtoV[u], mVtoV[v]);
				int en = mG.addEdge(u, v);
				mGWeightsNeg.set(en, -distances[mVtoV[u]][mVtoV[v]]);
				mGEdgeRef.set(en, e);
			}
		}

		/* Calculate maximum matching between the odd vertices */
		IntCollection matching = new MaximumMatchingWeightedGabow1990().computeMaximumPerfectMatching(mG, mGWeightsNeg);

		/* Build a graph of the union of the MST and the matching result */
		UGraph g1 = new GraphArrayUndirected(n);
		Weights.Int g1EdgeRef = g1.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		for (IntIterator it = mst.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int g1Edge = g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
			g1EdgeRef.set(g1Edge, e);
		}
		for (IntIterator it = matching.iterator(); it.hasNext();) {
			int mGedge = it.nextInt();
			int u = mVtoV[mG.edgeSource(mGedge)];
			int v = mVtoV[mG.edgeTarget(mGedge)];
			int g1Edge = g1.addEdge(u, v);
			g1EdgeRef.set(g1Edge, mGEdgeRef.getInt(mGedge));
		}

		Path cycle = TSPMetricUtils.calcEulerianTourAndConvertToHamiltonianCycle(g, g1, g1EdgeRef);

		/* Convert cycle of edges to list of vertices */
		int[] res = TSPMetricUtils.pathToVerticesList(cycle).toIntArray();

		mG.clear();

		return res;
	}

}