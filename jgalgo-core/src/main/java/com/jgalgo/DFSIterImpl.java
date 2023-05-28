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
package com.jgalgo;

import java.util.BitSet;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class DFSIterImpl implements DFSIter {

	private final Graph g;
	private final BitSet visited;
	private final Stack<EdgeIter> edgeIters;
	private final IntArrayList edgePath;
	private final IntList edgePathView;
	private boolean isValid;

	/**
	 * Create a DFS iterator rooted at some source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from
	 */
	public DFSIterImpl(Graph g, int source) {
		int n = g.vertices().size();
		this.g = g;
		visited = new BitSet(n);
		edgeIters = new ObjectArrayList<>();
		edgePath = new IntArrayList();
		edgePathView = IntLists.unmodifiable(edgePath);

		visited.set(source);
		edgeIters.push(g.edgesOut(source));
		isValid = true;
	}

	@Override
	public boolean hasNext() {
		if (isValid)
			return true;
		if (edgeIters.isEmpty())
			return false;
		for (;;) {
			for (EdgeIter eit = edgeIters.top(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				edgeIters.push(g.edgesOut(v));
				edgePath.add(e);
				return isValid = true;
			}
			edgeIters.pop();
			if (edgeIters.isEmpty()) {
				assert edgePath.isEmpty();
				return false;
			}
			edgePath.popInt();
		}
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		int ret = edgeIters.top().source();
		isValid = false;
		return ret;
	}

	@Override
	public IntList edgePath() {
		return edgePathView;
	}
}