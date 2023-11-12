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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ReversedGraphViewTest extends TestBase {

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph) {
		final long seed = 0x97dc96ffefd7165bL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		Graph<Integer, Integer> g;
		if (intGraph) {
			g = IntGraphFactory.newUndirected().setDirected(directed).newGraph();
		} else {
			g = GraphFactory.<Integer, Integer>newUndirected().setDirected(directed).newGraph();
		}

		for (int i = 0; i < n; i++)
			g.addVertex(Integer.valueOf(i + 1));
		for (int i = 0; i < m; i++)
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), Integer.valueOf(i + 1));
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					assertEquals(gOrig.vertices().size(), gRev.vertices().size());
					assertEquals(gOrig.vertices(), gRev.vertices());
				}
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					assertEquals(gOrig.edges().size(), gRev.edges().size());
					assertEquals(gOrig.edges(), gRev.edges());
				}
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					Integer nonExistingVertex, newVertex;
					if (gRev instanceof IndexGraph) {
						for (int v = 0;; v++) {
							if (!gRev0.vertices().contains(Integer.valueOf(v))) {
								nonExistingVertex = Integer.valueOf(v);
								break;
							}
						}
						newVertex = nonExistingVertex;

						/* index graphs should not support adding vertices with user defined identifiers */
						int newVertex0 = newVertex.intValue();
						assertThrows(UnsupportedOperationException.class,
								() -> ((IntGraph) gRev).addVertex(newVertex0));

						/* can't add new vertex directly to IndexGraph, only via wrapper Int/Obj Graph */
						IndexIdMap<Integer> viMap = gRev0.indexGraphVerticesMap();

						gRev0.addVertex(newVertex);
						newVertex = viMap.indexToId(newVertex.intValue());

					} else if (gRev instanceof IntGraph) {
						newVertex = Integer.valueOf(((IntGraph) gRev).addVertex());
					} else {
						for (int v = 0;; v++) {
							if (!gRev.vertices().contains(Integer.valueOf(v))) {
								nonExistingVertex = Integer.valueOf(v);
								break;
							}
						}
						newVertex = nonExistingVertex;
						gRev.addVertex(newVertex);
					}
					assertTrue(gOrig.vertices().contains(newVertex));
					assertTrue(gRev.vertices().contains(newVertex));
					assertEquals(gOrig.vertices(), gRev.vertices());

					for (int v = 0;; v++) {
						if (!gRev.vertices().contains(Integer.valueOf(v))) {
							nonExistingVertex = Integer.valueOf(v);
							break;
						}
					}
					if (gRev instanceof IndexGraph) {
						final Integer nonExistingVertex0 = nonExistingVertex;
						assertThrows(UnsupportedOperationException.class, () -> gRev.addVertex(nonExistingVertex0));
					} else {
						gRev.addVertex(nonExistingVertex);
						assertTrue(gOrig.vertices().contains(nonExistingVertex));
						assertTrue(gRev.vertices().contains(nonExistingVertex));
						assertEquals(gOrig.vertices(), gRev.vertices());
					}

					Integer vertexToRemove = gRev.vertices().iterator().next();
					gRev.removeVertex(vertexToRemove);
					if (!(gRev instanceof IndexGraph)) {
						assertFalse(gOrig.vertices().contains(vertexToRemove));
						assertFalse(gRev.vertices().contains(vertexToRemove));
					}
					assertEquals(gOrig.vertices(), gRev.vertices());
				}
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					Iterator<Integer> vit = gRev.vertices().iterator();
					Integer u = vit.next();
					Integer v = vit.next();

					Integer nonExistingEdge, newEdge;
					if (gRev instanceof IndexGraph) {
						for (int e = 0;; e++) {
							if (!gRev0.edges().contains(Integer.valueOf(e))) {
								nonExistingEdge = Integer.valueOf(e);
								break;
							}
						}
						newEdge = nonExistingEdge;

						/* index graphs should not support adding edges with user defined identifiers */
						int newEdge0 = newEdge.intValue();
						assertThrows(UnsupportedOperationException.class,
								() -> ((IntGraph) gRev).addEdge(u.intValue(), v.intValue(), newEdge0));

						/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
						IndexIdMap<Integer> viMap = gRev0.indexGraphVerticesMap();
						IndexIdMap<Integer> eiMap = gRev0.indexGraphEdgesMap();
						gRev0.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
						newEdge = eiMap.indexToId(newEdge.intValue());

					} else if (gRev instanceof IntGraph) {
						newEdge = Integer.valueOf(((IntGraph) gRev).addEdge(u.intValue(), v.intValue()));
					} else {
						for (int e = 0;; e++) {
							if (!gRev.edges().contains(Integer.valueOf(e))) {
								nonExistingEdge = Integer.valueOf(e);
								break;
							}
						}
						newEdge = nonExistingEdge;
						gRev.addEdge(u, v, newEdge);
					}
					assertTrue(gOrig.edges().contains(newEdge));
					assertTrue(gRev.edges().contains(newEdge));
					assertEquals(gOrig.edges(), gRev.edges());

					for (int e = 0;; e++) {
						if (!gRev.edges().contains(Integer.valueOf(e))) {
							nonExistingEdge = Integer.valueOf(e);
							break;
						}
					}
					if (gRev instanceof IndexGraph) {
						Integer nonExistingEdge0 = nonExistingEdge;
						assertThrows(UnsupportedOperationException.class, () -> gRev.addEdge(u, v, nonExistingEdge0));
					} else {
						gRev.addEdge(u, v, nonExistingEdge);
						assertTrue(gOrig.edges().contains(nonExistingEdge));
						assertTrue(gRev.edges().contains(nonExistingEdge));
						assertEquals(gOrig.edges(), gRev.edges());
					}

					Integer edgeToRemove = gRev.edges().iterator().next();
					gRev.removeEdge(edgeToRemove);
					if (!(gRev instanceof IndexGraph)) {
						assertFalse(gOrig.edges().contains(edgeToRemove));
						assertFalse(gRev.edges().contains(edgeToRemove));
					}
					assertEquals(gOrig.edges(), gRev.edges());
				}
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					for (Integer u : gRev.vertices()) {
						EdgeSet<Integer, Integer> edges = gRev.outEdges(u);
						assertEquals(gOrig.inEdges(u).size(), edges.size());
						assertEquals(gOrig.inEdges(u), edges);

						Set<Integer> iteratedEdges = new IntOpenHashSet();
						for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
							Integer peekNext = eit.peekNext();
							Integer e = eit.next();
							assertEquals(e, peekNext);

							assertEquals(u, eit.source());
							assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
							assertEquals(gRev.edgeEndpoint(e, u), eit.target());
							assertEquals(u, gRev.edgeEndpoint(e, eit.target()));

							iteratedEdges.add(e);
						}

						assertEquals(edges.size(), iteratedEdges.size());
						for (Integer e : gOrig.edges()) {
							if (iteratedEdges.contains(e)) {
								assertTrue(edges.contains(e));
							} else {
								assertFalse(edges.contains(e));
							}
						}
					}
					for (Integer v : gRev.vertices()) {
						EdgeSet<Integer, Integer> edges = gRev.inEdges(v);
						assertEquals(gOrig.outEdges(v).size(), edges.size());
						assertEquals(gOrig.outEdges(v), edges);

						Set<Integer> iteratedEdges = new IntOpenHashSet();
						for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
							Integer peekNext = eit.peekNext();
							Integer e = eit.next();
							assertEquals(e, peekNext);

							assertEquals(v, eit.target());
							assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
							assertEquals(gRev.edgeEndpoint(e, v), eit.source());
							assertEquals(v, gRev.edgeEndpoint(e, eit.source()));

							iteratedEdges.add(e);
						}

						assertEquals(edges.size(), iteratedEdges.size());
						for (Integer e : gOrig.edges()) {
							if (iteratedEdges.contains(e)) {
								assertTrue(edges.contains(e));
							} else {
								assertFalse(edges.contains(e));
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void testEdgesSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					for (Integer u : gRev.vertices()) {
						for (Integer v : gRev.vertices()) {
							EdgeSet<Integer, Integer> edges = gRev.getEdges(u, v);
							assertEquals(gOrig.getEdges(v, u).size(), edges.size());
							assertEquals(gOrig.getEdges(v, u), edges);

							if (edges.isEmpty()) {
								assertNull(gRev.getEdge(u, v));
							} else {
								Integer e = gRev.getEdge(u, v);
								assertNotNull(e);
								assertTrue(edges.contains(e));
							}

							for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
								Integer peekNext = eit.peekNext();
								Integer e = eit.next();
								assertEquals(e, peekNext);

								assertEquals(u, eit.source());
								assertEquals(v, eit.target());
								assertEquals(gOrig.edgeEndpoint(e, u), v);
								assertEquals(gOrig.edgeEndpoint(e, v), u);
								assertEquals(u, gRev.edgeEndpoint(e, v));
								assertEquals(v, gRev.edgeEndpoint(e, u));
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void testRemoveEdgesOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					Integer v = gRev.vertices().iterator().next();

					gRev.removeEdgesOf(v);
					assertTrue(gRev.outEdges(v).isEmpty());
					assertTrue(gRev.inEdges(v).isEmpty());
					assertTrue(gOrig.outEdges(v).isEmpty());
					assertTrue(gOrig.inEdges(v).isEmpty());
				}
			}
		}
	}

	@Test
	public void testRemoveEdgesInOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					Iterator<Integer> vit = gRev.vertices().iterator();
					Integer v1 = vit.next();
					Integer v2 = vit.next();
					Integer v3 = vit.next();

					gRev.removeInEdgesOf(v1);
					assertTrue(gRev.inEdges(v1).isEmpty());
					assertTrue(gOrig.outEdges(v1).isEmpty());

					gRev.inEdges(v2).clear();
					assertTrue(gRev.inEdges(v2).isEmpty());
					assertTrue(gOrig.outEdges(v2).isEmpty());

					if (!index) {
						for (Integer e : new IntArrayList(gRev.inEdges(v3)))
							gRev.inEdges(v3).remove(e);
						assertTrue(gRev.inEdges(v3).isEmpty());
						assertTrue(gOrig.outEdges(v3).isEmpty());
					}
				}
			}
		}
	}

	@Test
	public void testRemoveEdgesOutOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					Iterator<Integer> vit = gRev.vertices().iterator();
					Integer v1 = vit.next();
					Integer v2 = vit.next();
					Integer v3 = vit.next();

					gRev.removeOutEdgesOf(v1);
					assertTrue(gRev.outEdges(v1).isEmpty());
					assertTrue(gOrig.inEdges(v1).isEmpty());

					gRev.outEdges(v2).clear();
					assertTrue(gRev.outEdges(v2).isEmpty());
					assertTrue(gOrig.inEdges(v2).isEmpty());

					if (!index) {
						for (Integer e : new IntArrayList(gRev.outEdges(v3)))
							gRev.outEdges(v3).remove(e);
						assertTrue(gRev.outEdges(v3).isEmpty());
						assertTrue(gOrig.inEdges(v3).isEmpty());
					}
				}
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			Graph<Integer, Integer> gOrig0 = createGraph(true, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

				Integer e = gRev.edges().iterator().next();
				Integer s = gRev.edgeSource(e), t = gRev.edgeTarget(e);

				gRev.reverseEdge(e);
				assertEquals(s, gRev.edgeTarget(e));
				assertEquals(t, gRev.edgeSource(e));
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					for (Integer e : gRev.edges()) {
						assertEquals(gOrig.edgeSource(e), gRev.edgeTarget(e));
						assertEquals(gOrig.edgeTarget(e), gRev.edgeSource(e));
					}
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					gRev.clear();
					assertTrue(gRev.vertices().isEmpty());
					assertTrue(gRev.edges().isEmpty());
					assertTrue(gOrig.vertices().isEmpty());
					assertTrue(gOrig.edges().isEmpty());
				}
			}
		}
	}

	@Test
	public void testClearEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					gRev.clearEdges();
					assertTrue(gRev.edges().isEmpty());
					assertTrue(gOrig.edges().isEmpty());
				}
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		int keyCounter = 0;
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					String key1 = "key" + keyCounter++, key2 = "key" + keyCounter++;
					{
						WeightsInt<Integer> vWeights1 = gOrig.addVerticesWeights(key1, int.class);
						for (Integer v : gOrig.vertices())
							vWeights1.set(v, rand.nextInt(10000));
						WeightsInt<Integer> vWeights2 = gRev.addVerticesWeights(key2, int.class);
						for (Integer v : gRev.vertices())
							vWeights2.set(v, rand.nextInt(10000));
					}

					assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
					for (String key : List.of(key1, key2)) {
						WeightsInt<Integer> wOrig = gOrig.getVerticesWeights(key);
						WeightsInt<Integer> wRev = gRev.getVerticesWeights(key);

						for (Integer v : gRev.vertices())
							assertEquals(wOrig.get(v), wRev.get(v));
						assertEquals(wOrig.defaultWeight(), wRev.defaultWeight());
					}

					gRev.removeVerticesWeights(key1);
					assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
				}
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		int keyCounter = 0;
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

					String key1 = "key" + keyCounter++, key2 = "key" + keyCounter++;
					{
						WeightsInt<Integer> eWeights1 = gOrig.addEdgesWeights(key1, int.class);
						for (Integer e : gOrig.edges())
							eWeights1.set(e, rand.nextInt(10000));
						WeightsInt<Integer> eWeights2 = gRev.addEdgesWeights(key2, int.class);
						for (Integer e : gRev.edges())
							eWeights2.set(e, rand.nextInt(10000));
					}

					assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
					for (String key : List.of(key1, key2)) {
						WeightsInt<Integer> wOrig = gOrig.getEdgesWeights(key);
						WeightsInt<Integer> wRev = gRev.getEdgesWeights(key);

						for (Integer e : gRev.edges())
							assertEquals(wOrig.get(e), wRev.get(e));
						assertEquals(wOrig.defaultWeight(), wRev.defaultWeight());
					}

					gRev.removeEdgesWeights(key1);
					assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
				}
			}
		}
	}

	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
					assertEqualsBool(gOrig.isAllowParallelEdges(), gRev.isAllowParallelEdges());
					assertEqualsBool(gOrig.isAllowSelfEdges(), gRev.isAllowSelfEdges());
					assertEqualsBool(gOrig.isDirected(), gRev.isDirected());
				}
			}
		}
	}

	@Test
	public void testRemoveListeners() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				IndexGraph gRev = createGraph(directed, intGraph).indexGraph().reverseView();
				AtomicBoolean called = new AtomicBoolean();
				IndexRemoveListener listener = new IndexRemoveListener() {
					@Override
					public void removeLast(int removedIdx) {
						called.set(true);
					}

					@Override
					public void swapAndRemove(int removedIdx, int swappedIdx) {
						called.set(true);
					}
				};

				gRev.addVertexRemoveListener(listener);
				called.set(false);
				gRev.removeVertex(gRev.vertices().iterator().nextInt());
				assertTrue(called.get());

				called.set(false);
				gRev.removeEdge(gRev.edges().iterator().nextInt());
				assertFalse(called.get());

				gRev.removeVertexRemoveListener(listener);
				called.set(false);
				gRev.removeVertex(gRev.vertices().iterator().nextInt());
				assertFalse(called.get());

				gRev.addEdgeRemoveListener(listener);
				called.set(false);
				gRev.removeEdge(gRev.edges().iterator().nextInt());
				assertTrue(called.get());

				int v = gRev.vertices().iterator().nextInt();
				gRev.removeEdgesOf(v);
				called.set(false);
				gRev.removeVertex(v);
				assertFalse(called.get());

				gRev.removeEdgeRemoveListener(listener);
				called.set(false);
				gRev.removeEdge(gRev.edges().iterator().nextInt());
				assertFalse(called.get());
			}
		}
	}

}
