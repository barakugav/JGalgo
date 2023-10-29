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

import java.util.ArrayList;
import java.util.List;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;

class KShortestPathsSTs {

	static abstract class AbstractImpl implements KShortestPathsST {

		@Override
		public List<Path> computeKShortestPaths(IntGraph g, IWeightFunction w, int source, int target, int k) {
			if (g instanceof IndexGraph)
				return computeKShortestPaths((IndexGraph) g, w, source, target, k);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			List<Path> indexResult = computeKShortestPaths(iGraph, iw, iSource, iTarget, k);
			List<Path> result = new ArrayList<>(indexResult.size());
			for (Path p : indexResult)
				result.add(PathImpl.pathFromIndexPath(p, viMap, eiMap));
			return result;
		}

		abstract List<Path> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k);

	}

}
