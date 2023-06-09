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

import java.util.Random;
import java.util.Set;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {}

	static Boolean2ObjectFunction<Graph> defaultGraphImpl() {
		return direct -> Graph.newBuilderUndirected().setDirected(direct).build();
	}

	public static class RandomGraphBuilder {

		private final SeedGenerator seedGen;
		private int n;
		private int sn;
		private int tn;
		private int m;
		private boolean bipartite;
		private boolean directed;
		private boolean parallelEdges;
		private boolean selfEdges;
		private boolean cycles;
		private boolean connected;
		private Boolean2ObjectFunction<Graph> impl = defaultGraphImpl();

		public RandomGraphBuilder(long seed) {
			seedGen = new SeedGenerator(seed);
			n = sn = tn = m = 0;
			bipartite = false;
			parallelEdges = false;
			selfEdges = false;
			cycles = false;
			connected = false;
		}

		public RandomGraphBuilder n(int n) {
			this.n = n;
			return this;
		}

		public RandomGraphBuilder sn(int sn) {
			this.sn = sn;
			return this;
		}

		public RandomGraphBuilder tn(int tn) {
			this.tn = tn;
			return this;
		}

		public RandomGraphBuilder m(int m) {
			this.m = m;
			return this;
		}

		public RandomGraphBuilder bipartite(boolean bipartite) {
			this.bipartite = bipartite;
			return this;
		}

		public RandomGraphBuilder directed(boolean directed) {
			this.directed = directed;
			return this;
		}

		public RandomGraphBuilder parallelEdges(boolean parallelEdges) {
			this.parallelEdges = parallelEdges;
			return this;
		}

		public RandomGraphBuilder selfEdges(boolean selfEdges) {
			this.selfEdges = selfEdges;
			return this;
		}

		public RandomGraphBuilder cycles(boolean cycles) {
			this.cycles = cycles;
			return this;
		}

		public RandomGraphBuilder connected(boolean connected) {
			this.connected = connected;
			return this;
		}

		public RandomGraphBuilder graphImpl(Boolean2ObjectFunction<Graph> impl) {
			this.impl = impl;
			return this;
		}

		public Graph build() {
			IntList vertices = new IntArrayList();
			final Graph g;
			Weights.Bool partition = null;
			if (!bipartite) {
				if (n < 0 || m < 0)
					throw new IllegalStateException();
				g = impl.get(directed);
				for (int i = 0; i < n; i++)
					vertices.add(g.addVertex());
			} else {
				if (sn < 0 || tn < 0)
					throw new IllegalStateException();
				if ((sn == 0 || tn == 0) && m != 0)
					throw new IllegalStateException();
				n = sn + tn;
				g = impl.get(directed);
				for (int i = 0; i < n; i++)
					vertices.add(g.addVertex());
				partition = g.addVerticesWeights(Weights.DefaultBipartiteWeightKey, boolean.class);

				IntIterator vit = vertices.iterator();
				for (int u = 0; u < sn; u++)
					partition.set(vit.nextInt(), true);
				for (int u = 0; u < tn; u++)
					partition.set(vit.nextInt(), false);
			}
			if (n == 0)
				return g;
			if (!directed && !cycles && m >= n)
				throw new IllegalArgumentException();
			if (!cycles && selfEdges)
				throw new IllegalArgumentException();
			if (!parallelEdges) {
				long limit;
				if (bipartite)
					limit = n <= 16 ? sn * tn : ((long) sn) * tn * 2 / 3;
				else
					limit = n <= 16 ? (n - 1) * n / 2 : ((long) n) * n / 3;
				if (m > limit)
					throw new IllegalArgumentException("too much edges for random sampling (limit=" + limit + ")");
			}

			Set<IntList> existingEdges = new ObjectOpenHashSet<>();
			UnionFind uf = UnionFind.newBuilder().expectedSize(n).build();
			Weights.Int vertexToUf = Weights.createExternalVerticesWeights(g, int.class, Integer.valueOf(-1));
			for (int v : g.vertices()) {
				int ufIdx = uf.make();
				vertexToUf.set(v, ufIdx);
			}
			int componentsNum = n;
			Random rand = new Random(seedGen.nextSeed());
			Weights.Bool reachableFromRoot = Weights.createExternalVerticesWeights(g, boolean.class);
			reachableFromRoot.set(g.vertices().iterator().nextInt(), true);
			int reachableFromRootCount = 1;
			IntPriorityQueue queue = new IntArrayFIFOQueue();

			int dagRoot = g.vertices().iterator().nextInt();
			IntList dagOrder = new IntArrayList(g.vertices());
			dagOrder.rem(dagRoot);
			IntLists.shuffle(dagOrder, rand);
			dagOrder.add(0, dagRoot);
			Int2IntMap vToDagIdx = new Int2IntOpenHashMap(n);
			for (int i = 0; i < n; i++)
				vToDagIdx.put(dagOrder.getInt(i), i);

			while (true) {
				boolean done = true;
				if (g.edges().size() < m)
					done = false;
				if (connected) {
					if (!directed && componentsNum > 1)
						done = false;
					else if (directed && reachableFromRootCount < n)
						done = false;
				}
				if (done)
					break;

				int u, v;

				if (!bipartite) {
					u = vertices.getInt(rand.nextInt(n));
					v = vertices.getInt(rand.nextInt(n));
					if (directed && !cycles) {
						int uDagIdx = vToDagIdx.get(u);
						int vDagIdx = vToDagIdx.get(v);
						if (uDagIdx > vDagIdx) {
							int temp = u;
							u = v;
							v = temp;
						}
					}
				} else {
					do {
						u = vertices.getInt(rand.nextInt(n));
						v = vertices.getInt(rand.nextInt(n));
					} while (partition.getBool(u) == partition.getBool(v));
				}

				// avoid self edges
				if (!selfEdges && u == v)
					continue;

				// avoid double edges
				if (!parallelEdges) {
					int ut = u, vt = v;
					if (!directed && ut > vt) {
						int temp = ut;
						ut = vt;
						vt = temp;
					}
					if (!existingEdges.add(IntList.of(ut, vt)))
						continue;
				}

				// keep track of number of connectivity components
				if (!cycles || connected) {
					if (!directed) {
						int uComp = uf.find(vertexToUf.getInt(u));
						int vComp = uf.find(vertexToUf.getInt(v));

						// avoid cycles
						if (!cycles && uComp == vComp)
							continue;

						if (uComp != vComp)
							componentsNum--;
						uf.union(uComp, vComp);
					} else if (connected) {
						if (reachableFromRoot.getBool(u) && !reachableFromRoot.getBool(v)) {
							reachableFromRoot.set(v, true);
							reachableFromRootCount++;

							queue.enqueue(v);
							while (!queue.isEmpty()) {
								int p = queue.dequeueInt();

								for (EdgeIter eit = g.outEdges(p).iterator(); eit.hasNext();) {
									eit.nextInt();
									int pv = eit.target();
									if (reachableFromRoot.getBool(pv))
										continue;
									reachableFromRoot.set(pv, true);
									reachableFromRootCount++;
									queue.enqueue(pv);
								}
							}

						}
					}
				}
				g.addEdge(u, v);
			}
			return g;
		}

	}

	public static Graph randTree(int n, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(n - 1).directed(false).selfEdges(false).cycles(false).connected(true)
				.build();
	}

	static Graph randForest(int n, int m, long seed) {
		return new RandomGraphBuilder(seed).n(n).m(m).directed(false).selfEdges(false).cycles(false).connected(false)
				.build();
	}

	static Weights.Double assignRandWeights(Graph g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	static Weights.Double assignRandWeights(Graph g, double minWeight, double maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		Weights.Double weight = g.addEdgesWeights("weight", double.class);
		for (int e : g.edges())
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		return weight;
	}

	public static Weights.Int assignRandWeightsIntPos(Graph g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static Weights.Int assignRandWeightsIntNeg(Graph g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	static Weights.Int assignRandWeightsInt(Graph g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, seed);
		Weights.Int weight = g.addEdgesWeights("weight", int.class);
		for (int e : g.edges())
			weight.set(e, rand.next());
		return weight;
	}

	public static Graph randGraph(int n, int m, long seed) {
		return randGraph(n, m, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	static Graph randGraph(int n, int m, Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		return new RandomGraphBuilder(seed).graphImpl(graphImpl).n(n).m(m).directed(false).parallelEdges(false)
				.selfEdges(false).cycles(true).connected(false).build();
	}

}
