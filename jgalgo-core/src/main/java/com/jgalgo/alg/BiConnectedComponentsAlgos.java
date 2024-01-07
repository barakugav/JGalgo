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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntSet;

class BiConnectedComponentsAlgos {

	private BiConnectedComponentsAlgos() {}

	static class IndexResult implements BiConnectedComponentsAlgo.IResult {

		private final IndexGraph g;
		private final IntSet[] biccsVertices;
		private final int[][] biccsEdgesFromAlgo;
		private IntSet[] biccsEdges;
		private IntSet[] vertexBiCcs;
		private Bitmap cutVerticesBitmap;
		private IntSet cutVertices;
		private IntGraph blockGraph;

		IndexResult(IndexGraph g, List<Pair<int[], int[]>> biccs) {
			this.g = Objects.requireNonNull(g);
			final int biccsNum = biccs.size();

			biccsVertices = new IntSet[biccsNum];
			for (int biccIdx = 0; biccIdx < biccsNum; biccIdx++)
				biccsVertices[biccIdx] = ImmutableIntArraySet.withNaiveContains(biccs.get(biccIdx).first());

			biccsEdgesFromAlgo = new int[biccsNum][];
			for (int biccIdx = 0; biccIdx < biccsNum; biccIdx++)
				biccsEdgesFromAlgo[biccIdx] = biccs.get(biccIdx).second();
		}

		@Override
		public IntSet getVertexBiCcs(int vertex) {
			final int n = g.vertices().size();
			if (vertexBiCcs == null) {
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
			Assertions.Graphs.checkVertex(vertex, n);
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
							for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.targetInt();
								if (u == v)
									continue;
								int b = edge2bicc[e];
								if (b != -1)
									target2bicc[v] = b;
							}
							for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.targetInt();
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
							for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								eit.nextInt();
								target2bicc[eit.targetInt()] = -1;
							}
						}
					}

					/* search for self edges, which are not added to any bicc */
					if (g.isAllowSelfEdges()) {
						assert Graphs.selfEdges(g).intStream().allMatch(e -> edge2bicc[e] == -1);
						for (int u = 0; u < n; u++) {
							for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								eit.nextInt();
								int v = eit.targetInt();
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
							for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
								int e = eit.nextInt();
								int v = eit.targetInt();
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

		@Override
		public String toString() {
			return range(getNumberOfBiCcs())
					.mapToObj(this::getBiCcVertices)
					.map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
		}

		@Override
		public boolean isCutVertex(int vertex) {
			computeCutVerticesBitmap();
			Assertions.Graphs.checkVertex(vertex, g.vertices().size());
			return cutVerticesBitmap.get(vertex);
		}

		private void computeCutVerticesBitmap() {
			if (cutVerticesBitmap == null)
				cutVerticesBitmap = Bitmap.fromPredicate(g.vertices().size(), v -> getVertexBiCcs(v).size() > 1);
		}

		@Override
		public IntSet getCutVertices() {
			if (cutVertices == null) {
				computeCutVerticesBitmap();
				cutVertices = ImmutableIntArraySet.ofBitmap(cutVerticesBitmap);
			}
			return cutVertices;
		}

		@Override
		public IntGraph getBlockGraph() {
			if (blockGraph == null) {
				final int blockNum = getNumberOfBiCcs();
				IntGraphBuilder g = IntGraphBuilder.undirected();
				g.ensureVertexCapacity(blockNum);
				g.addVertices(range(blockNum));
				for (int cutVertex : getCutVertices())
					for (int b1 : getVertexBiCcs(cutVertex))
						for (int b2 : getVertexBiCcs(cutVertex))
							if (b1 != b2)
								g.addEdge(b1, b2);
				blockGraph = g.build();
			}
			return blockGraph;
		}

	}

	static class IntResultFromIndexResult implements BiConnectedComponentsAlgo.IResult {

		private final BiConnectedComponentsAlgo.IResult indexRes;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, BiConnectedComponentsAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet getVertexBiCcs(int vertex) {
			return indexRes.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return indexRes.getNumberOfBiCcs();
		}

		@Override
		public IntSet getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public IntSet getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(int vertex) {
			return indexRes.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public IntSet getCutVertices() {
			return IndexIdMaps.indexToIdSet(indexRes.getCutVertices(), viMap);
		}

		@Override
		public IntGraph getBlockGraph() {
			return indexRes.getBlockGraph();
		}
	}

	static class ObjResultFromIndexResult<V, E> implements BiConnectedComponentsAlgo.Result<V, E> {

		private final BiConnectedComponentsAlgo.IResult indexRes;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, BiConnectedComponentsAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet getVertexBiCcs(V vertex) {
			return indexRes.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return indexRes.getNumberOfBiCcs();
		}

		@Override
		public Set<V> getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public Set<E> getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(V vertex) {
			return indexRes.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public Set<V> getCutVertices() {
			return IndexIdMaps.indexToIdSet(indexRes.getCutVertices(), viMap);
		}

		@Override
		public IntGraph getBlockGraph() {
			return indexRes.getBlockGraph();
		}
	}

}
