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

import static com.jgalgo.internal.util.Range.range;
import java.util.Optional;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.BinarySearch;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;

class GraphCsrDirectedReindexed extends GraphCsrBase {

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	final boolean fastLookup;
	private final Int2IntMap[] edgesLookupTable;

	private GraphCsrDirectedReindexed(Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder,
			BuilderProcessEdgesDirected processEdges, Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing,
			boolean copyVerticesWeights, boolean copyEdgesWeights, boolean fastLookup) {
		super(true, graphOrBuilder, processEdges, edgesReIndexing, copyVerticesWeights, copyEdgesWeights);
		final int n = verticesNum(graphOrBuilder);
		final int m = edgesNum(graphOrBuilder);

		Optional<GraphCsrDirectedReindexed> csrGraph = graphOrBuilder.contains(IndexGraph.class)
				&& graphOrBuilder.get(IndexGraph.class) instanceof GraphCsrDirectedReindexed
						? Optional.of((GraphCsrDirectedReindexed) graphOrBuilder.get(IndexGraph.class))
						: Optional.empty();

		if (csrGraph.isPresent()) {
			assert edgesReIndexing.isEmpty();
			edgesIn = csrGraph.get().edgesIn;
			edgesInBegin = csrGraph.get().edgesInBegin;

		} else {
			edgesIn = processEdges.edgesIn;
			edgesInBegin = processEdges.edgesInBegin;
			assert processEdges.edgesOut.length == m;
			assert edgesIn.length == m;
			assert edgesInBegin.length == n + 1;

			IndexGraphBuilder.ReIndexingMap edgesReIndexing0 = edgesReIndexing.get();
			for (int eIdx : range(m)) {
				int eOrig = edgesIn[eIdx];
				int eCsr = edgesReIndexing0.origToReIndexed(eOrig);
				edgesIn[eIdx] = eCsr;
			}

			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class);
				assert g.isDirected();

				for (int eCsr : range(m)) {
					int eOrig = edgesReIndexing0.reIndexedToOrig(eCsr);
					setEndpoints(eCsr, g.edgeSource(eOrig), g.edgeTarget(eOrig));
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
				assert builder.isDirected();

				for (int eCsr : range(m)) {
					int eOrig = edgesReIndexing0.reIndexedToOrig(eCsr);
					setEndpoints(eCsr, builder.edgeSource(eOrig), builder.edgeTarget(eOrig));
				}
			}
		}

		this.fastLookup = fastLookup;
		if (!fastLookup) {
			edgesLookupTable = null;

		} else if (csrGraph.isPresent() && csrGraph.get().fastLookup) {
			edgesLookupTable = csrGraph.get().edgesLookupTable;

		} else {
			edgesLookupTable = new Int2IntMap[vertices().size()];
			initLookupTables();
		}
	}

	private void initLookupTables() {
		final int n = vertices().size();
		for (int u : range(n)) {
			int eBegin = edgesOutBegin[u];
			int eEnd = edgesOutBegin[u + 1];
			if (eBegin == eEnd) {
				edgesLookupTable[u] = EmptyEdgeMap;
				continue;
			}
			int uniqueTargets = 0;
			for (int lastTarget = -1, e = eBegin; e < eEnd; e++) {
				int v = target(e);
				assert lastTarget <= v;
				if (v != lastTarget) {
					lastTarget = v;
					uniqueTargets++;
				}
			}
			edgesLookupTable[u] = new Int2IntOpenHashMap(uniqueTargets);
			edgesLookupTable[u].defaultReturnValue(-1);
			for (int lastTarget = -1, e = eBegin; e < eEnd; e++) {
				int v = target(e);
				if (v == lastTarget)
					continue; /* store in the map the first edge only of same target */
				int oldEdge = edgesLookupTable[u].put(v, e);
				assert oldEdge < 0;
				lastTarget = v;
			}
		}
	}

	@Override
	boolean containsParallelEdgesImpl() {
		for (int u : range(vertices().size())) {
			int eBegin = edgesOutBegin[u];
			int eEnd = edgesOutBegin[u + 1];
			for (int lastTarget = -1, e = eBegin; e < eEnd; e++) {
				int v = target(e);
				assert lastTarget <= v;
				if (v == lastTarget)
					return true;
				lastTarget = v;
			}
		}
		return false;
	}

	static IndexGraphBuilder.ReIndexedGraph newInstance(IndexGraphBuilderImpl builder, boolean fastLookup) {
		return newInstance(Variant2.ofB(builder), true, true, fastLookup);
	}

	static IndexGraphBuilder.ReIndexedGraph newInstance(IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights, boolean fastLookup) {
		return newInstance(Variant2.ofA(g), copyVerticesWeights, copyEdgesWeights, fastLookup);
	}

	private static IndexGraphBuilder.ReIndexedGraph newInstance(
			Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder, boolean copyVerticesWeights,
			boolean copyEdgesWeights, boolean fastLookup) {
		GraphCsrBase.BuilderProcessEdgesDirected processEdges =
				GraphCsrBase.BuilderProcessEdgesDirected.valueOf(graphOrBuilder);

		Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing = Optional.empty();
		if (!graphOrBuilder.contains(IndexGraph.class)
				|| !(graphOrBuilder.get(IndexGraph.class) instanceof GraphCsrDirectedReindexed)) {
			final int m = edgesNum(graphOrBuilder);
			int[] edgesCsrToOrig = processEdges.edgesOut;
			int[] edgesOrigToCsr = new int[m];
			for (int eCsr : range(m))
				edgesOrigToCsr[edgesCsrToOrig[eCsr]] = eCsr;
			edgesReIndexing = Optional.of(new IndexGraphBuilderImpl.ReIndexingMapImpl(edgesOrigToCsr, edgesCsrToOrig));
		}

		GraphCsrDirectedReindexed g = new GraphCsrDirectedReindexed(graphOrBuilder, processEdges, edgesReIndexing,
				copyVerticesWeights, copyEdgesWeights, fastLookup);
		return new IndexGraphBuilderImpl.ReIndexedGraphImpl(g, Optional.empty(), edgesReIndexing);
	}

	@Override
	public int getEdge(int source, int target) {
		if (fastLookup) {
			checkVertex(source);
			checkVertex(target);
			return edgesLookupTable[source].get(target);
		} else {

			checkVertex(source);
			int eBegin = edgesOutBegin[source], eEnd = edgesOutBegin[source + 1];
			int e = BinarySearch.lowerBound(eBegin, eEnd, target, this::edgeTarget);
			if (e < eEnd && target(e) == target) {
				return e;
			} else {
				checkVertex(target);
				return -1;
			}
		}
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		if (fastLookup) {
			checkVertex(source);
			checkVertex(target);
			return new SourceTargetEdgesSetFastLookup(source, target);

		} else {
			checkVertex(source);
			int eBegin = edgesOutBegin[source], eEnd = edgesOutBegin[source + 1];
			IntIntPair edgeRange = BinarySearch.equalRange(eBegin, eEnd, target, this::edgeTarget);
			if (edgeRange == null) {
				checkVertex(target);
				edgeRange = IntIntPair.of(eBegin, eBegin);
			}
			return new EdgeSetSourceTarget(source, target, edgeRange.firstInt(), edgeRange.secondInt());
		}
	}

	@Override
	public IEdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {

		final int begin, end;

		EdgeSetOut(int source) {
			super(source);
			begin = edgesOutBegin[source];
			end = edgesOutBegin[source + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, begin, end);
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {

		final int begin, end;

		EdgeSetIn(int target) {
			super(target);
			begin = edgesInBegin[target];
			end = edgesInBegin[target + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn, begin, end);
		}
	}

	private class EdgeIterOut implements IEdgeIter {
		private final int source;
		int nextEdge;
		private final int endIdx;

		EdgeIterOut(int source, int beginEdge, int endEdge) {
			this.source = source;
			this.nextEdge = beginEdge;
			this.endIdx = endEdge;
		}

		@Override
		public boolean hasNext() {
			return nextEdge < endIdx;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			return nextEdge++;
		}

		@Override
		public int peekNextInt() {
			Assertions.hasNext(this);
			return nextEdge;
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			int lastEdge = nextEdge - 1; // undefined behavior if nextInt() wasn't called
			return GraphCsrDirectedReindexed.this.target(lastEdge);
		}
	}

	private class EdgeIterIn extends EdgeIterAbstract {
		private final int target;

		EdgeIterIn(int target, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.target = target;
		}

		@Override
		public int sourceInt() {
			return GraphCsrDirectedReindexed.this.source(lastEdge);
		}

		@Override
		public int targetInt() {
			return target;
		}
	}

	private class EdgeSetSourceTarget extends AbstractIntSet implements IEdgeSet {

		private final int source;
		private final int target;
		private final int begin;
		private final int end;

		EdgeSetSourceTarget(int source, int target, int start, int end) {
			this.source = source;
			this.target = target;
			this.begin = start;
			this.end = end;
		}

		@Override
		public boolean contains(int edge) {
			return edges.contains(edge) && source == edgeSource(edge) && target == edgeTarget(edge);
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, begin, end);
		}
	}

	private class SourceTargetEdgesSetFastLookup extends AbstractIntSet implements IEdgeSet {

		private final int source, target;

		SourceTargetEdgesSetFastLookup(int source, int target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public int size() {
			int firstEdge = edgesLookupTable[source].get(target);
			if (firstEdge < 0)
				return 0;
			for (int e = firstEdge, m = edgesOutBegin[source + 1];;) {
				e++;
				if (e == m || target(e) != target)
					return e - firstEdge;
			}
		}

		@Override
		public boolean isEmpty() {
			return edgesLookupTable[source].get(target) < 0;
		}

		@Override
		public boolean contains(int key) {
			return edges().contains(key) && source(key) == source && target(key) == target;
		}

		@Override
		public IEdgeIter iterator() {
			return new SourceTargetEdgesIterFastLookup(source, target);
		}
	}

	private class SourceTargetEdgesIterFastLookup implements IEdgeIter {

		private final int source, target;
		private final int sourceEnd;
		private int edge;

		SourceTargetEdgesIterFastLookup(int source, int target) {
			this.source = source;
			this.target = target;
			this.edge = edgesLookupTable[source].get(target);
			sourceEnd = edgesOutBegin[source + 1];
		}

		@Override
		public boolean hasNext() {
			return edge >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			int e = edge;
			edge++;
			if (edge == sourceEnd || GraphCsrDirectedReindexed.this.target(edge) != target)
				edge = -1;
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.hasNext(this);
			return edge;
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			return target;
		}
	}
}
