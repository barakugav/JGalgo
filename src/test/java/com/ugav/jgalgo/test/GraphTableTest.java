package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.GraphTableDirected;
import com.ugav.jgalgo.GraphTableUndirected;
import com.ugav.jgalgo.test.GraphImplTestUtils.GraphImpl;

public class GraphTableTest extends TestUtils {

	private static GraphImpl graphImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				return directed ? new GraphTableDirected(vertices) : new GraphTableUndirected(vertices);
			}
		};
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
	public void testUndirectedRandOps() {
		final long seed = 0x2aee685276834043L;
		GraphImplTestUtils.testUndirectedRandOps(graphImpl(), seed);
	}

}
