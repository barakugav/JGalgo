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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;

class FlowCirculations {

	static abstract class AbstractImpl implements FlowCirculation {

		@Override
		public void computeCirculation(Graph g, FlowNetwork net, WeightFunction supply) {
			if (g instanceof IndexGraph) {
				computeCirculation((IndexGraph) g, net, supply);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);

			computeCirculation(iGraph, iNet, iSupply);
		}

		abstract void computeCirculation(IndexGraph g, FlowNetwork net, WeightFunction supply);

	}

}