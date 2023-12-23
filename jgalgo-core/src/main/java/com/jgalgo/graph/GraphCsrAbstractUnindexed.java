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

import java.util.Optional;

import com.jgalgo.internal.util.JGAlgoUtils.Variant2;

abstract class GraphCsrAbstractUnindexed extends GraphCsrBase {

	final int[] edgesOut;

	GraphCsrAbstractUnindexed(boolean directed, IndexGraphBuilderImpl builder, BuilderProcessEdges processEdges) {
		super(directed, Variant2.ofB(builder), processEdges, Optional.empty(), true, true);
		edgesOut = processEdges.edgesOut;

		for (int m = builder.edges.size(), e = 0; e < m; e++)
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));
	}

	GraphCsrAbstractUnindexed(boolean directed, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(directed, g, copyVerticesWeights, copyEdgesWeights);

		if (g instanceof GraphCsrAbstractUnindexed) {
			GraphCsrAbstractUnindexed gCsr = (GraphCsrAbstractUnindexed) g;
			edgesOut = gCsr.edgesOut;

		} else {
			final int n = g.vertices().size();
			final int m = g.edges().size();
			int edgesOutArrLen;
			if (isDirected()) {
				edgesOutArrLen = m;
			} else {
				edgesOutArrLen = 0;
				for (int u = 0; u < n; u++)
					edgesOutArrLen += g.outEdges(u).size();
			}
			edgesOut = new int[edgesOutArrLen];

			for (int eIdx = 0, u = 0; u < n; u++) {
				edgesOutBegin[u] = eIdx;
				for (int e : g.outEdges(u))
					edgesOut[eIdx++] = e;
			}
			edgesOutBegin[n] = edgesOutArrLen;
		}
	}

}
