package com.jgalgo;

import java.util.Arrays;

public class LCARMQBenderFarachColton2000 implements LCAStatic {

	/*
	 * This implementation of static LCA (Lowest common ancestor) perform a
	 * pre processing of O(n + m) and later answer queries of LCA queries in O(1).
	 *
	 * This is done by traversing the tree with the Eulerian tour, and using RMQ on
	 * the depths of the tour. This RMQ is a special case of the general RMQ, as the
	 * difference between two consecutive elements is always +1/-1, and therefore
	 * allow more efficient implementation using
	 * RMQPlusMinusOneBenderFarachColton2000.
	 */

	private int[] vs;
	private int[] vToDepthsIdx;
	private final RMQ rmq;
	private boolean preProcessed;

	public LCARMQBenderFarachColton2000() {
		rmq = new RMQPlusMinusOneBenderFarachColton2000();
		preProcessed = false;
	}

	@Override
	public void preProcessLCA(Graph t, int r) {
		if (!Trees.isTree(t, r))
			throw new IllegalArgumentException();

		int n = t.vertices().size();
		int[] depths = new int[n * 2];
		int[] vs = new int[n * 2];
		int[] parent = new int[n];

		EdgeIter[] edges = new EdgeIter[n];

		parent[0] = -1;
		edges[0] = t.edgesOut(r);

		int aLen = 0;
		dfs: for (int u = r, depth = 0;;) {
			depths[aLen] = depth;
			vs[aLen] = u;
			aLen++;

			for (EdgeIter eit = edges[depth]; eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (v == parent[depth])
					continue;
				depth++;
				parent[depth] = u;
				edges[depth] = t.edgesOut(v);
				u = v;
				continue dfs;
			}
			u = parent[depth];
			if (--depth < 0)
				break;
		}

		depths = Arrays.copyOf(depths, aLen);
		vs = Arrays.copyOf(vs, aLen);

		int[] vToDepthsIdx = new int[n];
		Arrays.fill(vToDepthsIdx, -1);
		for (int i = 0; i < aLen; i++) {
			int v = vs[i];
			if (vToDepthsIdx[v] == -1)
				vToDepthsIdx[v] = i;
		}

		rmq.preProcessRMQ(RMQComparator.ofIntArray(depths), depths.length);
		this.vs = vs;
		this.vToDepthsIdx = vToDepthsIdx;
		preProcessed = true;
	}

	@Override
	public int calcLCA(int u, int v) {
		if (!preProcessed)
			throw new IllegalStateException("PreProcessing is required before query");
		int uIdx = vToDepthsIdx[u];
		int vIdx = vToDepthsIdx[v];
		if (uIdx > vIdx) {
			int temp = uIdx;
			uIdx = vIdx;
			vIdx = temp;
		}
		int lcaIdx = rmq.calcRMQ(uIdx, vIdx + 1);
		return vs[lcaIdx];
	}

}
