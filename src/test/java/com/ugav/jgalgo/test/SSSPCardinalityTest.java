package com.ugav.jgalgo.test;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPCardinality;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class SSSPCardinalityTest extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x19a192dc9f21c8d7L;
		testRandGraph(true, seed);
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x492144d38445cac7L;
		testRandGraph(false, seed);
	}

	private static void testRandGraph(boolean directed, long seed) {
		EdgeWeightFunc w = e -> 1;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).doubleEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			int source = rand.nextInt(g.vertices().size());

			SSSPCardinality algo = new SSSPCardinality();
			SSSP.Result actualRes = algo.calcDistances(g, source);

			SSSP validationAlgo = new SSSPDijkstra();
			SSSPTestUtils.validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

}