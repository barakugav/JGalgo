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
package com.jgalgo.gen;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CompleteGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		CompleteGraphGenerator<String, Integer> g = CompleteGraphGenerator.newInstance();
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = range(7).mapToObj(String::valueOf).collect(Collectors.toSet());
		g.setVertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		CompleteGraphGenerator<Integer, Integer> g = CompleteGraphGenerator.newIntInstance();
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = range(7);
		g.setVertices(range(7));
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		foreachBoolConfig((intGraph, directed, selfEdges) -> {
			CompleteGraphGenerator<Integer, Integer> g0 =
					intGraph ? CompleteGraphGenerator.newIntInstance() : CompleteGraphGenerator.newInstance();
			final int n = 7;
			g0.setVertices(range(n));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g0.generate());

			g0.setEdges(new AtomicInteger()::getAndIncrement);
			g0.setDirected(directed);
			g0.setSelfEdges(selfEdges);
			Graph<Integer, Integer> g = g0.generate();

			assertEqualsBool(intGraph, g instanceof IntGraph);

			int expectedNumEdges = 0;
			expectedNumEdges += directed ? n * (n - 1) : n * (n - 1) / 2;
			expectedNumEdges += selfEdges ? n : 0;
			assertEquals(range(expectedNumEdges), g.edges());

			Set<Pair<Integer, Integer>> edges = new ObjectOpenHashSet<>();
			for (Integer e : g.edges()) {
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!directed && u.intValue() > v.intValue()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				boolean dupEdge = !edges.add(Pair.of(u, v));
				assertFalse(dupEdge, "duplicate edge: (" + u + ", " + v + ")");
			}
			for (Integer u : range(n)) {
				for (Integer v : range(directed ? 0 : u.intValue(), n)) {
					if (!selfEdges && u.equals(v))
						continue;
					if (directed) {
						assertTrue(edges.contains(Pair.of(u, v)));
					} else {
						assertTrue(edges.contains(Pair.of(u, v)) || edges.contains(Pair.of(v, u)));
					}
				}
			}
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			CompleteGraphGenerator<Integer, Integer> g =
					intGraph ? CompleteGraphGenerator.newIntInstance() : CompleteGraphGenerator.newInstance();
			g.setVertices(IntList.of(1, 9, 3, 4, 5));
			g.setEdges(new AtomicInteger()::incrementAndGet);

			/* check default */
			assertFalse(g.generate().isDirected());

			/* check directed */
			g.setDirected(true);
			assertTrue(g.generate().isDirected());

			/* check undirected */
			g.setDirected(false);
			assertFalse(g.generate().isDirected());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			CompleteGraphGenerator<Integer, Integer> g =
					intGraph ? CompleteGraphGenerator.newIntInstance() : CompleteGraphGenerator.newInstance();
			g.setVertices(IntList.of(1, 9, 3, 4, 5));
			g.setEdges(new AtomicInteger()::incrementAndGet);

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

}