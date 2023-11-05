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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphBuilderTest extends TestBase {

	@SuppressWarnings("unchecked")
	@Test
	public void indexGraph() {
		final long seed = 0x56f68a18a0ca8d84L;
		final Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean buildMut : BooleanList.of(false, true)) {
				IndexGraphBuilder b = directed ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
				IndexGraph g = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();

				/* Add vertices and edges */
				final int n = 12 + rand.nextInt(12);
				final int m = 20 + rand.nextInt(20);
				while (g.vertices().size() < n) {
					int vG = g.addVertex();
					int vB = b.addVertex();
					assertEquals(vG, vB);
				}
				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int e = rand.nextInt(2 * m);
					if (g.edges().contains(e))
						continue;
					int u = vs[rand.nextInt(vs.length)], v = vs[rand.nextInt(vs.length)];
					g.addEdge(u, v);
				}
				for (int e : IntArrays.shuffle(g.edges().toIntArray(), rand)) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					b.addEdge(u, v, e);
				}

				/* Add weights */
				AtomicInteger weightIdx = new AtomicInteger();
				@SuppressWarnings("rawtypes")
				BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
					for (boolean edgesWeights : BooleanList.of(false, true)) {
						for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
							String key = "weight" + weightIdx.getAndIncrement();
							Object defVal = valSupplier.get();
							IWeights wG, wB;
							IntSet elements;
							if (!edgesWeights) {
								wG = (IWeights) g.addVerticesWeights(key, type, defVal);
								wB = (IWeights) b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = (IWeights) g.addEdgesWeights(key, type, defVal);
								wB = (IWeights) b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (int elm : elements) {
								Object w = valSupplier.get();
								wG.setAsObj(elm, w);
								wB.setAsObj(elm, w);
							}
						}
					}
				};
				addWeights.accept(byte.class, () -> Byte.valueOf((byte) rand.nextInt()));
				addWeights.accept(short.class, () -> Short.valueOf((short) rand.nextInt()));
				addWeights.accept(int.class, () -> Integer.valueOf(rand.nextInt()));
				addWeights.accept(long.class, () -> Long.valueOf(rand.nextLong()));
				addWeights.accept(float.class, () -> Float.valueOf(rand.nextFloat()));
				addWeights.accept(double.class, () -> Double.valueOf(rand.nextDouble()));
				addWeights.accept(boolean.class, () -> Boolean.valueOf(rand.nextBoolean()));
				addWeights.accept(char.class, () -> Character.valueOf((char) rand.nextInt()));
				addWeights.accept(String.class, () -> String.valueOf(rand.nextInt()));

				IntGraph gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				for (String key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeightAsObj(),
							gActual.getVerticesWeights(key).defaultWeightAsObj());
				for (String key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeightAsObj(),
							gActual.getEdgesWeights(key).defaultWeightAsObj());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void intGraph() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean buildMut : BooleanList.of(false, true)) {
				IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
				IntGraph g = directed ? IntGraph.newDirected() : IntGraph.newUndirected();

				/* Add vertices and edges */
				final int n = 12 + rand.nextInt(12);
				final int m = 20 + rand.nextInt(20);
				while (g.vertices().size() < n) {
					int v = rand.nextInt(2 * n);
					if (g.vertices().contains(v))
						continue;
					g.addVertex(v);
					b.addVertex(v);
				}
				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int e = rand.nextInt(2 * m);
					if (g.edges().contains(e))
						continue;
					int u = vs[rand.nextInt(vs.length)], v = vs[rand.nextInt(vs.length)];
					g.addEdge(u, v, e);
					b.addEdge(u, v, e);
				}

				/* Add weights */
				AtomicInteger weightIdx = new AtomicInteger();
				@SuppressWarnings("rawtypes")
				BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
					for (boolean edgesWeights : BooleanList.of(false, true)) {
						for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
							String key = "weight" + weightIdx.getAndIncrement();
							Object defVal = valSupplier.get();
							IWeights wG, wB;
							IntSet elements;
							if (!edgesWeights) {
								wG = (IWeights) g.addVerticesWeights(key, type, defVal);
								wB = (IWeights) b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = (IWeights) g.addEdgesWeights(key, type, defVal);
								wB = (IWeights) b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (int elm : elements) {
								Object w = valSupplier.get();
								wG.setAsObj(elm, w);
								wB.setAsObj(elm, w);
							}
						}
					}
				};
				addWeights.accept(byte.class, () -> Byte.valueOf((byte) rand.nextInt()));
				addWeights.accept(short.class, () -> Short.valueOf((short) rand.nextInt()));
				addWeights.accept(int.class, () -> Integer.valueOf(rand.nextInt()));
				addWeights.accept(long.class, () -> Long.valueOf(rand.nextLong()));
				addWeights.accept(float.class, () -> Float.valueOf(rand.nextFloat()));
				addWeights.accept(double.class, () -> Double.valueOf(rand.nextDouble()));
				addWeights.accept(boolean.class, () -> Boolean.valueOf(rand.nextBoolean()));
				addWeights.accept(char.class, () -> Character.valueOf((char) rand.nextInt()));
				addWeights.accept(String.class, () -> String.valueOf(rand.nextInt()));

				IntGraph gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				for (String key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeightAsObj(),
							gActual.getVerticesWeights(key).defaultWeightAsObj());
				for (String key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeightAsObj(),
							gActual.getEdgesWeights(key).defaultWeightAsObj());

				if (!buildMut) {
					int[] vs = gActual.vertices().toIntArray();
					int[] es = gActual.edges().toIntArray();
					for (String key : gActual.getVerticesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getVerticesWeights(key);
						int v = vs[rand.nextInt(vs.length)];
						Object data = w.getAsObj(Integer.valueOf(vs[rand.nextInt(vs.length)]));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(Integer.valueOf(v), data));
					}
					for (String key : gActual.getEdgesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getEdgesWeights(key);
						int e = es[rand.nextInt(es.length)];
						Object data = w.getAsObj(Integer.valueOf(es[rand.nextInt(es.length)]));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(Integer.valueOf(e), data));
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void objGraph() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean buildMut : BooleanList.of(false, true)) {
				GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
				Graph<Integer, Integer> g = directed ? Graph.newDirected() : Graph.newUndirected();

				/* Add vertices and edges */
				final int n = 12 + rand.nextInt(12);
				final int m = 20 + rand.nextInt(20);
				while (g.vertices().size() < n) {
					Integer v = Integer.valueOf(rand.nextInt(2 * n));
					if (g.vertices().contains(v))
						continue;
					g.addVertex(v);
					b.addVertex(v);
				}
				for (List<Integer> vs = new ArrayList<>(g.vertices()); g.edges().size() < m;) {
					Integer e = Integer.valueOf(rand.nextInt(2 * m));
					if (g.edges().contains(e))
						continue;
					Integer u = vs.get(rand.nextInt(vs.size())), v = vs.get(rand.nextInt(vs.size()));
					g.addEdge(u, v, e);
					b.addEdge(u, v, e);
				}

				/* Add weights */
				AtomicInteger weightIdx = new AtomicInteger();
				@SuppressWarnings("rawtypes")
				BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
					for (boolean edgesWeights : BooleanList.of(false, true)) {
						for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
							String key = "weight" + weightIdx.getAndIncrement();
							Object defVal = valSupplier.get();
							Weights wG, wB;
							Set<Integer> elements;
							if (!edgesWeights) {
								wG = g.addVerticesWeights(key, type, defVal);
								wB = b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = g.addEdgesWeights(key, type, defVal);
								wB = b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (Integer elm : elements) {
								Object w = valSupplier.get();
								wG.setAsObj(elm, w);
								wB.setAsObj(elm, w);
							}
						}
					}
				};
				addWeights.accept(byte.class, () -> Byte.valueOf((byte) rand.nextInt()));
				addWeights.accept(short.class, () -> Short.valueOf((short) rand.nextInt()));
				addWeights.accept(int.class, () -> Integer.valueOf(rand.nextInt()));
				addWeights.accept(long.class, () -> Long.valueOf(rand.nextLong()));
				addWeights.accept(float.class, () -> Float.valueOf(rand.nextFloat()));
				addWeights.accept(double.class, () -> Double.valueOf(rand.nextDouble()));
				addWeights.accept(boolean.class, () -> Boolean.valueOf(rand.nextBoolean()));
				addWeights.accept(char.class, () -> Character.valueOf((char) rand.nextInt()));
				addWeights.accept(String.class, () -> String.valueOf(rand.nextInt()));

				Graph<Integer, Integer> gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				for (String key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeightAsObj(),
							gActual.getVerticesWeights(key).defaultWeightAsObj());
				for (String key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeightAsObj(),
							gActual.getEdgesWeights(key).defaultWeightAsObj());

				if (!buildMut) {
					List<Integer> vs = new ArrayList<>(gActual.vertices());
					List<Integer> es = new ArrayList<>(gActual.edges());
					for (String key : gActual.getVerticesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getVerticesWeights(key);
						Integer v = vs.get(rand.nextInt(vs.size()));
						Object data = w.getAsObj(vs.get(rand.nextInt(vs.size())));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(v, data));
					}
					for (String key : gActual.getEdgesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getEdgesWeights(key);
						Integer e = es.get(rand.nextInt(es.size()));
						Object data = w.getAsObj(es.get(rand.nextInt(es.size())));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(e, data));
					}
				}
			}
		}
	}

}
