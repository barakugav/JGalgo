package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.jgalgo.RMQStatic;
import com.jgalgo.RMQStaticComparator;
import com.jgalgo.RMQStaticCartesianTrees;
import com.jgalgo.RMQStaticLookupTable;
import com.jgalgo.RMQStaticPlusMinusOne;
import com.jgalgo.RMQStaticPowerOf2Table;
import com.jgalgo.test.TestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;

public class RMQBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class PreProcess {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<Integer, RMQStaticComparator>> arrays;

		@Setup(Level.Trial)
		public void setup() {
			final SeedGenerator seedGen = new SeedGenerator(0xea7471a0349fe14eL);
			arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = TestUtils.randArray(arrSize, seedGen.nextSeed());
				arrays.add(Pair.of(Integer.valueOf(arrSize), RMQStaticComparator.ofIntArray(arr)));
			}
		}

		private void benchPreProcess(Supplier<? extends RMQStatic> builder, Blackhole blackhole) {
			for (Pair<Integer, RMQStaticComparator> arr : arrays) {
				RMQStatic rmq = builder.get();
				rmq.preProcessSequence(arr.second(), arr.first().intValue());
				blackhole.consume(rmq);
			}
		}

		@Benchmark
		public void LookupTable(Blackhole blackhole) {
			benchPreProcess(RMQStaticLookupTable::new, blackhole);
		}

		@Benchmark
		public void PowerOf2Table(Blackhole blackhole) {
			benchPreProcess(RMQStaticPowerOf2Table::new, blackhole);
		}

		@Benchmark
		public void CartesianTrees(Blackhole blackhole) {
			benchPreProcess(RMQStaticCartesianTrees::new, blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class PreProcessRMQPlusMinusOneBenderFarachColton2000 {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<Integer, RMQStaticComparator>> arrays;

		@Setup(Level.Trial)
		public void setup() {
			final Random rand = new Random(0x9fc881bb3f61bc29L);
			arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = new int[arrSize];
				for (int i = 1; i < arrSize; i++)
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				arrays.add(Pair.of(Integer.valueOf(arrSize), RMQStaticComparator.ofIntArray(arr)));
			}
		}

		@Benchmark
		public void benchPreProcess(Blackhole blackhole) {
			for (Pair<Integer, RMQStaticComparator> arr : arrays) {
				RMQStatic rmq = new RMQStaticPlusMinusOne();
				rmq.preProcessSequence(arr.second(), arr.first().intValue());
				blackhole.consume(rmq);
			}
		}

	}

	public static class QueryAbstract {

		int[] randArray(int size, long seed) {
			return TestUtils.randArray(size, seed);
		}

		List<Pair<RMQStatic.DataStructure, int[]>> createArrays(Supplier<? extends RMQStatic> builder, int arrSize) {
			final SeedGenerator seedGen = new SeedGenerator(0x5b3fba9dd26f2769L);

			List<Pair<RMQStatic.DataStructure, int[]>> arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = randArray(arrSize, seedGen.nextSeed());

				RMQStatic rmq = builder.get();
				RMQStatic.DataStructure rmqDS = rmq.preProcessSequence(RMQStaticComparator.ofIntArray(arr), arrSize);

				int queriesNum = arrSize;
				int[] queries = TestUtils.randArray(queriesNum * 2, 0, arrSize, seedGen.nextSeed());
				for (int q = 0; q < queriesNum; q++) {
					int i = queries[q * 2];
					int j = queries[q * 2 + 1];
					if (j < i) {
						int temp = i;
						i = j;
						j = temp;
					}
					queries[q * 2] = i;
					queries[q * 2 + 1] = j;
				}

				arrays.add(Pair.of(rmqDS, queries));
			}
			return arrays;
		}

		public void benchQuery(List<Pair<RMQStatic.DataStructure, int[]>> arrays, Blackhole blackhole) {
			for (Pair<RMQStatic.DataStructure, int[]> arr : arrays) {
				RMQStatic.DataStructure rmq = arr.first();
				int[] queries = arr.second();
				int queriesNum = queries.length / 2;
				for (int q = 0; q < queriesNum; q++) {
					int i = queries[q * 2];
					int j = queries[q * 2 + 1];
					int res = rmq.findMinimumInRange(i, j);
					blackhole.consume(res);
				}
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
		@State(Scope.Benchmark)
		public static class LookupTable extends QueryAbstract {

			@Param({ "128", "2500" })
			public int arrSize;
			private List<Pair<RMQStatic.DataStructure, int[]>> arrays;

			@Setup(Level.Iteration)
			public void setup() {
				arrays = createArrays(RMQStaticLookupTable::new, arrSize);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				benchQuery(arrays, blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
		@State(Scope.Benchmark)
		public static class PowerOf2Table extends QueryAbstract {

			@Param({ "128", "2500", "15000" })
			public int arrSize;
			private List<Pair<RMQStatic.DataStructure, int[]>> arrays;

			@Setup(Level.Iteration)
			public void setup() {
				arrays = createArrays(RMQStaticPowerOf2Table::new, arrSize);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				benchQuery(arrays, blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
		@State(Scope.Benchmark)
		public static class PlusMinusOne extends QueryAbstract {

			@Param({ "128", "2500", "15000" })
			public int arrSize;
			private List<Pair<RMQStatic.DataStructure, int[]>> arrays;

			@Override
			int[] randArray(int size, long seed) {
				final Random rand = new Random(seed);
				int[] arr = new int[size];
				for (int i = 1; i < arrSize; i++)
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				return arr;
			}

			@Setup(Level.Iteration)
			public void setup() {
				arrays = createArrays(RMQStaticPlusMinusOne::new, arrSize);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				benchQuery(arrays, blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
		@State(Scope.Benchmark)
		public static class CartesianTrees extends QueryAbstract {

			@Param({ "128", "2500", "15000" })
			public int arrSize;
			private List<Pair<RMQStatic.DataStructure, int[]>> arrays;

			@Setup(Level.Iteration)
			public void setup() {
				arrays = createArrays(RMQStaticCartesianTrees::new, arrSize);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				benchQuery(arrays, blackhole);
			}
		}

	}

}