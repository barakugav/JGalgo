package com.jgalgo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.LCADynamic;

import it.unimi.dsi.fastutil.ints.IntArrays;

public class LCADynamicTestUtils extends TestUtils {

	private LCADynamicTestUtils() {
	}

	static void fullBinaryTreesRandOps(Supplier<? extends LCADynamic<Integer>> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Collection<Op> ops = generateRandOpsOnFullBinaryTree(n, m, seedGen.nextSeed());
			testLCA(builder, n, ops);
		});
	}

	static void randTrees(Supplier<? extends LCADynamic<Integer>> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(8, 512, 512), phase(8, 512, 2048), phase(1, 4096, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Collection<Op> ops = generateRandOps(n, m, seedGen.nextSeed());
			testLCA(builder, n, ops);
		});
	}

	static Collection<Op> generateRandOpsOnFullBinaryTree(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		IntArrays.shuffle(opsOrder, rand);

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
			case addLeafOp: {
				int p = (nodesCount - 1) / 2;
				ops.add(new OpAddLeaf(p));
				nodesCount++;
				break;
			}
			case lcaOp: {
				int x = rand.nextInt(nodesCount);
				int y = rand.nextInt(nodesCount);
				ops.add(new OpLCAQuery(x, y));
				break;
			}
			default:
				throw new IllegalStateException();
			}
		}
		return ops;
	}

	public static Collection<Op> generateRandOps(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		IntArrays.shuffle(opsOrder, rand);

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
			case addLeafOp: {
				int p = rand.nextInt(nodesCount);
				ops.add(new OpAddLeaf(p));
				nodesCount++;
				break;
			}
			case lcaOp: {
				int x = rand.nextInt(nodesCount);
				int y = rand.nextInt(nodesCount);
				ops.add(new OpLCAQuery(x, y));
				break;
			}
			default:
				throw new IllegalStateException();
			}
		}
		return ops;
	}

	@SuppressWarnings("boxing")
	static void testLCA(Supplier<? extends LCADynamic<Integer>> builder, int n, Collection<Op> ops) {
		List<LCADynamic.Node<Integer>> nodes = new ArrayList<>();
		LCADynamic<Integer> lca = builder.get();

		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				nodes.add(lca.initTree(0));

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				LCADynamic.Node<Integer> parent = nodes.get(op.parent);
				nodes.add(lca.addLeaf(parent, parent.getNodeData() + 1));

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;

				LCADynamic.Node<Integer> x = nodes.get(op.x), y = nodes.get(op.y);
				if (x.getNodeData() > y.getNodeData()) {
					LCADynamic.Node<Integer> temp = x;
					x = y;
					y = temp;
				}
				while (x.getNodeData() < y.getNodeData())
					y = y.getParent();
				while (x != y) {
					x = x.getParent();
					y = y.getParent();
				}

				LCADynamic.Node<Integer> lcaExpected = x;
				LCADynamic.Node<Integer> lcaActual = lca.calcLCA(nodes.get(op.x), nodes.get(op.y));
				Assertions.assertEquals(lcaExpected, lcaActual, "LCA has an expected value");

			} else {
				throw new IllegalStateException();
			}
		}
	}

	public static class Op {
	}

	public static class OpInitTree extends Op {
	}

	public static class OpAddLeaf extends Op {
		public final int parent;

		OpAddLeaf(int parent) {
			this.parent = parent;
		}
	}

	public static class OpLCAQuery extends Op {
		public final int x, y;

		OpLCAQuery(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}