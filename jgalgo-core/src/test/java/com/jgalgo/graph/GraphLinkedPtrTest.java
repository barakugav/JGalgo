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

public class GraphLinkedPtrTest extends TestBase {

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl() {
		return directed -> IntGraphFactory.newUndirected().setOption("impl", "linked-list-ptr").setDirected(directed)
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
		final long seed = 0x100115652062b424L;
		GraphImplTestUtils.testClear(graphImpl(), seed);
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x914bb2f87efda719L;
		GraphImplTestUtils.testClearEdges(graphImpl(), seed);
	}

	@Test
	public void testCopy() {
		final long seed = 0x6f2eabc8e7cd3a70L;
		GraphImplTestUtils.testCopy(graphImpl(), seed);
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x5c51c3fa807b25bcL;
		GraphImplTestUtils.testCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x9f77f9dfded3f6fL;
		GraphImplTestUtils.testImmutableCopy(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0x6966e624022a1540L;
		GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x757d2f9883276f90L;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x96f07cf342fcb057L;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xb3775d0c2d4aa98aL;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xbda54e345679e161L;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0x136a0df5ecaae5a2L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
