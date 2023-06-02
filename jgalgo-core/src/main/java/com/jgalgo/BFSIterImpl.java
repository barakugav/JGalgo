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
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class BFSIterImpl implements BFSIter {

	private final IndexGraph g;
	private final BitSet visited;
	private final IntPriorityQueue queue;
	private int inEdge;
	private int layer;
	private int firstVInLayer;

	/**
	 * Create a BFS iterator rooted at a single source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from
	 */
	BFSIterImpl(IndexGraph g, int source) {
		this(g, IntIterators.singleton(source));
	}

	/**
	 * Create a BFS iterator rooted at multiple sources vertices.
	 *
	 * @param  g                        a graph
	 * @param  sources                  multiple sources vertices in the graph from which the search will start from
	 * @throws IllegalArgumentException if the sources iterator is empty
	 */
	private BFSIterImpl(IndexGraph g, IntIterator sources) {
		if (!sources.hasNext())
			throw new IllegalArgumentException("no sources provided");
		this.g = g;
		int n = g.vertices().size();
		visited = new BitSet(n);
		queue = new IntArrayFIFOQueue();
		inEdge = -1;
		layer = -1;

		firstVInLayer = -1;
		while (sources.hasNext()) {
			int source = sources.nextInt();
			visited.set(source);
			queue.enqueue(source);
			queue.enqueue(-1);
			if (firstVInLayer == -1)
				firstVInLayer = source;
		}
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		final int u = queue.dequeueInt();
		inEdge = queue.dequeueInt();
		if (u == firstVInLayer) {
			layer++;
			firstVInLayer = -1;
		}

		for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			int v = eit.target();
			if (visited.get(v))
				continue;
			visited.set(v);
			queue.enqueue(v);
			queue.enqueue(e);
			if (firstVInLayer == -1)
				firstVInLayer = v;
		}

		return u;
	}

	@Override
	public int inEdge() {
		return inEdge;
	}

	@Override
	public int layer() {
		return layer;
	}

	static class BFSFromIndexBFS implements BFSIter {

		private final BFSIter it;
		private final IndexGraphMap viMap;
		private final IndexGraphMap eiMap;

		BFSFromIndexBFS(BFSIter it, IndexGraphMap viMap, IndexGraphMap eiMap) {
			this.it = Objects.requireNonNull(it);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return viMap.indexToId(it.nextInt());
		}

		@Override
		public int inEdge() {
			int e = it.inEdge();
			return e == -1 ? -1 : eiMap.indexToId(e);
		}

		@Override
		public int layer() {
			return it.layer();
		}

	}
}
