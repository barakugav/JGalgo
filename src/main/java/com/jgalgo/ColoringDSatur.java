package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ColoringDSatur implements Coloring {

	/**
	 * Compute a coloring approximation in O(m n)
	 */

	@Override
	public Coloring.Result calcColoring(UGraph g) {
		if (Graphs.containsSelfLoops(g))
			throw new IllegalArgumentException("no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();

		IntSet uncolored = new IntOpenHashSet(n);
		BitSet[] usedColors = new BitSet[n];
		int[] usedColorsNum = new int[n];
		int[] degree = new int[n];
		for (int u = 0; u < n; u++) {
			uncolored.add(u);
			usedColors[u] = new BitSet();
			degree[u] = g.degreeOut(u);
		}

		while (!uncolored.isEmpty()) {
			int u = uncolored.iterator().nextInt();
			for (IntIterator it = uncolored.iterator(); it.hasNext();) {
				int u1 = it.nextInt();
				int s1 = usedColorsNum[u], s2 = usedColorsNum[u1];
				if (s1 > s2 || (s1 == s2 && degree[u] > degree[u1]))
					u = u1;
			}

			int color = 0;
			while (usedColors[u].get(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);
			uncolored.remove(u);
			usedColors[u].clear();
			usedColorsNum[u] = 0;
			usedColors[u] = null;

			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (res.colorOf(v) == -1) {/* v is uncolored */
					if (!usedColors[v].get(color)) {
						usedColorsNum[v]++;
						usedColors[v].set(color);
					}
				}
			}
		}
		return res;
	}

}