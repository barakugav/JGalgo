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

import com.jgalgo.Utils.BiInt2IntFunction;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

/**
 * The DSatur coloring algorithm.
 * <p>
 * The Saturation Degree (DSatur) coloring algorithm is a greedy algorithm, namely it examine the vertices in some order
 * and assign for each vertex the minimum (integer) color which is not used by its neighbors. It differ from other
 * greedy coloring algorithms by the order of the vertices: the next vertex to be colored is the vertex with the highest
 * number of colors in its neighborhood (called saturation degree).
 * <p>
 * The algorithm runs in \(O(m \log n)\) time assuming the number of colors is constant.
 * <p>
 * Note that the result is an approximate for the minimum number of colors, as finding an optimal coloring is an NP-hard
 * problem.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/DSatur">Wikipedia</a>
 * @author Barak Ugav
 */
public class ColoringDSatur implements Coloring {

	private HeapReferenceable.Builder<Integer, Integer> heapBuilder =
			HeapReferenceable.newBuilder().keysTypePrimitive(int.class).valuesTypePrimitive(int.class);

	/**
	 * Create a new coloring algorithm object.
	 */
	public ColoringDSatur() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(HeapReferenceable.Builder<?, ?> heapBuilder) {
		this.heapBuilder = heapBuilder.keysTypePrimitive(int.class).valuesTypePrimitive(int.class);
	}

	@Override
	public Coloring.Result computeColoring(Graph g) {
		ArgumentCheck.onlyUndirected(g);
		ArgumentCheck.noSelfLoops(g, "no valid coloring in graphs with self loops");

		ColoringResultImpl res = new ColoringResultImpl(g);
		int n = g.vertices().size();
		BitSet[] neighborColors = new BitSet[n];

		/* We want to compose both the saturationDegree and uncoloredDegree in a key int key, using 'toKey' func */
		int maxDegree = 0;
		for (int u = 0; u < n; u++)
			maxDegree = Math.max(maxDegree, g.degreeOut(u));
		final int maxDegreeFactor = maxDegree + 1;
		/* negate saturationDegree, more neighbor colors should be extracted from the heap first */
		BiInt2IntFunction createKey =
				(saturationDegree, uncoloredDegree) -> -(saturationDegree * maxDegreeFactor + uncoloredDegree);
		Int2IntFunction keyToSaturationDegree = key -> (-key) / maxDegreeFactor;
		Int2IntFunction keyToUncoloredDegree = key -> (-key) % maxDegreeFactor;

		HeapReferenceable<Integer, Integer> heap = heapBuilder.build();
		@SuppressWarnings("unchecked")
		HeapReference<Integer, Integer>[] refs = new HeapReference[n];
		for (int u = 0; u < n; u++) {
			int key = createKey.apply(/* saturationDegree= */0, g.degreeOut(u));
			refs[u] = heap.insert(Integer.valueOf(key), Integer.valueOf(u));
			neighborColors[u] = new BitSet();
		}

		while (!heap.isEmpty()) {
			int u = heap.extractMin().value().intValue();

			int color = 0;
			while (neighborColors[u].get(color))
				color++;
			res.colors[u] = color;
			res.colorsNum = Math.max(res.colorsNum, color + 1);

			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (res.colorOf(v) == -1) { /* v is uncolored */
					HeapReference<Integer, Integer> ref = refs[v];
					int key = ref.key().intValue();
					int saturationDegree = keyToSaturationDegree.applyAsInt(key);
					int uncoloredDegree = keyToUncoloredDegree.applyAsInt(key);

					/* we colored u, v has one less uncolored neighbor */
					uncoloredDegree--;

					if (!neighborColors[v].get(color)) {
						/* v has one more unique color in its neighborhood */
						neighborColors[v].set(color);
						saturationDegree++;

						key = createKey.apply(saturationDegree, uncoloredDegree);
						heap.decreaseKey(ref, Integer.valueOf(key));
					} else {

						key = createKey.apply(saturationDegree, uncoloredDegree);
						/*
						 * we would prefer to use decreaseKey, but we only decrease the uncolored degree, which is
						 * 'increaseKey' with respect to the heap ordering. Remove and insert a new element to the heap,
						 * and pay \(O(\log n)\) instead of \(O(1)\)
						 */
						heap.remove(ref);
						refs[v] = heap.insert(Integer.valueOf(key), ref.value());
					}

				}
			}
		}
		return res;
	}

}