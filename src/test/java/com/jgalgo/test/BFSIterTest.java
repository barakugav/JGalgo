package com.jgalgo.test;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.BFSIter;
import com.jgalgo.Graph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class BFSIterTest extends TestUtils {

	@Test
	public void testBfsConnected() {
		final long seed = 0xa782852da2497b7fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int source = rand.nextInt(n);

			BitSet visited = new BitSet(n);
			for (BFSIter it = new BFSIter(g, source); it.hasNext();) {
				int v = it.nextInt();
				int e = it.inEdge();
				assertFalse(visited.get(v), "already visited vertex " + v);
				if (v != source)
					assertTrue(g.edgeEndpoint(e, g.edgeEndpoint(e, v)) == v,
							"v is not an endpoint of inEdge");
				visited.set(v);
			}
		});
	}

}