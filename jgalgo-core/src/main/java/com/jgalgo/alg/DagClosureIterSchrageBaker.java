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

import java.util.Iterator;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * Schrage-Baker algorithm for enumerating all the closure subsets in a directed acyclic graph.
 *
 * <p>
 * Based on 'Dynamic Programming Solution of Sequencing Problems with Precedence Constraints' by Linus Schrage and
 * Kenneth R. Baker (1978).
 *
 * @author Barak Ugav
 */
class DagClosureIterSchrageBaker {

	private final TopologicalOrderAlgo topoAlgo = TopologicalOrderAlgo.newInstance();

	Iterator<Bitmap> enumerateAllClosures(IndexGraph g) {
		final int n = g.vertices().size();

		int[] topoIdxToV =
				((TopologicalOrderAlgo.IResult) topoAlgo.computeTopologicalSorting(g)).orderedVertices().toIntArray();
		IntArrays.reverse(topoIdxToV);
		int[] vToTopoIndex = new int[n];
		for (int topoIdx = 0; topoIdx < n; topoIdx++)
			vToTopoIndex[topoIdxToV[topoIdx]] = topoIdx;

		Bitmap m = new Bitmap(n);
		return new Iterator<>() {
			int nextClearBit = 0;

			@Override
			public boolean hasNext() {
				return nextClearBit < n;
			}

			@Override
			public Bitmap next() {
				/* Find the smallest positive integer j for which m(j)=0; call it i */
				int i = nextClearBit;

				/* (if m(j)=1 for j=1,...,n then all subsets have been enumerated) */
				Assertions.Iters.hasNext(this);

				/* Set m(i)=1 */
				m.set(i);

				/* For j=i-1 to 1 step -1 */
				cleanLoop: for (int j = i - 1; j >= 0; j--) {
					/* If m(j)=1 and j is in R(j) */
					/* m(j) is always 1, i is the smallest index for which m(i)=0 // if (!m.get(j)) continue; */
					for (IEdgeIter eit = g.inEdges(topoIdxToV[j]).iterator(); eit.hasNext();) {
						eit.nextInt();
						int predecessor = eit.sourceInt();
						assert j < vToTopoIndex[predecessor];
						if (m.get(vToTopoIndex[predecessor]))
							continue cleanLoop;
					}
					/* Let m(j)=0 */
					m.clear(j);
				}

				Bitmap closure = new Bitmap(n);
				for (int topoIdx : m)
					closure.set(topoIdxToV[topoIdx]);

				nextClearBit = m.nextClearBit(0);

				return closure;
			}
		};
	}

}
