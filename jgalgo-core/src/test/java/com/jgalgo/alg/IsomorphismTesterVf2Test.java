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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class IsomorphismTesterVf2Test extends TestBase {

	@Test
	public void positiveDirected() {
		testPositive(new IsomorphismTesterVf2(), true, false);
	}

	@Test
	public void positiveUndirected() {
		testPositive(new IsomorphismTesterVf2(), false, false);
	}

	private static void testPositive(IsomorphismTester algo, boolean directed, boolean parallelEdges) {
		final Random rand = new Random(0xe382dc68ec73aa85L);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 8).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(16, 18).repeat(128);
		tester.addPhase().withArgs(19, 39).repeat(64);
		tester.addPhase().withArgs(23, 52).repeat(32);
		tester.addPhase().withArgs(23, 26).repeat(32);
		tester.addPhase().withArgs(64, 256).repeat(20);
		tester.addPhase().withArgs(80, 400).repeat(1);
		tester.run((n, m) -> {
			Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> graphs =
					randIsomorphicGraphs(n, m, directed, parallelEdges, rand.nextLong());
			Graph<Integer, Integer> g1 = graphs.left(), g2 = graphs.second();
			g1 = maybeIndexGraph(g1, rand);
			g2 = maybeIndexGraph(g2, rand);
			testPositive(g1, g2, algo, rand.nextLong());
		});
	}

	private static void testPositive(Graph<Integer, Integer> g1, Graph<Integer, Integer> g2, IsomorphismTester algo,
			long seed) {
		final Random rand = new Random(seed);

		/* isIsomorphic() */
		assertTrue(algo.isIsomorphic(g1, g2));

		/* isomorphicMappingsIter() */
		Iterator<IsomorphismTester.Mapping<Integer, Integer, Integer, Integer>> it =
				algo.isomorphicMappingsIter(g1, g2);
		assertTrue(it.hasNext());

		Set<Int2IntMap> seenMappings = new ObjectOpenHashSet<>();
		while (it.hasNext()) {
			IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> m1 = it.next();
			checkMapping(g1, g2, m1, rand);

			/* assert the returned mappings as unique */
			Int2IntMap mapping = new Int2IntOpenHashMap(g1.vertices().size());
			for (Integer v1 : g1.vertices())
				mapping.put(v1.intValue(), m1.mapVertex(v1).intValue());
			boolean added = seenMappings.add(mapping);
			assertTrue(added);
		}

		/* isomorphicMapping() */
		Optional<IsomorphismTester.Mapping<Integer, Integer, Integer, Integer>> mapping =
				algo.isomorphicMapping(g1, g2);
		assertTrue(mapping.isPresent());
		checkMapping(g1, g2, mapping.get(), rand);
	}

	@Test
	public void negativeDifferentDegrees() {
		IsomorphismTester algo = new IsomorphismTesterVf2();
		final Random rand = new Random(0x94db38eea7e00f94L);
		foreachBoolConfig(directed -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(4, 8).repeat(128);
			tester.addPhase().withArgs(16, 32).repeat(128);
			tester.addPhase().withArgs(16, 18).repeat(128);
			tester.addPhase().withArgs(19, 39).repeat(64);
			tester.addPhase().withArgs(23, 52).repeat(32);
			tester.run((n, m) -> {
				Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> graphs =
						randIsomorphicGraphs(n, m, directed, false, rand.nextLong());
				Graph<Integer, Integer> g1 = graphs.left(), g2 = graphs.second();

				Integer e = Graphs.randEdge(g2, rand);
				Integer uOld = g2.edgeSource(e), vOld = g2.edgeTarget(e);
				g2.removeEdge(e);
				Integer uNew, vNew;
				for (int repeat = 0; repeat < 100; repeat++) {
					uNew = Graphs.randVertex(g2, rand);
					vNew = Graphs.randVertex(g2, rand);
					if (g2.containsEdge(uNew, vNew))
						continue;
					if (g2.isDirected()) {
						if (g2.outEdges(uOld).size() == g2.outEdges(uNew).size())
							continue;
						if (g2.inEdges(vOld).size() == g2.inEdges(vNew).size())
							continue;
					} else {
						int uOldDeg = g2.outEdges(uOld).size();
						int vOldDeg = g2.outEdges(vOld).size();
						int uNewDeg = g2.outEdges(uNew).size();
						int vNewDeg = g2.outEdges(vNew).size();
						if (uOldDeg == uNewDeg && vOldDeg == vNewDeg)
							continue;
						if (uOldDeg == vNewDeg && vOldDeg == uNewDeg)
							continue;
					}
					break;
				}

				g1 = maybeIndexGraph(g1, rand);
				g2 = maybeIndexGraph(g2, rand);
				assertFalse(algo.isIsomorphic(g1, g2));
			});
		});
	}

	@Test
	public void noVertices() {
		final Random rand = new Random(0xd8b45de953dcc4eaL);
		foreachBoolConfig(directed -> {
			IsomorphismTester algo = new IsomorphismTesterVf2();
			IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			assertTrue(algo.isIsomorphic(g1, g2));

			List<IsomorphismTester.Mapping<Integer, Integer, Integer, Integer>> mappings =
					new ObjectArrayList<>(algo.isomorphicMappingsIter(g1, g2));
			assertEquals(1, mappings.size());
			IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> mapping = mappings.get(0);
			checkMapping(g1, g2, mapping, rand);
		});
	}

	@Test
	public void noEdges() {
		final Random rand = new Random(0x9b512bfff33abb78L);
		foreachBoolConfig(directed -> {
			for (final int n : IntList.of(1, 2, 3, 4, 7)) {
				IsomorphismTester algo = new IsomorphismTesterVf2();
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertices(range(n));
				g2.addVertices(range(n));

				Set<Int2IntMap> seenMappings = new ObjectOpenHashSet<>();
				Iterator<IsomorphismTester.Mapping<Integer, Integer, Integer, Integer>> mappings =
						algo.isomorphicMappingsIter(g1, g2);
				while (mappings.hasNext()) {
					IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> m1 = mappings.next();
					checkMapping(g1, g2, m1, rand);

					/* assert the returned mappings as unique */
					Int2IntMap mapping = new Int2IntOpenHashMap(g1.vertices().size());
					for (Integer v1 : g1.vertices())
						mapping.put(v1.intValue(), m1.mapVertex(v1).intValue());
					boolean added = seenMappings.add(mapping);
					assertTrue(added);
				}

				/* we expected \(n!\) mappings, all permutations of the vertices */
				int expectedMappingsNum = 1;
				for (int i = 1; i <= n; i++)
					expectedMappingsNum *= i;
				assertEquals(expectedMappingsNum, seenMappings.size());
			}
		});
	}

	@Test
	public void negativeDifferentVerticesNum() {
		foreachBoolConfig(directed -> {
			IsomorphismTester algo = new IsomorphismTesterVf2();
			IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			g1.addVertexInt();
			assertFalse(algo.isIsomorphic(g1, g2));
		});
	}

	@Test
	public void negativeDifferentEdgesNum() {
		foreachBoolConfig(directed -> {
			IsomorphismTester algo = new IsomorphismTesterVf2();
			IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			g1.addVertex(0);
			g1.addVertex(1);
			g1.addEdge(0, 1, 0);
			IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			g2.addVertex(0);
			g2.addVertex(1);
			assertFalse(algo.isIsomorphic(g1, g2));
		});
	}

	@Test
	public void negativeDifferentDirectedUndirected() {
		IsomorphismTester algo = new IsomorphismTesterVf2();
		IntGraph g1 = IntGraph.newDirected();
		IntGraph g2 = IntGraph.newUndirected();
		assertFalse(algo.isIsomorphic(g1, g2));
		assertFalse(algo.isIsomorphic(g2, g1));
	}

	private static void checkMapping(Graph<Integer, Integer> g1, Graph<Integer, Integer> g2,
			IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> m1, Random rand) {
		IsomorphismTester.Mapping<Integer, Integer, Integer, Integer> m2 = m1.inverse();
		assertTrue(m1.inverse() == m2, "inverse matching was not cached");

		/* assert the vertex mapping is a bijective function */
		for (Integer v1 : g1.vertices()) {
			Integer v2 = m1.mapVertex(v1);
			assertTrue(g2.vertices().contains(v2));
			assertEquals(v1, m2.mapVertex(v2));
		}
		for (Integer v2 : g2.vertices()) {
			Integer v1 = m2.mapVertex(v2);
			assertTrue(g1.vertices().contains(v1));
			assertEquals(v2, m1.mapVertex(v1));
		}
		assertThrows(NoSuchVertexException.class, () -> {
			Integer nonExistingVertex;
			do {
				nonExistingVertex = Integer.valueOf(rand.nextInt());
			} while (g1.vertices().contains(nonExistingVertex));
			m1.mapVertex(nonExistingVertex);
		});
		assertThrows(NoSuchVertexException.class, () -> {
			Integer nonExistingVertex;
			do {
				nonExistingVertex = Integer.valueOf(rand.nextInt());
			} while (g2.vertices().contains(nonExistingVertex));
			m2.mapVertex(nonExistingVertex);
		});

		/* assert the edge mapping is a bijective function */
		for (Integer e1 : g1.edges()) {
			Integer e2 = m1.mapEdge(e1);
			assertTrue(g2.edges().contains(e2));
			assertEquals(e1, m2.mapEdge(e2));
		}
		for (Integer e2 : g2.edges()) {
			Integer e1 = m2.mapEdge(e2);
			assertTrue(g1.edges().contains(e1));
			assertEquals(e2, m1.mapEdge(e1));
		}
		assertThrows(NoSuchEdgeException.class, () -> {
			Integer nonExistingEdge;
			do {
				nonExistingEdge = Integer.valueOf(rand.nextInt());
			} while (g1.edges().contains(nonExistingEdge));
			m1.mapEdge(nonExistingEdge);
		});
		assertThrows(NoSuchEdgeException.class, () -> {
			Integer nonExistingEdge;
			do {
				nonExistingEdge = Integer.valueOf(rand.nextInt());
			} while (g2.edges().contains(nonExistingEdge));
			m2.mapEdge(nonExistingEdge);
		});

		/* assert the mapping is valid */
		for (Integer e1 : g1.edges()) {
			Integer u1 = g1.edgeSource(e1), v1 = g1.edgeTarget(e1);
			Integer u2 = m1.mapVertex(u1), v2 = m1.mapVertex(v1);
			Integer e2 = m1.mapEdge(e1);
			Integer u2Expected = g2.edgeSource(e2);
			Integer v2Expected = g2.edgeTarget(e2);
			if (g1.isDirected()) {
				assertEquals(u2Expected, u2);
				assertEquals(v2Expected, v2);
			} else {
				assertTrue((u2Expected.equals(u2) && v2Expected.equals(v2))
						|| (u2Expected.equals(v2) && v2Expected.equals(u2)));
			}
		}
	}

	@SuppressWarnings("boxing")
	private static Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> randIsomorphicGraphs(int n, int m,
			boolean directed, boolean parallelEdges, long seed) {
		Random rand = new Random(seed);
		Graph<Integer, Integer> g1 = GraphsTestUtils.randGraph(n, m, directed, true, parallelEdges, rand.nextLong());

		/* use copy() and clear() to ensure g2 is IntGraph if g1 was */
		Graph<Integer, Integer> g2 = g1.copy();
		g2.clear();

		/* add n vertices to g2 */
		while (g2.vertices().size() < g1.vertices().size()) {
			int v = rand.nextInt(g1.vertices().size() * 2);
			if (!g2.vertices().contains(v))
				g2.addVertex(v);
		}
		Map<Integer, Integer> vMapping = randMapping(g1.vertices(), g2.vertices(), rand.nextLong());

		/* add all edges to g2 */
		List<Integer> g1Edges = new ArrayList<>(g1.edges());
		Collections.shuffle(g1Edges, rand);
		for (Integer e1 : g1Edges) {
			Integer u1 = g1.edgeSource(e1), v1 = g1.edgeTarget(e1);
			Integer u2 = vMapping.get(u1), v2 = vMapping.get(v1);
			Integer e2;
			do {
				e2 = rand.nextInt(g1.edges().size() * 2);
			} while (g2.edges().contains(e2));
			g2.addEdge(u2, v2, e2);
		}

		return Pair.of(g1, g2);
	}

	private static <A, B> Map<A, B> randMapping(Set<A> a, Set<B> b, long seed) {
		List<A> aList = new ArrayList<>(a);
		List<B> bList = new ArrayList<>(b);
		assert aList.size() == bList.size();
		int[] perm = randPermutation(aList.size(), seed);
		Map<A, B> map = new HashMap<>();
		for (int i = 0; i < perm.length; i++)
			map.put(aList.get(i), bList.get(perm[i]));
		return map;
	}

	@Test
	public void defaultImpl() {
		IsomorphismTester defAlgo = IsomorphismTester.newInstance();
		assertEquals(defAlgo.getClass(), IsomorphismTesterVf2.class);
	}

}