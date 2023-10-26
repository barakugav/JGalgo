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
package com.jgalgo.alg;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Hopcroft-Tarjan algorithm for bi-connected components.
 * <p>
 * The algorithm performs a DFS and look for edges from vertices with greater depth to vertices with lower depth,
 * indicating for to separates paths between them: one using the DFS tree, and the other using the edge. The algorithm
 * runs in linear time and space.
 * <p>
 * Based on 'Algorithm 447: efficient algorithms for graph manipulation' by Hopcroft, J. and Tarjan, R. 1973.
 *
 * @author Barak Ugav
 */
class BiConnectedComponentsAlgoHopcroftTarjan extends BiConnectedComponentsAlgoAbstract {

	@Override
	BiConnectedComponentsAlgo.Result findBiConnectedComponents(IndexGraph g) {
		Assertions.Graphs.onlyUndirected(g);

		final int n = g.vertices().size();
		int[] depths = new int[n];
		int[] edgePath = new int[n];
		int[] lowpoint = new int[n];
		EdgeIter[] edgeIters = new EdgeIter[n];
		// TODO DFS stack class

		IntArrayList edgeStack = new IntArrayList();

		Arrays.fill(depths, -1);

		var biccVerticesFromBiccEdgesState = new Object() {
			BitSet visited = new BitSet(n);
		};
		Function<int[], int[]> biccVerticesFromBiccEdges = biccsEdges -> {
			BitSet visited = biccVerticesFromBiccEdgesState.visited;
			assert visited.isEmpty();
			int biccVerticesCount = 0;
			for (int e : biccsEdges) {
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) }) {
					if (!visited.get(w)) {
						visited.set(w);
						biccVerticesCount++;
					}
				}
			}
			for (int e : biccsEdges)
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) })
					visited.clear(w);
			int[] biccVertices = new int[biccVerticesCount];
			biccVerticesCount = 0;
			for (int e : biccsEdges) {
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) }) {
					if (!visited.get(w)) {
						visited.set(w);
						biccVertices[biccVerticesCount++] = w;
					}
				}
			}
			assert biccVertices.length == biccVerticesCount;
			for (int w : biccVertices)
				visited.clear(w);
			return biccVertices;
		};

		// BitSet separatingVertex = new BitSet(n);
		List<Pair<int[], int[]>> biccs = new ObjectArrayList<>();
		IntList biccEdgesTemp = new IntArrayList();

		for (int root = 0; root < n; root++) {
			if (depths[root] != -1)
				continue;
			int depth = 0;
			depths[root] = depth;
			lowpoint[depth] = depth;
			edgeIters[depth] = g.outEdges(root).iterator();

			int rootChildrenCount = 0;

			dfs: for (int u = root;;) {
				final int parent = depth == 0 ? -1 : g.edgeEndpoint(edgePath[depth - 1], u);

				for (EdgeIter eit = edgeIters[depth]; eit.hasNext();) {
					int e = eit.nextInt();

					int v = eit.target();
					final int vDepth = depths[v];
					if (vDepth == -1) { /* unvisited */
						edgeStack.push(e);
						edgePath[depth] = e;
						depth++;
						depths[v] = depth;
						lowpoint[depth] = depth;
						edgeIters[depth] = g.outEdges(v).iterator();
						u = v;
						continue dfs;

					} else if (v != parent && vDepth < depth) {
						edgeStack.push(e);
						lowpoint[depth] = Math.min(lowpoint[depth], vDepth);
					}
				}

				if (depth > 0) {
					depth--;
					if (lowpoint[depth + 1] >= depth) {
						// separatingVertex.set(parent);

						assert biccEdgesTemp.isEmpty();
						for (int lastEdge = edgePath[depth]; !edgeStack.isEmpty();) {
							int e = edgeStack.popInt();
							biccEdgesTemp.add(e);
							if (e == lastEdge)
								break;
						}
						assert !biccEdgesTemp.isEmpty();
						int[] biccEdges = biccEdgesTemp.toIntArray();
						int[] biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						biccs.add(Pair.of(biccVertices, biccEdges));
						biccEdgesTemp.clear();
					} else {
						lowpoint[depth] = Math.min(lowpoint[depth], lowpoint[depth + 1]);
					}

					if (depth == 0)
						rootChildrenCount++;
					u = parent;

				} else {
					assert u == root;
					// if (rootChildrenCount > 1)
					// separatingVertex.set(root);

					if (rootChildrenCount == 0) {
						biccs.add(Pair.of(new int[] { root }, IntArrays.DEFAULT_EMPTY_ARRAY));

					} else if (!edgeStack.isEmpty()) {
						int[] biccEdges = edgeStack.toIntArray();
						int[] biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						edgeStack.clear();
						biccs.add(Pair.of(biccVertices, biccEdges));

					}
					break;
				}
			}
		}

		return new Res(g, biccs);
	}

	private static class Res implements BiConnectedComponentsAlgo.Result {

		private final IndexGraph g;
		private final IntSet[] biccsVertices;
		private final int[][] biccsEdgesFromAlgo;
		private IntSet[] biccsEdges;
		private IntSet[] vertexBiCcs;
		// private final BitSet separatingVerticesBitmap;
		// private IntList separatingVertices;

		Res(IndexGraph g, List<Pair<int[], int[]>> biccs) {
			this.g = Objects.requireNonNull(g);
			final int biccsNum = biccs.size();

			biccsVertices = new IntSet[biccsNum];
			for (int biccIdx = 0; biccIdx < biccsNum; biccIdx++)
				biccsVertices[biccIdx] = ImmutableIntArraySet.withNaiveContains(biccs.get(biccIdx).first());

			biccsEdgesFromAlgo = new int[biccsNum][];
			for (int biccIdx = 0; biccIdx < biccsNum; biccIdx++)
				biccsEdgesFromAlgo[biccIdx] = biccs.get(biccIdx).second();
			// this.separatingVerticesBitmap = Objects.requireNonNull(separatingVerticesBitmap);
		}

		@Override
		public IntSet getVertexBiCcs(int vertex) {
			if (vertexBiCcs == null) {
				final int n = g.vertices().size();

				int[] vertexBiCcsCount = new int[n + 1];
				for (int biccIdx = 0; biccIdx < biccsVertices.length; biccIdx++)
					for (int v : biccsVertices[biccIdx])
						vertexBiCcsCount[v]++;
				int vertexBiCcsCountTotal = 0;
				for (int v = 0; v < n; v++)
					vertexBiCcsCountTotal += vertexBiCcsCount[v];

				int[] sortedBiccs = new int[vertexBiCcsCountTotal];
				int[] vertexOffset = vertexBiCcsCount;
				for (int s = 0, v = 0; v < n; v++) {
					int k = vertexOffset[v];
					vertexOffset[v] = s;
					s += k;
				}
				for (int biccIdx = 0; biccIdx < biccsVertices.length; biccIdx++)
					for (int v : biccsVertices[biccIdx])
						sortedBiccs[vertexOffset[v]++] = biccIdx;
				for (int v = n; v > 0; v--)
					vertexOffset[v] = vertexOffset[v - 1];
				vertexOffset[0] = 0;

				vertexBiCcs = new IntSet[n];
				for (int v = 0; v < n; v++)
					vertexBiCcs[v] =
							ImmutableIntArraySet.withNaiveContains(sortedBiccs, vertexOffset[v], vertexOffset[v + 1]);
			}
			return vertexBiCcs[vertex];
		}

		@Override
		public int getNumberOfBiCcs() {
			return biccsVertices.length;
		}

		@Override
		public IntSet getBiCcVertices(int biccIdx) {
			return biccsVertices[biccIdx];
		}

		@Override
		public IntSet getBiCcEdges(int biccIdx) {
			if (biccsEdges == null) {
				final int biccsNum = getNumberOfBiCcs();
				biccsEdges = new IntSet[biccsNum];

				if (!g.isAllowParallelEdges() && !g.isAllowSelfEdges()) {
					for (int b = 0; b < biccsNum; b++)
						biccsEdges[b] = ImmutableIntArraySet.withNaiveContains(biccsEdgesFromAlgo[b]);

				} else {
					/*
					 * in case parallel edges exists in the graph, we may need to manually add them to the Bi-comp edges
					 * collection, as they will not be added by the main algorithm.
					 */
					int[] biccExtraEdgesCount = new int[biccsNum];
					final int n = g.vertices().size();
					final int m = g.edges().size();

					int[] edge2bicc = new int[m];
					Arrays.fill(edge2bicc, -1);
					for (int b = 0; b < biccsNum; b++) {
						for (int e : biccsEdgesFromAlgo[b]) {
							assert edge2bicc[e] == -1;
							edge2bicc[e] = b;
						}
					}

					/* Search for parallel edges, which may not be included in the edges list by the main algorithm */
					int[] extraEdgesBiccs = null;
					if (g.isAllowParallelEdges()) {
						int[] target2bicc = new int[n];
						Arrays.fill(target2bicc, -1);
						extraEdgesBiccs = new int[m];
						Arrays.fill(extraEdgesBiccs, -1);
						for (int u = 0; u < n; u++) {
							for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.target();
								if (u == v)
									continue;
								int b = edge2bicc[e];
								if (b != -1)
									target2bicc[v] = b;
							}
							for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.target();
								if (u == v)
									continue;
								int b = edge2bicc[e];
								if (b == -1) {
									b = target2bicc[v];
									edge2bicc[e] = b;
									assert extraEdgesBiccs[e] == -1;
									extraEdgesBiccs[e] = b;
									biccExtraEdgesCount[b]++;
								}
							}
							for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								eit.nextInt();
								target2bicc[eit.target()] = -1;
							}
						}
					}

					/* search for self edges, which are not added to any bicc */
					if (g.isAllowSelfEdges()) {
						assert g.edges().intStream().filter(e -> g.edgeSource(e) == g.edgeTarget(e))
								.allMatch(e -> edge2bicc[e] == -1);
						for (int u = 0; u < n; u++) {
							for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								eit.nextInt();
								int v = eit.target();
								if (u != v)
									continue;
								for (int b : getVertexBiCcs(u))
									biccExtraEdgesCount[b]++;
							}
						}
					}

					for (int b = 0; b < biccsNum; b++) {
						if (biccExtraEdgesCount[b] == 0)
							continue;
						int[] biccEdges = biccsEdgesFromAlgo[b];
						int oldLength = biccEdges.length;
						biccEdges = Arrays.copyOf(biccEdges, oldLength + biccExtraEdgesCount[b]);
						biccsEdgesFromAlgo[b] = biccEdges;
						biccExtraEdgesCount[b] = oldLength;
					}

					/* add parallel edges */
					if (g.isAllowParallelEdges()) {
						for (int b, e = 0; e < m; e++)
							if ((b = extraEdgesBiccs[e]) != -1)
								biccsEdgesFromAlgo[b][biccExtraEdgesCount[b]++] = e;
					}

					/* add self edges */
					if (g.isAllowSelfEdges()) {
						for (int u = 0; u < n; u++) {
							for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.target();
								if (u != v)
									continue;
								for (int b : getVertexBiCcs(u))
									biccsEdgesFromAlgo[b][biccExtraEdgesCount[b]++] = e;
							}
						}
					}

					for (int b = 0; b < biccsNum; b++) {
						biccsEdges[b] = ImmutableIntArraySet.withNaiveContains(biccsEdgesFromAlgo[b]);
						biccsEdgesFromAlgo[b] = null;
					}
				}
			}
			return biccsEdges[biccIdx];
		}

		// @Override
		// public IntCollection separatingVertices() {
		// if (separatingVertices == null) {
		// separatingVertices = new IntArrayList(separatingVerticesBitmap.cardinality());
		// for (int v : Utils.iterable(separatingVerticesBitmap))
		// separatingVertices.add(v);
		// separatingVertices = IntLists.unmodifiable(separatingVertices);
		// }
		// return separatingVertices;
		// }

		// @Override
		// public boolean isSeparatingVertex(int vertex) {
		// if (!g.vertices().contains(vertex))
		// throw new IndexOutOfBoundsException(vertex);
		// return separatingVerticesBitmap.get(vertex);
		// }

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder().append('[');
			final int biccNum = getNumberOfBiCcs();
			for (int biccIdx = 0; biccIdx < biccNum; biccIdx++) {
				if (biccIdx > 0)
					s.append(", ");
				s.append(getBiCcVertices(biccIdx).toString());
			}
			return s.append(']').toString();
		}

	}
}