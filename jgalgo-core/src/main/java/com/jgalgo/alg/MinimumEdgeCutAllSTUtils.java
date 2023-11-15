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

import java.util.Iterator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.JGAlgoUtils;

class MinimumEdgeCutAllSTUtils {

	private MinimumEdgeCutAllSTUtils() {}

	abstract static class AbstractImpl implements MinimumEdgeCutAllST {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <V, E> Iterator<VertexBiPartition<V, E>> computeAllMinimumCuts(Graph<V, E> g, WeightFunction<E> w,
				V source, V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue();
				int sink0 = ((Integer) sink).intValue();
				return (Iterator) computeAllMinimumCuts((IndexGraph) g, w0, source0, sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				Iterator<IVertexBiPartition> indexIter = computeAllMinimumCuts(iGraph, iw, iSource, iSink);
				return JGAlgoUtils.iterMap(indexIter,
						iPartition -> VertexBiPartitions.partitionFromIndexPartition(g, iPartition));
			}
		}

		abstract Iterator<IVertexBiPartition> computeAllMinimumCuts(IndexGraph g, IWeightFunction w, int source,
				int sink);

	}

}
