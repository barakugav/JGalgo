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

package com.jgalgo.graph;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;

public class GraphTableTest extends TestBase {

	private static Boolean2ObjectFunction<Graph> graphImpl() {
		return directed -> IndexGraphFactory.newUndirected().setOption("impl", "matrix").setDirected(directed)
				.newGraph();
	}

	@Test
	public void testVertexAdd() {
		GraphImplTestUtils.testVertexAdd(graphImpl());
	}

	@Test
	public void testAddEdge() {
		GraphImplTestUtils.testAddEdge(graphImpl());
	}

	@Test
	public void testGetEdge() {
		GraphImplTestUtils.testGetEdge(graphImpl());
	}

	@Test
	public void testGetEdgesOutIn() {
		GraphImplTestUtils.testGetEdgesOutIn(graphImpl());
	}

	@Test
	public void testGetEdgesSourceTarget() {
		GraphImplTestUtils.testGetEdgesSourceTarget(graphImpl());
	}

	@Test
	public void testEdgeIter() {
		GraphImplTestUtils.testEdgeIter(graphImpl());
	}

	@Test
	public void testDegree() {
		GraphImplTestUtils.testDegree(graphImpl());
	}

	@Test
	public void testClear() {
		final long seed = 0x25824e374104beceL;
		GraphImplTestUtils.testClear(graphImpl(), seed);
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x79550c17b3a2bb6eL;
		GraphImplTestUtils.testClearEdges(graphImpl(), seed);
	}

	@Test
	public void testCopy() {
		final long seed = 0xd400f44f753a56b6L;
		GraphImplTestUtils.testCopy(graphImpl(), seed);
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x1e7e9287a4e51db6L;
		GraphImplTestUtils.testCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x325c47e089f6edebL;
		GraphImplTestUtils.testImmutableCopy(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0x662367fc40987614L;
		GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x63a396934a49021cL;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0xcebe72e8015778c1L;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xe2e6e3d11dfaa9dfL;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0x2aee685276834043L;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0x4cd9a3bcb63cf8f8L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
