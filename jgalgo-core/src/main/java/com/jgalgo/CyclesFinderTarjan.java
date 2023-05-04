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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Tarjan's algorithm for finding all cycles in a directed graph.
 * <p>
 * The algorithm runs in \(O((n+m)(c+1))\) time and \(O(n)\) space where \(c\) is the number of simple cycles in the
 * graph.
 * <p>
 * Based on the paper 'Enumeration of the elementary circuits of a directed graph' by Robert Tarjan.
 *
 * @author Barak Ugav
 */
public class CyclesFinderTarjan implements CyclesFinder {

	/**
	 * Create a new cycles finder algorithm object.
	 */
	public CyclesFinderTarjan() {}

	@Override
	public List<Path> findAllCycles(Graph g) {
		ArgumentCheck.onlyDirected(g);
		Worker worker = new Worker(g);
		int n = g.vertices().size();
		for (int s = 0; s < n; s++) {
			worker.findAllCycles(s);
			worker.reset();
		}
		return worker.cycles;
	}

	private static class Worker {
		private final Graph g;
		private final IntStack path = new IntArrayList();
		private final IntStack markedStack = new IntArrayList();
		private final BitSet isMarked;
		private final List<Path> cycles = new ArrayList<>();

		Worker(Graph g) {
			this.g = g;
			int n = g.vertices().size();
			isMarked = new BitSet(n);
		}

		void reset() {
			((IntArrayList) path).clear();
			((IntArrayList) markedStack).clear();
			isMarked.clear();
		}

		boolean findAllCycles(int startV) {
			boolean cycleFound = false;

			int u = path.isEmpty() ? startV : g.edgeTarget(path.topInt());
			isMarked.set(u);
			markedStack.push(u);

			for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
				int e = it.nextInt();
				int v = it.target();
				if (v < startV)
					continue;
				if (v == startV) {
					path.push(e);
					cycles.add(new Path(g, startV, startV, new IntArrayList((IntArrayList) path)));
					path.popInt();
					cycleFound = true;
				} else if (!isMarked.get(v)) {
					path.push(e);
					if (findAllCycles(startV))
						cycleFound = true;
					path.popInt();
				}
			}
			if (cycleFound) {
				while (markedStack.topInt() != u) {
					int w = markedStack.popInt();
					isMarked.clear(w);
				}
				markedStack.popInt();
				isMarked.clear(u);
			}

			return cycleFound;
		}
	}

}