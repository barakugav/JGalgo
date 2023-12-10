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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntGraphImplTest extends TestBase {

	@Test
	public void addVertex() {
		final Random rand = new Random(0x6771c2cd7f36ac88L);
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IntGraph g = directed ? IntGraph.newDirected() : IntGraph.newDirected();
				IntSet vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v))
						continue;
					g.addVertex(v);
					vertices.add(v);
				}
				assertEquals(vertices, g.vertices());
			}
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0x105a48eea70e320dL);
		foreachBoolConfig(directed -> {
			IntGraph g = directed ? IntGraph.newDirected() : IntGraph.newDirected();

			IntSet vertices = new IntOpenHashSet();
			IntList verticesList = new IntArrayList();
			for (int r = 0; r < 50; r++) {
				int num = rand.nextInt(5);
				IntList vs = new IntArrayList();
				while (vs.size() < num) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v) || vs.contains(v))
						continue;
					vs.add(v);
				}
				if (r % 4 == 0) {
					g.addVertices(vs);
					vertices.addAll(vs);
					verticesList.addAll(vs);
				} else if (r % 4 == 1) {
					vs.add(-1);
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
				} else if (r % 4 == 2 && vs.size() > 0) {
					vs.add(randElement(vs, rand));
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
				} else if (r % 4 == 3 && vertices.size() > 0) {
					vs.add(randElement(verticesList, rand));
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
				}
				assertEquals(vertices, g.vertices());
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesToArray() {
		final Random rand = new Random(0x6771c2cd7f36ac88L);
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IntGraph g = directed ? IntGraph.newDirected() : IntGraph.newDirected();
				IntSet vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v))
						continue;
					g.addVertex(v);
					vertices.add(v);
				}

				Object[] arr1 = g.vertices().toArray();
				Integer[] arr2 = g.vertices().toArray(new Integer[0]);
				Integer[] arr3Input = new Integer[vertices.size()];
				Integer[] arr3 = g.vertices().toArray(arr3Input);
				Integer[] arr4Input = new Integer[vertices.size() + 7];
				Arrays.fill(arr4Input, -788);
				Integer[] arr4 = g.vertices().toArray(arr4Input);
				int[] arr5 = g.vertices().toIntArray();
				int[] arr6 = g.vertices().toArray(new int[0]);
				int[] arr7Input = new int[vertices.size()];
				int[] arr7 = g.vertices().toArray(arr7Input);
				int[] arr8Input = new int[vertices.size() + 7];
				Arrays.fill(arr8Input, 989846);
				int[] arr8 = g.vertices().toArray(arr8Input);
				assertEquals(vertices, Set.of(arr1)); /* Set.of() checks that there are no duplications */
				assertEquals(vertices, Set.of(arr2));
				assertEquals(vertices, Set.of(arr3));
				assertEquals(vertices, Set.of(Arrays.copyOf(arr4, n)));
				assertEquals(vertices, IntSet.of(arr5)); /* IntSet.of() checks that there are no duplications */
				assertEquals(vertices, IntSet.of(arr6));
				assertEquals(vertices, IntSet.of(arr7));
				assertEquals(vertices, IntSet.of(Arrays.copyOf(arr8, n)));
				assertTrue(arr3Input == arr3);
				assertTrue(arr4Input == arr4);
				assertTrue(arr7Input == arr7);
				assertTrue(arr8Input == arr8);
				assertNull(arr4[n]);
				for (int i = n + 1; i < arr4.length; i++)
					assertEquals(-788, arr4[i]);
				for (int i = n; i < arr8.length; i++)
					assertEquals(989846, arr8[i]);
			}
		});
	}

	@Test
	public void addVertexNegative() {
		IntGraph g = IntGraph.newUndirected();
		assertThrows(IllegalArgumentException.class, () -> g.addVertex(-1));
	}

	@Test
	public void addVertexDuplication() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(7);
		assertThrows(IllegalArgumentException.class, () -> g.addVertex(7));
	}

	@Test
	public void addEdgeNegative() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(7);
		g.addVertex(8);
		assertThrows(IllegalArgumentException.class, () -> g.addEdge(7, 8, -1));
	}

	@Test
	public void addEdgeDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.addEdge(8, 7, 3));
	}

	@Test
	public void renameVertex() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.renameVertex(7, 22);

		IntGraph expected = IntGraph.newDirected();
		expected.addVertex(22);
		expected.addVertex(8);
		expected.addEdge(22, 8, 3);
		assertEquals(expected, g);
	}

	@Test
	public void renameVertexDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex(7, 8));
	}

	@Test
	public void renameVertexNonExisting() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(NoSuchVertexException.class, () -> g.renameVertex(22, 23));
	}

	@Test
	public void renameVertexNegative() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex(7, -1));
	}

	@Test
	public void renameEdge() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.renameEdge(3, 10);

		IntGraph expected = IntGraph.newDirected();
		expected.addVertex(7);
		expected.addVertex(8);
		expected.addEdge(7, 8, 10);
		assertEquals(expected, g);
	}

	@Test
	public void renameEdgeDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.addEdge(7, 8, 10);
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge(3, 10));
	}

	@Test
	public void renameEdgeNonExisting() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(NoSuchEdgeException.class, () -> g.renameEdge(10, 15));
	}

	@Test
	public void renameEdgeNegative() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge(3, -1));
	}

}
