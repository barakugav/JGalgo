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
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ImmutableGraphViewTest extends TestBase {

	private static final String VerticesWeightsKey = "vWeights";
	private static final String EdgesWeightsKey = "eWeights";

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

		WeightsInt<Integer> vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i = 0; i < n; i++) {
			Integer v = Integer.valueOf(i + 1);
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt<Integer> eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int i = 0; i < m; i++) {
			Integer e = Integer.valueOf(i + 1);
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					assertEquals(gOrig.vertices().size(), gImmutable.vertices().size());
					assertEquals(gOrig.vertices(), gImmutable.vertices());
				}
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
					assertEquals(gOrig.edges().size(), gImmutable.edges().size());
					assertEquals(gOrig.edges(), gImmutable.edges());
				}
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					Integer nonExistingVertex;
					for (int v = 0;; v++) {
						if (!gImmutable.vertices().contains(Integer.valueOf(v))) {
							nonExistingVertex = Integer.valueOf(v);
							break;
						}
					}

					if (gImmutable instanceof IntGraph)
						assertThrows(UnsupportedOperationException.class, () -> ((IntGraph) gImmutable).addVertex());
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(nonExistingVertex));

					Integer vertexToRemove = gImmutable.vertices().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeVertex(vertexToRemove));
				}
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					Iterator<Integer> vit = gImmutable.vertices().iterator();
					Integer u = vit.next();
					Integer v = vit.next();

					Integer nonExistingEdge;
					for (int e = 0;; e++) {
						if (!gImmutable.edges().contains(Integer.valueOf(e))) {
							nonExistingEdge = Integer.valueOf(e);
							break;
						}
					}

					if (gImmutable instanceof IntGraph)
						assertThrows(UnsupportedOperationException.class,
								() -> ((IntGraph) gImmutable).addEdge(u.intValue(), v.intValue()));
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdge(u, v, nonExistingEdge));

					Integer edgeToRemove = gImmutable.edges().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdge(edgeToRemove));
				}
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					for (Integer u : gImmutable.vertices()) {
						EdgeSet<Integer, Integer> edges = gImmutable.outEdges(u);
						assertEquals(gOrig.outEdges(u).size(), edges.size());
						assertEquals(gOrig.outEdges(u), edges);

						Set<Integer> iteratedEdges = new IntOpenHashSet();
						for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
							Integer peekNext = eit.peekNext();
							Integer e = eit.next();
							assertEquals(e, peekNext);

							assertEquals(u, eit.source());
							assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
							assertEquals(gImmutable.edgeEndpoint(e, u), eit.target());
							assertEquals(u, gImmutable.edgeEndpoint(e, eit.target()));

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
					for (Integer v : gImmutable.vertices()) {
						EdgeSet<Integer, Integer> edges = gImmutable.inEdges(v);
						assertEquals(gOrig.inEdges(v).size(), edges.size());
						assertEquals(gOrig.inEdges(v), edges);

						Set<Integer> iteratedEdges = new IntOpenHashSet();
						for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
							Integer peekNext = eit.peekNext();
							Integer e = eit.next();
							assertEquals(e, peekNext);

							assertEquals(v, eit.target());
							assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
							assertEquals(gImmutable.edgeEndpoint(e, v), eit.source());
							assertEquals(v, gImmutable.edgeEndpoint(e, eit.source()));

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
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					for (Integer u : gImmutable.vertices()) {
						for (Integer v : gImmutable.vertices()) {
							EdgeSet<Integer, Integer> edges = gImmutable.getEdges(u, v);
							assertEquals(gOrig.getEdges(u, v).size(), edges.size());
							assertEquals(gOrig.getEdges(u, v), edges);

							if (edges.isEmpty()) {
								assertNull(gImmutable.getEdge(u, v));
							} else {
								Integer e = gImmutable.getEdge(u, v);
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
								assertEquals(u, gImmutable.edgeEndpoint(e, v));
								assertEquals(v, gImmutable.edgeEndpoint(e, u));
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
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
					Integer v = gImmutable.vertices().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdgesOf(v));
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeOutEdgesOf(v));
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeInEdgesOf(v));
				}
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
					Integer e = gImmutable.edges().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.reverseEdge(e));
				}
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
					for (Integer e : gImmutable.edges()) {
						assertEquals(gOrig.edgeSource(e), gImmutable.edgeSource(e));
						assertEquals(gOrig.edgeTarget(e), gImmutable.edgeTarget(e));
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
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.clear());
					assertThrows(UnsupportedOperationException.class, () -> gImmutable.clearEdges());
				}
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					assertEquals(gOrig.getVerticesWeightsKeys(), gImmutable.getVerticesWeightsKeys());
					WeightsInt<Integer> wOrig = gOrig.getVerticesWeights(VerticesWeightsKey);
					WeightsInt<Integer> wImmutable = gImmutable.getVerticesWeights(VerticesWeightsKey);

					for (Integer v : gImmutable.vertices())
						assertEquals(wOrig.get(v), wImmutable.get(v));
					assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

					Integer vertex = gImmutable.vertices().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(vertex, 42));
					assertThrows(UnsupportedOperationException.class,
							() -> gImmutable.removeVerticesWeights(VerticesWeightsKey));
					assertThrows(UnsupportedOperationException.class,
							() -> gImmutable.addVerticesWeights("key", Object.class));
				}
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					assertEquals(gOrig.getEdgesWeightsKeys(), gImmutable.getEdgesWeightsKeys());
					WeightsInt<Integer> wOrig = gOrig.getEdgesWeights(EdgesWeightsKey);
					WeightsInt<Integer> wImmutable = gImmutable.getEdgesWeights(EdgesWeightsKey);

					for (Integer e : gImmutable.edges())
						assertEquals(wOrig.get(e), wImmutable.get(e));
					assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

					Integer edge = gImmutable.edges().iterator().next();
					assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(edge, 42));
					assertThrows(UnsupportedOperationException.class,
							() -> gImmutable.removeEdgesWeights(EdgesWeightsKey));
					assertThrows(UnsupportedOperationException.class,
							() -> gImmutable.addEdgesWeights("key", Object.class));
				}
			}
		}
	}

	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
				Graph<Integer, Integer> gImmutable0 = gOrig0.immutableView();
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

					assertEqualsBool(gOrig.isAllowParallelEdges(), gImmutable.isAllowParallelEdges());
					assertEqualsBool(gOrig.isAllowSelfEdges(), gImmutable.isAllowSelfEdges());
					assertEqualsBool(gOrig.isDirected(), gImmutable.isDirected());
				}
			}
		}
	}

	@Test
	public void testImmutableViewOfImmutableView() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = gOrig.immutableView();

					assertTrue(gImmutable == gImmutable.immutableView());
				}
			}
		}
	}

	@Test
	public void testEquals() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = gOrig.immutableView();

					assertTrue(gImmutable.equals(gImmutable));
					assertTrue(gImmutable.equals(gOrig));
					assertTrue(gOrig.equals(gImmutable));

					assertFalse(gImmutable.equals(null));
					assertFalse(gImmutable.equals(new Object()));
				}
			}
		}
	}

	@Test
	public void testHashCode() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
					Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
					Graph<Integer, Integer> gImmutable = gOrig.immutableView();

					assertEquals(gImmutable.hashCode(), gOrig.hashCode());
					assertEquals(gImmutable.hashCode(), gOrig.immutableView().hashCode());
				}
			}
		}
	}

}
