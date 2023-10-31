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
package com.jgalgo.example;

import com.jgalgo.alg.LowestCommonAncestorDynamic;
import com.jgalgo.alg.LowestCommonAncestorStatic;
import com.jgalgo.graph.IntGraph;

public class LowestCommonAncestorExample {

	public static void staticLCAExample() {
		/* Create a full binary tree of height 3 */
		IntGraph tree = IntGraph.newUndirected();
		int rt = tree.addVertex();
		int v1 = tree.addVertex();
		int v2 = tree.addVertex();
		int v3 = tree.addVertex();
		int v4 = tree.addVertex();
		int v5 = tree.addVertex();
		int v6 = tree.addVertex();
		tree.addEdge(rt, v1);
		tree.addEdge(rt, v2);
		tree.addEdge(v1, v3);
		tree.addEdge(v1, v4);
		tree.addEdge(v2, v5);
		tree.addEdge(v2, v6);

		/* Pre process the tree for LCA queries */
		LowestCommonAncestorStatic lcaAlgo = LowestCommonAncestorStatic.newInstance();
		LowestCommonAncestorStatic.IDataStructure lcaDs =
				(LowestCommonAncestorStatic.IDataStructure) lcaAlgo.preProcessTree(tree, Integer.valueOf(rt));

		/* Find the lowest common ancestor of any pair of vertices in the tree */
		assert lcaDs.findLca(v1, v2) == rt;
		assert lcaDs.findLca(v1, v3) == v1;
		assert lcaDs.findLca(v3, v4) == v1;
		assert lcaDs.findLca(v5, v6) == v2;
		assert lcaDs.findLca(v3, v6) == rt;
	}

	public static void dynamicLCAExample() {
		/* Create a full binary tree of height 3 and perform LCA queries during the construction */
		LowestCommonAncestorDynamic lcaAlgo = LowestCommonAncestorDynamic.newInstance();
		LowestCommonAncestorDynamic.Vertex rt = lcaAlgo.initTree();

		LowestCommonAncestorDynamic.Vertex v1 = lcaAlgo.addLeaf(rt);
		LowestCommonAncestorDynamic.Vertex v2 = lcaAlgo.addLeaf(rt);
		assert lcaAlgo.findLowestCommonAncestor(v1, v2) == rt;

		LowestCommonAncestorDynamic.Vertex v3 = lcaAlgo.addLeaf(v1);
		assert lcaAlgo.findLowestCommonAncestor(v1, v3) == v1;

		LowestCommonAncestorDynamic.Vertex v4 = lcaAlgo.addLeaf(v1);
		assert lcaAlgo.findLowestCommonAncestor(v3, v4) == v1;

		LowestCommonAncestorDynamic.Vertex v5 = lcaAlgo.addLeaf(v2);
		LowestCommonAncestorDynamic.Vertex v6 = lcaAlgo.addLeaf(v2);
		assert lcaAlgo.findLowestCommonAncestor(v5, v6) == v2;
		assert lcaAlgo.findLowestCommonAncestor(v3, v6) == rt;
	}

	public static void main(String[] args) {
		staticLCAExample();
		dynamicLCAExample();
	}

}
