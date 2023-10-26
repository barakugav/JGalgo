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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Arrays;
import java.util.List;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class MatchingUnweightedTestUtils extends TestUtils {

	private MatchingUnweightedTestUtils() {}

	static void randGraphs(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(128);
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(32, 32).repeat(64);
		tester.addPhase().withArgs(32, 64).repeat(32);
		tester.addPhase().withArgs(64, 64).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(12);
		tester.addPhase().withArgs(256, 256).repeat(4);
		tester.addPhase().withArgs(256, 512).repeat(4);
		tester.addPhase().withArgs(1000, 2500).repeat(1);
		tester.run((n, m) -> {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			int expeced = calcExpectedMaxMatching(g);
			testAlgo(algo, g, expeced);
		});
	}

	private static void testAlgo(MatchingAlgo algo, Graph g, int expectedMatchSize) {
		Matching match = algo.computeMaximumCardinalityMatching(g);
		validateMatching(g, match);
		assertEquals(expectedMatchSize, match.edges().size(), "unexpected match size");
	}

	static void validateMatching(Graph g, Matching matching) {
		IntSet matched = new IntOpenHashSet();
		for (int e : matching.edges()) {
			for (int v : new int[] { g.edgeSource(e), g.edgeTarget(e) }) {
				if (!matched.add(v))
					fail("Invalid matching, clash: " + v + " " + e);
			}
		}
	}

	/* implementation of general graphs maximum matching from the Internet */

	private static int calcExpectedMaxMatching(Graph g) {
		int n = g.vertices().size();
		@SuppressWarnings("unchecked")
		List<Integer>[] graph = new List[n];
		IndexIdMap vToIdx = g.indexGraphVerticesMap();
		for (int u : g.vertices()) {
			graph[vToIdx.idToIndex(u)] = new ObjectArrayList<>();
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				graph[vToIdx.idToIndex(u)].add(Integer.valueOf(vToIdx.idToIndex(v)));
			}
		}
		return EdmondsMaximumCardinalityMatching.maxMatching(graph);
	}

	private static class EdmondsMaximumCardinalityMatching {
		private static int lca(int[] match, int[] base, int[] p, int a, int b) {
			boolean[] used = new boolean[match.length];
			while (true) {
				a = base[a];
				used[a] = true;
				if (match[a] == -1)
					break;
				a = p[match[a]];
			}
			while (true) {
				b = base[b];
				if (used[b])
					return b;
				b = p[match[b]];
			}
		}

		private static void markPath(int[] match, int[] base, boolean[] blossom, int[] p, int v, int b, int children) {
			for (; base[v] != b; v = p[match[v]]) {
				blossom[base[v]] = blossom[base[match[v]]] = true;
				p[v] = children;
				children = match[v];
			}
		}

		private static int findPath(List<Integer>[] graph, int[] match, int[] p, int root) {
			int n = graph.length;
			boolean[] used = new boolean[n];
			Arrays.fill(p, -1);
			int[] base = new int[n];
			for (int i = 0; i < n; ++i)
				base[i] = i;
			used[root] = true;
			int qh = 0;
			int qt = 0;
			int[] q = new int[n];
			q[qt++] = root;
			while (qh < qt) {
				int v = q[qh++];
				for (int to : graph[v]) {
					if (base[v] == base[to] || match[v] == to)
						continue;
					if (to == root || match[to] != -1 && p[match[to]] != -1) {
						int curbase = lca(match, base, p, v, to);
						boolean[] blossom = new boolean[n];
						markPath(match, base, blossom, p, v, curbase, to);
						markPath(match, base, blossom, p, to, curbase, v);
						for (int i = 0; i < n; ++i)
							if (blossom[base[i]]) {
								base[i] = curbase;
								if (!used[i]) {
									used[i] = true;
									q[qt++] = i;
								}
							}
					} else if (p[to] == -1) {
						p[to] = v;
						if (match[to] == -1)
							return to;
						to = match[to];
						used[to] = true;
						q[qt++] = to;
					}
				}
			}
			return -1;
		}

		public static int maxMatching(List<Integer>[] graph) {
			int n = graph.length;
			int[] match = new int[n];
			Arrays.fill(match, -1);
			int[] p = new int[n];
			for (int i = 0; i < n; ++i) {
				if (match[i] == -1) {
					int v = findPath(graph, match, p, i);
					while (v != -1) {
						int pv = p[v];
						int ppv = match[pv];
						match[v] = pv;
						match[pv] = v;
						v = ppv;
					}
				}
			}
			int matches = 0;
			for (int i = 0; i < n; ++i)
				if (match[i] != -1)
					++matches;
			return matches / 2;
		}
	}

}
