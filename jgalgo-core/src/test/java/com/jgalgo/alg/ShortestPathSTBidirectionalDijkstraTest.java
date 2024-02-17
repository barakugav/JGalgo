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
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSTBidirectionalDijkstraTest extends TestBase {

	private static ShortestPathSingleSource sssp() {
		return ShortestPathSTTestUtils.ssspFromSpst(new ShortestPathSTBidirectionalDijkstra());
	}

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0x6cc179f14ce846ebL;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(sssp(), true, seed);
	}

	@Test
	public void testSSSPUndirectedPositive() {
		final long seed = 0xc1b8a406eeebf0b3L;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(sssp(), false, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0xa8f5fd9715bf8077L;
		ShortestPathSingleSourceTestUtils.testSSSPDirectedPositiveInt(sssp(), seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0xbebff4437c47bf83L;
		ShortestPathSingleSourceTestUtils.testSSSPUndirectedPositiveInt(sssp(), seed);
	}

	@Test
	public void testSSSPUndirectedCardinality() {
		final long seed = 0x306bceca7951ff3bL;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(sssp(), false, seed);
	}

	@Test
	public void testSSSPDirectedCardinality() {
		final long seed = 0xf0938b03455c55aeL;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(sssp(), true, seed);
	}

	@SuppressWarnings("boxing")
	@Test
	public void simpleSmallGraph() {
		IntGraph g = IntGraph.newDirected();
		g.addVertices(range(3));
		g.addEdge(0, 1, 10);
		g.addEdge(1, 2, 11);

		ShortestPathST algo = new ShortestPathSTBidirectionalDijkstra();
		assertEquals(Fastutil.list(10, 11), algo.computeShortestPath(g, null, 0, 2).edges());
		assertEquals(Fastutil.list(10, 11), algo.computeShortestPathAndWeight(g, null, 0, 2).first().edges());
		assertEquals(2, algo.computeShortestPathAndWeight(g, null, 0, 2).secondDouble());
		assertNull(algo.computeShortestPath(g, null, 2, 0));
		assertNull(algo.computeShortestPathAndWeight(g, null, 2, 0));
	}

}
