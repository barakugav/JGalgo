package com.ugav.algo.test;

import java.util.Random;

import com.ugav.algo.RMQ;
import com.ugav.algo.RMQ.IntArrayComparator;

class RMQTestUtils {

	private RMQTestUtils() {
		throw new InternalError();
	}

	static boolean testRMQ65536(RMQ rmq) {
		return testRMQ(rmq, 65536, 4096);
	}

	static boolean testRMQ(RMQ rmq, int n, int queriesNum) {
		long seed = Utils.randSeed();
		int a[] = new int[n];
		int queries[][] = new int[queriesNum][];
		randRMQDataAndQueries(a, queries, seed);

		return testRMQ(rmq, a, queries);
	}

	static boolean testRMQ(RMQ rmq, int a[], int queries[][]) {
		try {
			RMQ.Result res = rmq.preprocessRMQ(new IntArrayComparator(a), a.length);

			for (int idx = 0; idx < queries.length; idx++) {
				int i = queries[idx][0];
				int j = queries[idx][1];
				int expectedIdx = queries[idx][2];
				int expected = a[expectedIdx];
				int actualIdx = res.query(i, j);
				int actual = a[actualIdx];

				if (actual != expected) {
					TestUtils.printTestStr(" [" + i + "," + j + "] -> expected[" + expectedIdx + "]=" + expected
							+ " actual[" + actualIdx + "]=" + actual + "\n");
					TestUtils.printTestStr("data size: " + a.length + "\n");
					TestUtils.printTestStr("queries num: " + queries.length + "\n");
					TestUtils.printTestStr(formatRMQDataAndQueries(a, queries));
					return false;
				}
			}
		} catch (RuntimeException e) {
			TestUtils.printTestStr(formatRMQDataAndQueries(a, queries));
			throw e;
		}

		return true;
	}

	static void randRMQDataPlusMinusOne(int a[], long seed) {
		Random rand = new Random(seed ^ 0x147d9f4baebea2f5L);
		a[0] = 0;
		for (int i = 1; i < a.length; i++)
			a[i] = a[i - 1] + rand.nextInt(2) * 2 - 1;
	}

	static void randRMQQueries(int a[], int queries[][], int blockSize, long seed) {
		Random rand = new Random(seed ^ 0x75af25429edde823L);
		for (int q = 0; q < queries.length;) {
			int i = rand.nextInt(a.length);
			if (i % blockSize == blockSize - 1)
				continue;
			int blockBase = (i / blockSize) * blockSize;
			int blockEnd = blockBase + blockSize;
			int j = rand.nextInt(blockEnd - i) + i + 1;

			int m = i;
			for (int k = i; k < j; k++)
				if (a[k] < a[m])
					m = k;
			queries[q++] = new int[] { i, j, m };
		}

	}

	static void randRMQDataAndQueries(int a[], int queries[][], long seed) {
		randRMQDataAndQueries(a, queries, a.length, seed);
	}

	static void randRMQDataAndQueries(int a[], int queries[][], int blockSize, long seed) {
		Utils.randArray(a, 0, 64, seed);
		randRMQQueries(a, queries, blockSize, seed);
	}

	static CharSequence formatRMQDataAndQueries(int a[], int queries[][]) {
		StringBuilder s = new StringBuilder();

		final int dataPerLine = 32;
		final int queriesPerLine = 12;

		if (a.length == 0)
			s.append("{}\n");
		else {
			s.append("{");
			for (int i = 0; i < a.length - 1; i++) {
				s.append(a[i]);
				s.append(((i + 1) % dataPerLine) == 0 ? ",\n" : ", ");
			}
			s.append(a[a.length - 1]);
			s.append("}\n");
		}

		if (queries.length == 0)
			s.append("{}\n");
		else {
			s.append("{");
			for (int i = 0; i < queries.length - 1; i++) {
				int q[] = queries[i];
				s.append("{" + q[0] + "," + q[1] + "," + q[2] + "},");
				s.append(((i + 1) % queriesPerLine) == 0 ? "\n" : " ");
			}
			int q[] = queries[queries.length - 1];
			s.append("{" + q[0] + "," + q[1] + "," + q[2] + "}");
			s.append("}\n");
		}

		return s;
	}
}
