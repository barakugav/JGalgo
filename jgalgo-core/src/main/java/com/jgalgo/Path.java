package com.jgalgo;

import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * A path of edges in a graph.
 * <p>
 * A path is a list of edges \(e_1,e_2,\ldots\) where each target vertex of edge \(e_i\) is the source vertex of the
 * next edge \(e_{i+1}\). If the graph is undirected the definition of a 'source' and 'target' are interchangeable, and
 * each pair of consecutive edges simply share an endpoint.
 * <p>
 * The Path object can be treated as a {@link IntList} of edges.
 * <p>
 * A Path object might be used to represent a cycle as well, if the source and target of the path are the same vertex.
 * <p>
 * If the underlying graph was modified after the Path object was created, the Path object should not be used.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * Path p = Path.findPath(g, sourceVertex, targetVertex);
 *
 * System.out.println("The path between u and v consist of the following edges:");
 * for (EdgeIter it = p.edgeIter(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = it.u(), v = it.v();
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * @author Barak Ugav
 */
public class Path extends AbstractIntList {

	private final Graph g;
	private final int source;
	private final int target;
	private final IntList edges;

	/**
	 * Construct a new path in a graph from an edge list, a source and a target vertices.
	 *
	 * @param g      a graph
	 * @param source a source vertex
	 * @param target a target vertex
	 * @param edges  a list of edges that form a path from the {@code source} to the {@code target} vertices in the
	 *                   graph.
	 */
	public Path(Graph g, int source, int target, IntList edges) {
		this.g = g;
		this.source = source;
		this.target = target;
		this.edges = edges instanceof IntLists.UnmodifiableList ? edges : IntLists.unmodifiable(edges);
	}

	/**
	 * Get the source vertex of the path.
	 * <p>
	 * If the returned vertex is the same as {@link #target()}, the represented path is actually a cycle.
	 *
	 * @return the source vertex of the path.
	 */
	public int source() {
		return source;
	}

	/**
	 * Get the target vertex of the path.
	 * <p>
	 * If the returned vertex is the same as {@link #source()}, the represented path is actually a cycle.
	 *
	 * @return the target vertex of the path.
	 */
	public int target() {
		return target;
	}

	/**
	 * Get an iterator that iterate over the edges of the path.
	 */
	@Override
	public IntListIterator iterator() {
		return edges.iterator();
	}

	/**
	 * Get an {@link EdgeIter} that iterate over the edges of the path.
	 *
	 * @return an {@link EdgeIter} that iterate over the edges of the path.
	 */
	public EdgeIter edgeIter() {
		if (g instanceof UGraph) {
			return new IterUndirected((UGraph) g, edges, source);
		} else if (g instanceof DiGraph) {
			return new IterDirected((DiGraph) g, edges);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static class IterUndirected implements EdgeIterImpl {

		private final UGraph g;
		private final IntListIterator it;
		private int e = -1, v = -1;

		IterUndirected(UGraph g, IntList path, int source) {
			this.g = g;
			v = source;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			e = it.nextInt();
			assert v == g.edgeSource(e) || v == g.edgeTarget(e);
			v = g.edgeEndpoint(e, v);
			return e;
		}

		@Override
		public int peekNext() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int u() {
			return g.edgeEndpoint(e, v);
		}

		@Override
		public int v() {
			return v;
		}

	}

	private static class IterDirected implements EdgeIterImpl {

		private final DiGraph g;
		private final IntListIterator it;
		private int e = -1;

		IterDirected(DiGraph g, IntList path) {
			this.g = g;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			int eNext = it.nextInt();
			if (e != -1)
				assert g.edgeTarget(e) == g.edgeSource(eNext);
			return e = eNext;
		}

		@Override
		public int peekNext() {
			int peek = it.nextInt();
			it.previousInt(); /* go back */
			return peek;
		}

		@Override
		public int u() {
			return g.edgeSource(e);
		}

		@Override
		public int v() {
			return g.edgeTarget(e);
		}

	}

	@Override
	public int size() {
		return edges.size();
	}

	@Override
	public int getInt(int index) {
		return edges.getInt(index);
	}

	@Override
	public int indexOf(int k) {
		return edges.indexOf(k);
	}

	@Override
	public int lastIndexOf(int k) {
		return edges.lastIndexOf(k);
	}

	/**
	 * Find a valid path from \(u\) to \(v\).
	 * <p>
	 * This function uses BFS, which will result in the shortest path in the number of edges.
	 *
	 * @param  g a graph
	 * @param  u source vertex
	 * @param  v target vertex
	 * @return   a path from \(u\) to \(v\), or {@code null} if no such path was found
	 */
	public static Path findPath(Graph g, final int u, final int v) {
		if (u == v)
			return new Path(g, u, v, IntLists.emptyList());
		boolean reverse = true;
		int u0 = u, v0 = v;
		if (g instanceof UGraph) {
			u0 = v;
			v0 = u;
			reverse = false;
		}
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntArrayList path = new IntArrayList();
		for (BFSIter it = new BFSIter(g, u0); it.hasNext();) {
			int p = it.nextInt();
			backtrack[p] = it.inEdge();
			if (p == v0)
				break;
		}

		if (backtrack[v0] == -1)
			return null;

		for (int p = v0; p != u0;) {
			int e = backtrack[p];
			path.add(e);
			p = g.edgeEndpoint(e, p);
		}

		if (reverse)
			IntArrays.reverse(path.elements(), 0, path.size());
		return new Path(g, u, v, path);
	}

}
