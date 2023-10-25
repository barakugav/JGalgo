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
import java.util.Iterator;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The Bron-Kerbosch algorithm for Maximal cliques.
 * <p>
 * Based on 'Algorithm 457: finding all cliques of an undirected graph' by Coen Bron and Joep Kerbosch.
 *
 * @author Barak Ugav
 */
class MaximalCliquesBronKerbosch extends MaximalCliquesUtils.AbstractImpl {

	@Override
	Iterator<IntSet> iterateMaximalCliques(IndexGraph g) {
		Assertions.Graphs.onlyUndirected(g);

		return new Iterator<>() {

			final int n = g.vertices().size();
			final BitSet edges;

			final IntList currentClique = new IntArrayList();
			final Stack<IntList> potentialStack = new ObjectArrayList<>();
			final Stack<IntList> excludedStack = new ObjectArrayList<>();
			boolean hasNextClique;

			{
				edges = new BitSet(n * n);
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					if (u == v)
						continue;
					edges.set(u * n + v);
					edges.set(v * n + u);
				}
				potentialStack.push(new IntArrayList(g.vertices()));
				excludedStack.push(new IntArrayList());

				findNextClique();
			}

			private boolean containsEdge(int u, int v) {
				return edges.get(u * n + v);
			}

			@Override
			public boolean hasNext() {
				return hasNextClique;
			}

			@Override
			public IntSet next() {
				Assertions.Iters.hasNext(this);
				IntSet ret = ImmutableIntArraySet.withNaiveContains(currentClique.toIntArray());
				removeLastInsertedVertex(potentialStack.top(), excludedStack.top());
				findNextClique();
				return ret;
			}

			private void findNextClique() {
				IntList potential = potentialStack.top();
				IntList excluded = excludedStack.top();

				recursionLoop: for (;;) {
					assert !potential.isEmpty() || !excluded.isEmpty();

					while (!potential.isEmpty()) {
						int v = potential.getInt(potential.size() - 1);

						currentClique.add(v);
						IntList nextPotential = new IntArrayList();
						for (int u : potential)
							if (containsEdge(u, v))
								nextPotential.add(u);
						IntList nextExcluded = new IntArrayList();
						for (int u : excluded)
							if (containsEdge(u, v))
								nextExcluded.add(u);
						if (nextPotential.isEmpty() && nextExcluded.isEmpty()) {
							hasNextClique = true;
							return;
						}

						boolean skip = false;
						excludedLoop: for (int w : excluded) {
							for (int u : potential)
								if (!containsEdge(u, w))
									continue excludedLoop;
							skip = true;
							break;
						}
						if (!skip) {
							potentialStack.push(potential = nextPotential);
							excludedStack.push(excluded = nextExcluded);
							continue recursionLoop;
						}
						removeLastInsertedVertex(potential, excluded);
					}

					potentialStack.pop();
					excludedStack.pop();
					if (potentialStack.isEmpty()) {
						hasNextClique = false;
						return;
					}
					potential = potentialStack.top();
					excluded = excludedStack.top();
					removeLastInsertedVertex(potential, excluded);
				}
			}

			private void removeLastInsertedVertex(IntList potential, IntList excluded) {
				int v = currentClique.removeInt(currentClique.size() - 1); /* R.remove(v); */
				int lastPotential = potential.removeInt(potential.size() - 1); /* P.remove(v); */
				assert lastPotential == v;
				excluded.add(v);
			}

		};
	}

}
