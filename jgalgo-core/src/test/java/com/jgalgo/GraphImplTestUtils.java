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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

class GraphImplTestUtils extends TestUtils {

	static IntSet intSetOf(int... elms) {
		IntSet set = new IntOpenHashSet();
		for (int e : elms)
			set.add(e);
		return IntSets.unmodifiable(set);
	}

	static void testVertexAdd(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.setDirected(directed).build();
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int i = 0; i < n; i++) {
				int v = g.addVertex();
				verticesSet.add(v);
			}
			assertEquals(verticesSet, g.vertices());
			assertEquals(IntSets.emptySet(), g.edges());
		}
	}

	static void testAddEdge(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.setDirected(directed).build();
			for (int i = 0; i < n; i++)
				g.addVertex();

			Int2ObjectMap<int[]> edges = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = u + 1; v < n; v++) {
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					edges.put(e, new int[] { e, u, v });
				}
			}
			assertEquals(edges.keySet(), g.edges());
			for (int[] edge : edges.values()) {
				int e = edge[0], u = edge[1], v = edge[2];
				assertEndpoints(g, e, u, v);
			}
		}
	}

	private static void assertEndpoints(Graph g, int e, int source, int target) {
		if (g.getCapabilities().directed()) {
			assertEquals(source, g.edgeSource(e));
			assertEquals(target, g.edgeTarget(e));
		} else {
			assertEquals(intSetOf(source, target), intSetOf(g.edgeSource(e), g.edgeTarget(e)));
		}
		assertEquals(source, g.edgeEndpoint(e, target));
		assertEquals(target, g.edgeEndpoint(e, source));
	}

	static void testGetEdge(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.setDirected(directed).build();
			for (int i = 0; i < n; i++)
				g.addVertex();

			Object2IntMap<IntCollection> edges = new Object2IntOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(IntList.of(u, v), e);
					} else {
						edges.put(intSetOf(u, v), e);
					}
				}
			}
			for (Object2IntMap.Entry<IntCollection> edge : edges.object2IntEntrySet()) {
				IntCollection endpoints = edge.getKey();
				IntIterator endpointsIt = endpoints.iterator();
				int u = endpointsIt.nextInt(), v = endpointsIt.hasNext() ? endpointsIt.nextInt() : u;
				int e = edge.getIntValue();
				assertEquals(e, g.getEdge(u, v));
			}
		}

	}

	static void testGetEdges(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.setDirected(directed).build();
			for (int i = 0; i < n; i++)
				g.addVertex();

			Int2ObjectMap<IntSet> edgesOut = new Int2ObjectOpenHashMap<>();
			Int2ObjectMap<IntSet> edgesIn = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					if (directed) {
						edgesOut.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						edgesIn.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					} else {
						edgesOut.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						edgesOut.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					}
				}
			}
			for (int u = 0; u < n; u++) {
				assertEquals(edgesOut.get(u), new IntOpenHashSet(g.edgesOut(u)));
				assertEquals(edgesOut.get(u), g.edgesOut(u));
			}
			if (directed) {
				for (int u = 0; u < n; u++) {
					for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						assertEquals(u, eit.source());
						assertEquals(g.edgeEndpoint(e, u), eit.target());
					}
					assertEquals(edgesOut.get(u), new IntOpenHashSet(g.edgesOut(u)));
					assertEquals(edgesOut.get(u), g.edgesOut(u));
				}
				for (int v = 0; v < n; v++) {
					IntSet vEdges = new IntOpenHashSet();
					for (EdgeIter eit = g.edgesIn(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						assertEquals(v, eit.target());
						assertEquals(g.edgeEndpoint(e, v), eit.source());
						vEdges.add(e);
					}
					assertEquals(edgesIn.get(v), vEdges);
				}
			}
		}

	}

	static void testEdgeIter(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.setDirected(directed).build();
			for (int i = 0; i < n; i++)
				g.addVertex();

			Int2ObjectMap<IntCollection> edges = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(e, IntList.of(u, v));
					} else {
						edges.put(e, intSetOf(u, v));
					}
				}
			}
			for (int u = 0; u < n; u++) {
				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					if (directed) {
						assertEquals(edges.get(e), IntList.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), intSetOf(eit.source(), eit.target()));
					}
					assertEquals(u, eit.source());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}
			}
			for (int v = 0; v < n; v++) {
				for (EdgeIter eit = g.edgesIn(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int u = eit.source();
					if (directed) {
						assertEquals(edges.get(e), IntList.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), intSetOf(eit.source(), eit.target()));
					}
					assertEquals(v, eit.target());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}
			}
		}
	}

	static void testDegree(Graph.Builder graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.setDirected(directed).build();
			for (int i = 0; i < n; i++)
				g.addVertex();

			Int2IntMap degreeOut = new Int2IntOpenHashMap();
			Int2IntMap degreeIn = new Int2IntOpenHashMap();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					g.addEdge(u, v);

					degreeOut.put(u, degreeOut.get(u) + 1);
					degreeIn.put(v, degreeIn.get(v) + 1);
					if (!directed && u != v) {
						degreeOut.put(v, degreeOut.get(v) + 1);
						degreeIn.put(u, degreeIn.get(u) + 1);
					}
				}
			}
			for (int u = 0; u < n; u++) {
				assertEquals(degreeOut.get(u), g.edgesOut(u).size(), "u=" + u);
				assertEquals(degreeIn.get(u), g.edgesIn(u).size(), "u=" + u);
			}
		}
	}

	static void testClear(Graph.Builder graphImpl, long seed) {
		Random rand = new Random(seed);
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.setDirected(directed).build();
			boolean parallelEdges = g.getCapabilities().parallelEdges();

			int totalOpNum = 1000;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedN = 0;
				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex();
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex();
						expectedN++;
					} else {
						int u, v;
						for (int retry = 20;;) {
							if (retry-- > 0)
								continue opsLoop;
							u = rand.nextInt(g.vertices().size());
							v = rand.nextInt(g.vertices().size());
							if (u == v)
								continue;
							if (!parallelEdges && g.getEdge(u, v) != -1)
								continue;
							break;
						}
						g.addEdge(u, v);
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clear();
				assertEquals(0, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		}
	}

	static void testClearEdges(Graph.Builder graphImpl, long seed) {
		Random rand = new Random(seed);
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.setDirected(directed).build();
			boolean parallelEdges = g.getCapabilities().parallelEdges();

			int totalOpNum = 1000;
			int expectedN = 0;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex();
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex();
						expectedN++;
					} else {
						int u, v;
						for (int retry = 20;;) {
							if (retry-- > 0)
								continue opsLoop;
							u = rand.nextInt(g.vertices().size());
							v = rand.nextInt(g.vertices().size());
							if (u == v)
								continue;
							if (!parallelEdges && g.getEdge(u, v) != -1)
								continue;
							break;
						}
						g.addEdge(u, v);
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clearEdges();
				assertEquals(expectedN, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		}
	}

	static void testCopy(Graph.Builder graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean directed : new boolean[] { true, false }) {
			/* Create a random graph g */
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(300).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();

			/* assign some weights to the vertices of g */
			final Object gVDataKey = new Utils.Obj("vData");
			Weights<Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Int2ObjectMap<Object> gVDataMap = new Int2ObjectOpenHashMap<>();
			for (int u : g.vertices()) {
				Object data = new Utils.Obj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final Object gEDataKey = new Utils.Obj("eData");
			Weights<Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Int2ObjectMap<Object> gEDataMap = new Int2ObjectOpenHashMap<>();
			for (int e : g.edges()) {
				Object data = new Utils.Obj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph copy = g.copy();

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (int u : g.vertices()) {
				assertEquals(g.edgesOut(u), copy.edgesOut(u));
				assertEquals(g.edgesIn(u), copy.edgesIn(u));
			}

			/* Assert weights were copied */
			Weights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
			Weights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNotNull(copyVData);
			assertNotNull(copyEData);
			Int2ObjectMap<Object> copyVDataMap = new Int2ObjectOpenHashMap<>(gVDataMap);
			Int2ObjectMap<Object> copyEDataMap = new Int2ObjectOpenHashMap<>(gEDataMap);
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}

			/* Reassign some weights to both g and copy, and assert they are updated independently */
			for (int ops = 0; ops < g.vertices().size() / 4; ops++) {
				int u = rand.nextInt(g.vertices().size());
				Object data = new Utils.Obj("data" + u + "new");
				g.getVerticesWeights(gVDataKey).set(u, data);
				gVDataMap.put(u, data);
			}
			for (int ops = 0; ops < copy.vertices().size() / 4; ops++) {
				int u = rand.nextInt(copy.vertices().size());
				Object data = new Utils.Obj("data" + u + "new");
				copy.getVerticesWeights(gVDataKey).set(u, data);
				copyVDataMap.put(u, data);
			}
			int[] gEdges = g.edges().toIntArray();
			for (int ops = 0; ops < g.edges().size() / 4; ops++) {
				int e = gEdges[rand.nextInt(g.edges().size())];
				Object data = new Utils.Obj("data" + e + "new");
				g.getEdgesWeights(gEDataKey).set(e, data);
				gEDataMap.put(e, data);
			}
			int[] copyEdges = copy.edges().toIntArray();
			for (int ops = 0; ops < copy.edges().size() / 4; ops++) {
				int e = copyEdges[rand.nextInt(copy.edges().size())];
				Object data = new Utils.Obj("data" + e + "new");
				copy.getEdgesWeights(gEDataKey).set(e, data);
				copyEDataMap.put(e, data);
			}

			/* Assert the weights were updated independently */
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}
		}
	}

	static void testUndirectedMST(Graph.Builder graphImpl, long seed) {
		MinimumSpanningTreeTestUtils.testRandGraph(new MinimumSpanningTreeKruskal(), graphImpl, seed);
	}

	static void testDirectedMDST(Graph.Builder graphImpl, long seed) {
		MinimumDirectedSpanningTreeTarjanTest.testRandGraph(new MinimumDirectedSpanningTreeTarjan(), graphImpl, seed);
	}

	static void testDirectedMaxFlow(Graph.Builder graphImpl, long seed) {
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowEdmondsKarp(), graphImpl, seed, /* directed= */ true);
	}

	static void testUndirectedBipartiteMatching(Graph.Builder graphImpl, long seed) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MaximumMatchingCardinalityGabow1976(), graphImpl, seed);
	}

	static void testUndirectedBipartiteMatchingWeighted(Graph.Builder graphImpl, long seed) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MaximumMatchingWeightedBipartiteHungarianMethod(),
				graphImpl, seed);
	}

	static void testRandOps(Graph.Builder graphImpl, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(2056, 6, 6), phase(32, 16, 16), phase(32, 16, 32), phase(16, 64, 64),
				phase(16, 64, 128), phase(4, 512, 512), phase(2, 512, 1324), phase(1, 1025, 2016));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
			final int opsNum = 128;
			testRandOps(g, opsNum, seedGen.nextSeed());
		});
	}

	private static class RandWeighted<E> {
		private final List<E> elms = new ArrayList<>();
		private final IntList weights = new IntArrayList();
		private int totalWeight;

		void add(E elm, int weight) {
			if (weight <= 0)
				throw new IllegalArgumentException();
			elms.add(elm);
			weights.add(weight);
			totalWeight += weight;
		}

		E get(Random rand) {
			final int v = rand.nextInt(totalWeight);
			int s = 0;
			for (int i = 0; i < elms.size(); i++) {
				s += weights.getInt(i);
				if (v < s)
					return elms.get(i);
			}
			throw new IllegalStateException();
		}
	}

	private static class GraphTracker {
		private final List<Vertex> vertices = new ArrayList<>();
		private final List<Edge> edges = new ArrayList<>();
		private final boolean directed;
		private final Object dataKey;
		private final boolean debugPrints = false;

		GraphTracker(Graph g, Object dataKey) {
			this.directed = g.getCapabilities().directed();
			this.dataKey = dataKey;

			g.getVerticesIDStrategy().addIDSwapListener((id1, id2) -> {
				Vertex v1 = getVertex(id1), v2 = getVertex(id2);
				v1.id = id2;
				v2.id = id1;
				vertices.set(id1, v2);
				vertices.set(id2, v1);
			});

			if (debugPrints)
				System.out.println("\n\n*****");
		}

		int verticesNum() {
			return vertices.size();
		}

		int edgesNum() {
			return edges.size();
		}

		void addVertex(int v) {
			if (debugPrints)
				System.out.println("newVertex(" + v + ")");
			vertices.add(new Vertex(v));
		}

		void removeVertex(Vertex v) {
			removeEdgesOf(v);
			if (debugPrints)
				System.out.println("removeVertex(" + v + ")");

			boolean removed = vertices.remove(v);
			assertTrue(removed);
		}

		Vertex getVertex(int id) {
			Vertex v = vertices.get(id);
			assertEquals(v.id, id);
			assert v.id == id;
			return v;
		}

		Vertex getRandVertex(Random rand) {
			return vertices.get(rand.nextInt(vertices.size()));
		}

		void addEdge(Vertex u, Vertex v, int data) {
			if (debugPrints)
				System.out.println("addEdge(" + u + ", " + v + ", " + data + ")");
			edges.add(new Edge(u, v, data));
		}

		Edge getEdge(int data) {
			for (Edge edge : edges)
				if (edge.data == data)
					return edge;
			fail("edge not found");
			return null;
		}

		Edge getEdge(Vertex u, Vertex v) {
			if (directed) {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if (e.u == u && e.v == v)
						return e;
				}
			} else {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if ((e.u == u && e.v == v) || (e.v == u && e.u == v))
						return e;
				}
			}
			return null;
		}

		void removeEdge(Edge edge) {
			if (debugPrints)
				System.out.println("removeEdge(" + edge.u + ", " + edge.v + ")");
			boolean removed = edges.remove(edge);
			assertTrue(removed);
		}

		void removeEdgesOf(Vertex u) {
			if (debugPrints)
				System.out.println("removeEdgesOf(" + u + ")");
			edges.removeIf(edge -> edge.u == u || edge.v == u);
		}

		void removeEdgesOutOf(Vertex u) {
			if (debugPrints)
				System.out.println("removeEdgesOutOf(" + u + ")");
			edges.removeIf(edge -> edge.u == u);
		}

		void removeEdgesInOf(Vertex v) {
			if (debugPrints)
				System.out.println("removeEdgesInOf(" + v + ")");
			edges.removeIf(edge -> edge.v == v);
		}

		void reverseEdge(Edge edge) {
			if (debugPrints)
				System.out.println("reverse(" + edge.u + ", " + edge.v + ")");
			Vertex temp = edge.u;
			edge.u = edge.v;
			edge.v = temp;
		}

		Edge getRandEdge(Random rand) {
			return edges.get(rand.nextInt(edges.size()));
		}

		@SuppressWarnings("unused")
		void clearEdges() {
			if (debugPrints)
				System.out.println("clearEdges()");
			edges.clear();
		}

		void checkEdgesEqual(Graph g) {
			assertEquals(edgesNum(), g.edges().size());
			Weights.Int edgeData = g.getEdgesWeights(dataKey);

			List<IntList> actual = new ArrayList<>();
			List<IntList> expected = new ArrayList<>();

			for (int e : g.edges()) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edgeData.getInt(e);
				actual.add(IntList.of(u, v, data));
			}

			for (Edge edge : edges) {
				int u = edge.u.id, v = edge.v.id;
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edge.data;
				expected.add(IntList.of(u, v, data));
			}

			Comparator<IntList> cmp = (e1, e2) -> {
				int u1 = e1.getInt(0), v1 = e1.getInt(1), d1 = e1.getInt(2);
				int u2 = e2.getInt(0), v2 = e2.getInt(1), d2 = e2.getInt(2);
				int c;
				if ((c = Integer.compare(u1, u2)) != 0)
					return c;
				if ((c = Integer.compare(v1, v2)) != 0)
					return c;
				if ((c = Integer.compare(d1, d2)) != 0)
					return c;
				return 0;
			};
			actual.sort(cmp);
			expected.sort(cmp);
			assertEquals(expected, actual);
		}

		private static class Vertex {
			int id;

			Vertex(int id) {
				this.id = id;
			}

			@Override
			public String toString() {
				return Integer.toString(id);
			}
		}

		private static class Edge {
			Vertex u, v;
			final int data;

			Edge(Vertex u, Vertex v, int data) {
				this.u = u;
				this.v = v;
				this.data = data;
			}

			@Override
			public String toString() {
				return "(" + u + ", " + v + ", " + data + ")";
			}
		}
	}

	private static enum GraphOp {
		GetEdge, GetVertexEdges, GetVertexEdgesOut, GetVertexEdgesIn,

		EdgeSource, EdgeTarget,

		Degree, DegreeIn, DegreeOut,

		AddEdge, RemoveEdge, RemoveEdgeUsingOutIter, RemoveEdgeUsingInIter,
		// RemoveEdgesMulti,
		RemoveEdgesOfVertex, RemoveEdgesOfVertexUsingIter, RemoveEdgesInOfVertex, RemoveEdgesInOfVertexUsingIter, RemoveEdgesOutOfVertex, RemoveEdgesOutOfVertexUsingIter, ReverseEdge,

		// ClearEdges,

		AddVertex, RemoveVertex,
		// RemoveVertices,
	}

	private static class UniqueGenerator {
		private final Random rand;
		private final IntSet used;

		UniqueGenerator(long seed) {
			rand = new Random(seed);
			used = new IntOpenHashSet();
		}

		int next() {
			for (;;) {
				int x = rand.nextInt();
				if (!used.contains(x)) {
					used.add(x);
					return x;
				}
			}
		}
	}

	private static void testRandOps(Graph g, int opsNum, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		GraphCapabilities capabilities = g.getCapabilities();
		Random rand = new Random(seedGen.nextSeed());
		RandWeighted<GraphOp> opRand = new RandWeighted<>();
		if (capabilities.edgeAdd())
			opRand.add(GraphOp.AddEdge, 80);
		if (capabilities.edgeRemove()) {
			opRand.add(GraphOp.RemoveEdge, 4);
			opRand.add(GraphOp.RemoveEdgeUsingOutIter, 4);
			opRand.add(GraphOp.RemoveEdgeUsingInIter, 4);
			// opRand.add(GraphOp.RemoveEdgesMulti, 1);
			opRand.add(GraphOp.RemoveEdgesOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesOfVertexUsingIter, 1);
			// opRand.add(GraphOp.ClearEdges, 1);
		}
		if (capabilities.edgeRemove() && capabilities.directed()) {
			opRand.add(GraphOp.RemoveEdgesInOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesInOfVertexUsingIter, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertexUsingIter, 1);
		}
		if (capabilities.directed())
			opRand.add(GraphOp.ReverseEdge, 6);
		if (capabilities.vertexAdd())
			opRand.add(GraphOp.AddVertex, 20);
		if (capabilities.vertexRemove()) {
			if (!capabilities.edgeRemove())
				throw new IllegalArgumentException("vertex removal can't be supported while edge removal is not");
			opRand.add(GraphOp.RemoveVertex, 3);
			// opRand.add(GraphOp.RemoveVertices, 1);
		}

		final Object dataKey = new Utils.Obj("data");
		Weights.Int edgeData = g.addEdgesWeights(dataKey, int.class);
		UniqueGenerator dataGen = new UniqueGenerator(seedGen.nextSeed());

		GraphTracker tracker = new GraphTracker(g, dataKey);
		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			// final int data = dataGen.next();
			// edgeData.set(e, data);
			tracker.addVertex(v);
		}
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			final int data = dataGen.next();
			edgeData.set(e, data);
			tracker.addEdge(tracker.getVertex(u), tracker.getVertex(v), data);
		}

		ToIntFunction<GraphTracker.Edge> getEdge = edge -> {
			int e = -1;
			for (EdgeIter eit = g.getEdges(edge.u.id, edge.v.id).iterator(); eit.hasNext();) {
				int e0 = eit.nextInt();
				if (edge.data == edgeData.getInt(e0)) {
					e = e0;
					break;
				}
			}
			assertTrue(e != -1, "edge not found");
			return e;
		};

		opLoop: for (; opsNum > 0;) {
			final GraphOp op = opRand.get(rand);
			switch (op) {
				case AddEdge: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u, v;
					for (int retry = 20;; retry--) {
						if (retry <= 0)
							continue opLoop;

						u = tracker.getRandVertex(rand);
						v = tracker.getRandVertex(rand);
						if (!capabilities.selfEdges() && u == v)
							continue;
						if (!capabilities.parallelEdges() && tracker.getEdge(u, v) != null)
							continue;
						break;
					}

					final int data = dataGen.next();
					int e = g.addEdge(u.id, v.id);
					edgeData.set(e, data);
					tracker.addEdge(u, v, data);
					break;
				}
				case RemoveEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					int e = getEdge.applyAsInt(edge);

					g.removeEdge(e);
					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingOutIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex source = edge.u;

					Set<GraphTracker.Edge> iterationExpected = new HashSet<>();
					for (int eOther : g.edgesOut(source.id)) {
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new HashSet<>();
					for (EdgeIter it = g.edgesOut(source.id).iterator(); it.hasNext();) {
						int eOther = it.nextInt();
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationActual.add(edgeOther);
							assertFalse(duplication);
						} else {
							assertFalse(removed);
							it.remove();
							tracker.removeEdge(edge);
							removed = true;
						}
					}
					assertTrue(removed);
					assertEquals(iterationExpected, iterationActual);
					break;
				}
				case RemoveEdgeUsingInIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex target = edge.v;

					Set<GraphTracker.Edge> iterationExpected = new HashSet<>();
					for (int eOther : g.edgesIn(target.id)) {
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new HashSet<>();
					for (EdgeIter it = g.edgesIn(target.id).iterator(); it.hasNext();) {
						int eOther = it.nextInt();
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationActual.add(edgeOther);
							assertFalse(duplication);
						} else {
							assertFalse(removed);
							it.remove();
							tracker.removeEdge(edge);
							removed = true;
						}
					}
					assertTrue(removed);
					assertEquals(iterationExpected, iterationActual);
					break;
				}
				// case RemoveEdgesMulti: {
				// if (tracker.edgesNum() < 3)
				// continue;
				// Set<GraphTracker.Edge> edges = new HashSet<>(3);
				// while (edges.size() < 3)
				// edges.add(tracker.getRandEdge(rand));
				// IntSet edgesInt = new IntOpenHashSet(3);
				// for (GraphTracker.Edge edge : edges)
				// edgesInt.add(getEdge.applyAsInt(edge));
				// g.removeEdges(edgesInt);
				// for (GraphTracker.Edge edge : edges)
				// tracker.removeEdge(edge);
				// break;
				// }
				case RemoveEdgesOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeEdgesOf(u.id);
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.edgesOut(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					for (EdgeIter it = g.edgesIn(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeEdgesInOf(u.id);
					tracker.removeEdgesInOf(u);
					break;
				}
				case RemoveEdgesInOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.edgesIn(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeEdgesInOf(u);
					break;
				}
				case RemoveEdgesOutOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeEdgesOutOf(u.id);
					tracker.removeEdgesOutOf(u);
					break;
				}
				case RemoveEdgesOutOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.edgesOut(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeEdgesOutOf(u);
					break;
				}
				case ReverseEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					if (edge.u != edge.v && g.getEdge(edge.v.id, edge.u.id) != -1 && !capabilities.parallelEdges())
						continue;
					int e = getEdge.applyAsInt(edge);

					g.reverseEdge(e);
					tracker.reverseEdge(edge);
					break;
				}
				// case ClearEdges:
				// if (g.edges().size() == 0)
				// continue;
				// g.clearEdges();
				// tracker.clearEdges();
				// break;

				case AddVertex: {
					int v = g.addVertex();
					tracker.addVertex(v);
					break;
				}
				case RemoveVertex:
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex v = tracker.getRandVertex(rand);
					g.removeVertex(v.id);
					tracker.removeVertex(v);
					break;
				// case RemoveVertices: {
				// if (tracker.verticesNum() < 3)
				// continue;
				// Set<GraphTracker.Vertex> vertices = new HashSet<>(3);
				// while (vertices.size() < 3)
				// vertices.add(tracker.getRandVertex(rand));
				// IntSet verticesInt = new IntOpenHashSet(3);
				// for (GraphTracker.Vertex vertex : vertices)
				// verticesInt.add(vertex.id);
				// g.removeVertices(verticesInt);
				// for (GraphTracker.Vertex vertex : vertices)
				// tracker.removeVertex(vertex);
				// break;
				// }

				default:
					throw new IllegalArgumentException("Unexpected value: " + op);
			}

			assertEquals(tracker.verticesNum(), g.vertices().size());
			assertEquals(tracker.edgesNum(), g.edges().size());
			if (opsNum % 10 == 0)
				tracker.checkEdgesEqual(g);

			opsNum--;
		}
	}

}
