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

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestBase {

	private static GraphImpl graphImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				if (directed) {
					return vertices == 0 ? new GraphLinkedDirected() : new GraphLinkedDirected(vertices);
				} else {
					return vertices == 0 ? new GraphLinkedUndirected() : new GraphLinkedUndirected(vertices);
				}
			}
		};
	}

	@Test
	public void testVertexAdd() {
		GraphImplTestUtils.testVertexAdd(graphImpl());
	}

	@Test
	public void testCreateWithNVertices() {
		GraphImplTestUtils.testCreateWithNVertices(graphImpl());
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
	public void testGetEdges() {
		GraphImplTestUtils.testGetEdges(graphImpl());
	}

	@Test
	public void testEdgeIter() {
		GraphImplTestUtils.testEdgeIter(graphImpl());
	}

	@Test
	public void testDgree() {
		GraphImplTestUtils.testDgree(graphImpl());
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
