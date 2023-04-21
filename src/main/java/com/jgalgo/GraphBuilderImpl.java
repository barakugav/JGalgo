package com.jgalgo;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.jgalgo.IDStrategy.IDAddRemoveListener;

import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphBuilderImpl {

	private static abstract class Abstract implements GraphBuilder {

		int verticesNum;
		Class<? extends IDStrategy> edgesIDStrategy;

		@Override
		public GraphBuilder setVerticesNum(int n) {
			if (n < 0)
				throw new IllegalArgumentException();
			verticesNum = n;
			return this;
		}

		@Override
		public GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy) {
			this.edgesIDStrategy = edgesIDStrategy;
			return this;
		}

		Graph wrapWithCustomIDStrategies(GraphBaseContinues g) {
			IDStrategy eIDStrat = createIDStrategy(edgesIDStrategy);
			if (eIDStrat == null)
				return g;
			if (g instanceof DiGraph) {
				return new GraphCustomIDStrategiesDirected(g, eIDStrat);
			} else {
				return new GraphCustomIDStrategiesUndirected(g, eIDStrat);
			}
		}

		private static IDStrategy createIDStrategy(Class<? extends IDStrategy> strategyClass) {
			if (strategyClass == null)
				return null;
			if (strategyClass == IDStrategy.Continues.class) {
				return null; /* Use default */
			} else if (strategyClass == IDStrategy.Fixed.class) {
				return new IDStrategy.Fixed();
			} else if (strategyClass == IDStrategy.Rand.class) {
				return new IDStrategy.Rand();
			} else {
				throw new IllegalArgumentException(strategyClass.toString());
			}
		}
	}

	static class Array extends GraphBuilderImpl.Abstract {
		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphArrayUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphArrayDirected(verticesNum));
		}
	}

	static class Linked extends GraphBuilderImpl.Abstract {
		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphLinkedUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphLinkedDirected(verticesNum));
		}
	}

	static class Table extends GraphBuilderImpl.Abstract {
		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphTableUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphTableDirected(verticesNum));
		}
	}

	private static class GraphCustomIDStrategies extends GraphBase {

		final GraphBaseContinues g;

		GraphCustomIDStrategies(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(new IDStrategy.Continues(g.vertices().size()), edgesIDStrategy, g.getCapabilities());
			this.g = Objects.requireNonNull(g);

			g.getVerticesIDStrategy().addIDSwapListener((vIdx1, vIdx2) -> verticesIDStrategy.idxSwap(vIdx1, vIdx2));
			g.getVerticesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					verticesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = verticesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					verticesIDStrategy.clear();
				}
			});
			g.getEdgesIDStrategy().addIDSwapListener((eIdx1, eIdx2) -> edgesIDStrategy.idxSwap(eIdx1, eIdx2));
			g.getEdgesIDStrategy().addIDAddRemoveListener(new IDAddRemoveListener() {

				@Override
				public void idRemove(int id) {
					edgesIDStrategy.removeIdx(id);
				}

				@Override
				public void idAdd(int id) {
					int idx = edgesIDStrategy.newIdx();
					if (idx != id)
						throw new IllegalStateException();
				}

				@Override
				public void idsClear() {
					edgesIDStrategy.clear();
				}
			});
		}

		@Override
		public int addVertex() {
			int uIdx = g.addVertex();
			return verticesIDStrategy.idxToId(uIdx);
		}

		@Override
		public void removeVertex(int v) {
			int vIdx = verticesIDStrategy.idToIdx(v);
			g.removeVertex(vIdx);
		}

		@Override
		public void removeVertices(IntCollection vs) {
			g.removeVertices(new AbstractIntCollection() {

				@Override
				public int size() {
					return vs.size();
				}

				@Override
				public IntIterator iterator() {
					return new IntIterator() {
						final IntIterator it = vs.iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public int nextInt() {
							return verticesIDStrategy.idToIdx(it.nextInt());
						}
					};
				}

				@Override
				public boolean contains(int uIdx) {
					return vs.contains(verticesIDStrategy.idxToId(uIdx));
				}

			});
		}

		@Override
		public EdgeIter edgesOut(int u) {
			EdgeIter it = g.edgesOut(verticesIDStrategy.idToIdx(u));
			return new EdgeItr(it);
		}

		@Override
		public EdgeIter edgesIn(int v) {
			EdgeIter it = g.edgesIn(verticesIDStrategy.idToIdx(v));
			return new EdgeItr(it);
		}

		@Override
		public int getEdge(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			int eIdx = g.getEdge(uIdx, vIdx);
			return eIdx == -1 ? -1 : edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public EdgeIter getEdges(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			EdgeIter it = g.getEdges(uIdx, vIdx);
			return new EdgeItr(it);
		}

		@Override
		public int addEdge(int u, int v) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			int vIdx = verticesIDStrategy.idToIdx(v);
			int eIdx = g.addEdge(uIdx, vIdx);
			return edgesIDStrategy.idxToId(eIdx);
		}

		@Override
		public void removeEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			g.removeEdge(eIdx);
		}

		@Override
		public void removeEdges(IntCollection edges) {
			g.removeEdges(new AbstractIntCollection() {

				@Override
				public int size() {
					return edges.size();
				}

				@Override
				public IntIterator iterator() {
					return new IntIterator() {
						final IntIterator it = edges.iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public int nextInt() {
							return edgesIDStrategy.idToIdx(it.nextInt());
						}
					};
				}

				@Override
				public boolean contains(int eIdx) {
					return edges.contains(edgesIDStrategy.idxToId(eIdx));
				}

			});
		}

		@Override
		public void removeEdgesOf(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			g.removeEdgesOf(uIdx);
		}

		@Override
		public void removeEdgesOutOf(int u) {
			g.removeEdgesOutOf(verticesIDStrategy.idToIdx(u));
		}

		@Override
		public void removeEdgesInOf(int v) {
			g.removeEdgesInOf(verticesIDStrategy.idToIdx(v));
		}

		@Override
		public int edgeSource(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeSource(eIdx);
		}

		@Override
		public int edgeTarget(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			return g.edgeTarget(eIdx);
		}

		@Override
		public int edgeEndpoint(int edge, int endpoint) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			int endpointIdx = verticesIDStrategy.idToIdx(endpoint);
			int resIdx = g.edgeEndpoint(eIdx, endpointIdx);
			return verticesIDStrategy.idxToId(resIdx);
		}

		@Override
		public int degreeOut(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			return g.degreeOut(uIdx);
		}

		@Override
		public int degreeIn(int u) {
			int uIdx = verticesIDStrategy.idToIdx(u);
			return g.degreeIn(uIdx);
		}

		@Override
		public void clear() {
			g.clear();
			verticesIDStrategy.clear();
			edgesIDStrategy.clear();
		}

		@Override
		public void clearEdges() {
			g.clearEdges();
			edgesIDStrategy.clear();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key) {
			return g.verticesWeight(key);
		}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return g.getVerticesWeightKeys();
		}

		@Override
		public Collection<Weights<?>> getVerticesWeights() {
			return g.getVerticesWeights();
		}

		@Override
		public void removeVerticesWeights(Object key) {
			g.removeVerticesWeights(key);
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT edgesWeight(Object key) {
			return g.edgesWeight(key);
		}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return g.getEdgesWeightsKeys();
		}

		@Override
		public void removeEdgesWeights(Object key) {
			g.removeEdgesWeights(key);
		}

		@Override
		public Collection<Weights<?>> getEdgesWeights() {
			return g.getEdgesWeights();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrategy;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrategy;
		}

		@Override
		void addVerticesWeightsContainer(Object key, Weights<?> weights) {
			g.addVerticesWeightsContainer(key, weights);
		}

		@Override
		void addEdgesWeightsContainer(Object key, Weights<?> weights) {
			g.addEdgesWeightsContainer(key, weights);
		}

		class EdgeItr implements EdgeIter {

			private final EdgeIter it;

			EdgeItr(EdgeIter it) {
				this.it = it;
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int nextInt() {
				int eIdx = it.nextInt();
				return edgesIDStrategy.idxToId(eIdx);
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public int v() {
				int vIdx = it.v();
				return verticesIDStrategy.idxToId(vIdx);
			}

			@Override
			public int u() {
				int uIdx = it.u();
				return verticesIDStrategy.idxToId(uIdx);
			}

		}

	}

	private static class GraphCustomIDStrategiesDirected extends GraphCustomIDStrategies implements DiGraph {

		GraphCustomIDStrategiesDirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			if (!(g instanceof DiGraph))
				throw new IllegalArgumentException();
		}

		private DiGraph digraph() {
			return (DiGraph) g;
		}

		@Override
		public void reverseEdge(int edge) {
			int eIdx = edgesIDStrategy.idToIdx(edge);
			digraph().reverseEdge(eIdx);
		}

	}

	private static class GraphCustomIDStrategiesUndirected extends GraphCustomIDStrategies implements UGraph {

		GraphCustomIDStrategiesUndirected(GraphBaseContinues g, IDStrategy edgesIDStrategy) {
			super(g, edgesIDStrategy);
			if (!(g instanceof UGraph))
				throw new IllegalArgumentException();
		}

	}

}