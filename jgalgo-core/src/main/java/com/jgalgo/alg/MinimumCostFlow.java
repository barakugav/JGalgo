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

import java.util.Collection;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Compute the minimum-cost (max) flow in a flow network.
 *
 * <p>
 * There are a few variations of the minimum-cost flow problem: (1) given source(s) and sink(s) terminal vertices, and
 * the objecting is to find the flow with the lowest cost out of all maximum flows. (2) given per-vertex finite supply,
 * and the objective is to find a minimum-cost flow satisfying the supply, namely that for each vertex the sum of flow
 * units going out of the vertex minus the sum of flow units going into it is equal to its supply.
 *
 * <p>
 * In addition to these variants, a lower bound for each edge flow can be specified, similar to the capacities which can
 * be viewed as upper bounds.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    MaximumFlow
 * @see    FlowNetwork
 * @see    <a href= "https://en.wikipedia.org/wiki/Minimum-cost_flow_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCostFlow {

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net} and
	 * {@link IWeightFunction} as {@code cost} to avoid boxing/unboxing.
	 *
	 * @param <V>    the vertices type
	 * @param <E>    the edges type
	 * @param g      the graph
	 * @param net    the flow network. The result flow values will be set using this network
	 * @param cost   an edge weight function representing the cost of each unit of flow along the edge
	 * @param source a source vertex
	 * @param sink   a sink vertex
	 */
	<V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost, V source, V sink);

	/**
	 * Compute the min-cost max-flow in a network between a source and a sink given a lower bound for the edges flows.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net},
	 * {@link IWeightFunction} as {@code cost} and {@code lowerBound} to avoid boxing/unboxing.
	 *
	 * @param <V>        the vertices type
	 * @param <E>        the edges type
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param source     a source vertex
	 * @param sink       a sink vertex
	 */
	<V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, V source, V sink);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net},
	 * {@link IWeightFunction} as {@code cost}, and {@link IntCollection} as {@code sources} and {@code sinks} to avoid
	 * boxing/unboxing.
	 *
	 * @param <V>     the vertices type
	 * @param <E>     the edges type
	 * @param g       the graph
	 * @param net     the flow network. The result flow values will be set using this network
	 * @param cost    an edge weight function representing the cost of each unit of flow along the edge
	 * @param sources a set of source vertices
	 * @param sinks   a set of sinks vertices
	 */
	<V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost,
			Collection<V> sources, Collection<V> sinks);

	/**
	 * Compute the min-cost max-flow in a network between a set of sources and a set of sinks given a lower bound for
	 * the edges flows.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net},
	 * {@link IWeightFunction} as {@code cost} and {@code lowerBound}, and {@link IntCollection} as {@code sources} and
	 * {@code sinks} to avoid boxing/unboxing.
	 *
	 * @param <V>        the vertices type
	 * @param <E>        the edges type
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param sources    a set of source vertices
	 * @param sinks      a set of sinks vertices
	 */
	<V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, Collection<V> sources, Collection<V> sinks);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex.
	 *
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net},
	 * {@link IWeightFunction} as {@code cost} and {@code supply} to avoid boxing/unboxing.
	 *
	 * @param <V>    the vertices type
	 * @param <E>    the edges type
	 * @param g      the graph
	 * @param net    the flow network. The result flow values will be set using this network
	 * @param cost   an edge weight function representing the cost of each unit of flow along the edge
	 * @param supply a vertex weight function representing the supply for each vertex
	 */
	<V, E> void computeMinCostFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost,
			WeightFunction<V> supply);

	/**
	 * Compute the min-cost (not maximum!) flow in a network given a supply for each vertex and a lower bound for the
	 * edges flows.
	 *
	 * <p>
	 * The supply is a scalar for each vertex, and the objective is to find a minimum-cost flow satisfying the supply,
	 * namely that for each vertex the sum of flow units going out of the vertex minus the sum of flow units going into
	 * it is equal to its supply.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IFlowNetwork} as {@code net},
	 * {@link IWeightFunction} as {@code cost}, {@code lowerBound} and {@code supply} to avoid boxing/unboxing.
	 *
	 * @param <V>        the vertices type
	 * @param <E>        the edges type
	 * @param g          the graph
	 * @param net        the flow network. The result flow values will be set using this network
	 * @param cost       an edge weight function representing the cost of each unit of flow along the edge
	 * @param lowerBound an edge weight function representing a lower bound for the flow along each edge
	 * @param supply     a vertex weight function representing the supply for each vertex
	 */
	<V, E> void computeMinCostFlow(Graph<V, E> g, FlowNetwork<V, E> net, WeightFunction<E> cost,
			WeightFunction<E> lowerBound, WeightFunction<V> supply);

	/**
	 * Create a new min-cost-flow algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCostFlow} object. The
	 * {@link MinimumCostFlow.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumCostFlow}
	 */
	static MinimumCostFlow newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new minimum cost flow algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumCostFlow} objects
	 */
	static MinimumCostFlow.Builder newBuilder() {
		return new MinimumCostFlow.Builder() {
			String impl;
			boolean integerNetwork;
			boolean integerCosts;

			@Override
			public MinimumCostFlow build() {
				if (impl != null) {
					switch (impl) {
						case "cycle-canceling":
							return new MinimumCostFlowCycleCanceling();
						case "cost-scaling":
							return new MinimumCostFlowCostScaling();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				if (integerNetwork && integerCosts) {
					return new MinimumCostFlowCostScaling();
				} else {
					return new MinimumCostFlow() {

						private final MinimumCostFlow integerAlgo = new MinimumCostFlowCostScaling();
						private final MinimumCostFlow floatsAlgo = new MinimumCostFlowCycleCanceling();

						@Override
						public <V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, V source, V sink) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, source, sink);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, source, sink);
							}
						}

						@Override
						public <V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, V source, V sink) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt
									&& lowerBound instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, source, sink);
							}
						}

						@Override
						public <V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, Collection<V> sources, Collection<V> sinks) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, sources, sinks);
							}
						}

						@Override
						public <V, E> void computeMinCostMaxFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, Collection<V> sources,
								Collection<V> sinks) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt
									&& lowerBound instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
							} else {
								floatsAlgo.computeMinCostMaxFlow(g, net, cost, lowerBound, sources, sinks);
							}
						}

						@Override
						public <V, E> void computeMinCostFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, WeightFunction<V> supply) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt
									&& supply instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostFlow(g, net, cost, supply);
							} else {
								floatsAlgo.computeMinCostFlow(g, net, cost, supply);
							}
						}

						@Override
						public <V, E> void computeMinCostFlow(Graph<V, E> g, FlowNetwork<V, E> net,
								WeightFunction<E> cost, WeightFunction<E> lowerBound, WeightFunction<V> supply) {
							if (net instanceof FlowNetworkInt && cost instanceof WeightFunctionInt
									&& lowerBound instanceof WeightFunctionInt && supply instanceof WeightFunctionInt) {
								integerAlgo.computeMinCostFlow(g, net, cost, lowerBound, supply);
							} else {
								floatsAlgo.computeMinCostFlow(g, net, cost, lowerBound, supply);
							}
						}

					};
				}
			}

			@Override
			public MinimumCostFlow.Builder integerNetwork(boolean enable) {
				integerNetwork = enable;
				return this;
			}

			@Override
			public MinimumCostFlow.Builder integerCosts(boolean enable) {
				integerCosts = enable;
				return this;
			}

			@Override
			public MinimumCostFlow.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						MinimumCostFlow.Builder.super.setOption(key, value);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MinimumCostFlow} objects.
	 *
	 * @see    MinimumCostFlow#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum cost flow computation.
		 *
		 * @return a new minimum cost flow algorithm
		 */
		MinimumCostFlow build();

		/**
		 * Enable/disable integer network (capacities, flows, vertices supplies and edges flow lower bound).
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the network is known to be integral. If the
		 * option is enabled, non-integer networks will not be supported by the built algorithms.
		 *
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer networks only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerNetwork(boolean enable);

		/**
		 * Enable/disable integer costs.
		 *
		 * <p>
		 * More efficient and accurate implementations may be supported if the cost function is known to be integral. If
		 * the option is enabled, non-integer cost functions will not be supported by the built algorithms.
		 *
		 * <p>
		 * The default value of this option is {@code false}.
		 *
		 * @param  enable if {@code true}, algorithms built by this builder will support integer cost functions only
		 * @return        this builder
		 */
		MinimumCostFlow.Builder integerCosts(boolean enable);

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default MinimumCostFlow.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
