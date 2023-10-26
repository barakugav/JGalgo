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

import com.jgalgo.internal.util.Assertions;

class GraphCSRUndirected extends GraphCSRAbstractUnindexed {

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(false, true, true);

	GraphCSRUndirected(IndexGraphBuilderImpl builder, BuilderProcessEdgesUndirected processEdges) {
		super(Capabilities, builder, processEdges);
	}

	GraphCSRUndirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);
		Assertions.Graphs.onlyUndirected(g);
	}

	@Override
	public EdgeSet outEdges(int source) {
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet inEdges(int target) {
		return new EdgeSetIn(target);
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {

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
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edgesOut, begin, end);
		}
	}

	class EdgeSetIn extends GraphBase.EdgeSetInUndirected {

		final int begin, end;

		EdgeSetIn(int target) {
			super(target);
			begin = edgesOutBegin[target];
			end = edgesOutBegin[target + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edgesOut, begin, end);
		}
	}

	private class EdgeIterOut extends EdgeIterAbstract {
		private final int source;

		EdgeIterOut(int source, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.source = source;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			return edgeEndpoint(lastEdge, source);
		}
	}

	private class EdgeIterIn extends EdgeIterAbstract {
		private final int target;

		EdgeIterIn(int target, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.target = target;
		}

		@Override
		public int source() {
			return edgeEndpoint(lastEdge, target);
		}

		@Override
		public int target() {
			return target;
		}
	}

}