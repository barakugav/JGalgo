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

public class GraphLinkedTest extends TestBase {

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl() {
		return directed -> IntGraphFactory.newUndirected().setOption("impl", "linked-list").setDirected(directed)
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
		final long seed = 0xa87763a4d802f408L;
		GraphImplTestUtils.testClear(graphImpl(), seed);
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x43435f8582dc816eL;
		GraphImplTestUtils.testClearEdges(graphImpl(), seed);
	}

	@Test
	public void testCopy() {
		final long seed = 0xe2efa42f139a4254L;
		GraphImplTestUtils.testCopy(graphImpl(), seed);
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x814eb6bdae72fed1L;
		GraphImplTestUtils.testCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x3766037dcbed2ec3L;
		GraphImplTestUtils.testImmutableCopy(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0xd66a281795c52020L;
		GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0xfa13f4e010916dcaL;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x62f68fa5a31c6010L;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0x643cd690ad09efb4L;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xa05a3427656375aaL;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xdd2bc9ad386bf866L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
