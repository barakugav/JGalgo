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

import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class MinimumCostFlows {

	private static abstract class AbstractImplBase implements MinimumCostFlow {

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, int source, int sink) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, source, sink);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iSource, iSink);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				int source, int sink) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, lowerBound, source, sink);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iLowerBound, iSource, iSink);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
				IntCollection sinks) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, sources, sinks);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iSources, iSinks);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				IntCollection sources, IntCollection sinks) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, lowerBound, sources, sinks);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iLowerBound, iSources, iSinks);
		}

		@Override
		public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction supply) {
			if (g instanceof IndexGraph) {
				computeMinCostFlow((IndexGraph) g, net, cost, supply);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);

			computeMinCostFlow(iGraph, iNet, iCost, iSupply);
		}

		@Override
		public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction supply) {
			if (g instanceof IndexGraph) {
				computeMinCostFlow((IndexGraph) g, net, cost, lowerBound, supply);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			WeightFunction iSupply = IndexIdMaps.idToIndexWeightFunc(supply, viMap);

			computeMinCostFlow(iGraph, iNet, iCost, iLowerBound, iSupply);
		}

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, int source, int sink);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost,
				WeightFunction lowerBound, int source, int sink);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
				IntCollection sinks);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost,
				WeightFunction lowerBound, IntCollection sources, IntCollection sinks);

		abstract void computeMinCostFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction supply);

		abstract void computeMinCostFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction supply);

	}

	static abstract class AbstractImpl extends AbstractImplBase {

		@Override
		void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				int source, int sink) {
			Objects.requireNonNull(lowerBound);
			computeMinCostMaxFlow(g, net, cost, lowerBound, IntList.of(source), IntList.of(sink));
		}

		@Override
		void computeMinCostFlow(IndexGraph g, FlowNetwork netOrig, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction supply) {
			Objects.requireNonNull(g);
			Objects.requireNonNull(netOrig);
			Objects.requireNonNull(cost);
			Objects.requireNonNull(lowerBound);

			Assertions.Graphs.onlyDirected(g);
			Assertions.Flows.checkLowerBound(g, netOrig, lowerBound);
			Assertions.Flows.checkSupply(g, supply);

			final boolean integerFlow = netOrig instanceof FlowNetwork.Int && lowerBound instanceof WeightFunction.Int;

			/*
			 * To solve the minimum cost flow for a given supply and edges lower bounds, we perform a reduction to the
			 * problem with given supply without any edges flow lower bounds. For each edge with lower bound we subtract
			 * the lower bound from the capacity of the edge, and add/remove supply to the edge endpoints.
			 */

			/* Create a network by subtracting the lower bound from each edge capacity */
			FlowNetwork net;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				WeightFunction.Int lowerBoundInt = (WeightFunction.Int) lowerBound;
				net = new FlowNetwork.Int() {
					@Override
					public int getCapacityInt(int edge) {
						return netOrigInt.getCapacityInt(edge) - lowerBoundInt.weightInt(edge);
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public int getFlowInt(int edge) {
						return netOrigInt.getFlowInt(edge) - lowerBoundInt.weightInt(edge);
					}

					@Override
					public void setFlow(int edge, int flow) {
						netOrigInt.setFlow(edge, flow + lowerBoundInt.weightInt(edge));
					}
				};
			} else {
				net = new FlowNetwork() {
					@Override
					public double getCapacity(int edge) {
						return netOrig.getCapacity(edge) - lowerBound.weight(edge);
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public double getFlow(int edge) {
						return netOrig.getFlow(edge) - lowerBound.weight(edge);
					}

					@Override
					public void setFlow(int edge, double flow) {
						netOrig.setFlow(edge, flow + lowerBound.weight(edge));
					}
				};
			}

			/* For each edge with lower bound we add/remove supply from the end endpoints */
			WeightFunction supply2 = computeSupply(g, netOrig, lowerBound, supply);

			/* Solve the reduction problem with only supply without edges lower bounds */
			computeMinCostFlow(g, net, cost, supply2);
		}

		static double hugeCost(IndexGraph g, WeightFunction cost) {
			if (cost instanceof WeightFunction.Int)
				return hugeCostLong(g, (WeightFunction.Int) cost);

			double costSum = 0;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				costSum += Math.abs(cost.weight(e));
			return 1 + costSum;
		}

		static int hugeCost(IndexGraph g, WeightFunction.Int cost) {
			long costSum = hugeCostLong(g, cost);
			int costSumInt = (int) costSum;
			if (costSum != costSumInt)
				throw new AssertionError("integer overflow, huge cost can't fit in 32bit int");
			return costSumInt;
		}

		private static long hugeCostLong(IndexGraph g, WeightFunction.Int cost) {
			long costSum = 0;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				costSum += Math.abs(cost.weightInt(e));
			return costSum + 1;
		}

		static WeightFunction computeSupply(IndexGraph g, FlowNetwork net, WeightFunction lowerBound,
				WeightFunction supply) {
			boolean isInt = net instanceof FlowNetwork.Int;
			if (lowerBound != null)
				isInt = isInt && lowerBound instanceof WeightFunction.Int;
			if (supply != null)
				isInt = isInt && lowerBound instanceof WeightFunction.Int;
			if (isInt)
				return computeSupply(g, (FlowNetwork.Int) net, (WeightFunction.Int) lowerBound,
						(WeightFunction.Int) supply);

			Weights.Double supply2 = Weights.createExternalVerticesWeights(g, double.class);
			if (supply != null) {
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					supply2.set(v, supply.weight(v));
			}
			if (lowerBound != null) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double l = lowerBound.weight(e);
					if (l == 0)
						continue;
					net.setFlow(e, l);
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

		static WeightFunction.Int computeSupply(IndexGraph g, FlowNetwork.Int net, WeightFunction.Int lowerBound,
				WeightFunction.Int supply) {
			Weights.Int supply2 = Weights.createExternalVerticesWeights(g, int.class);
			if (supply != null) {
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					supply2.set(v, supply.weightInt(v));
			}
			if (lowerBound != null) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int l = lowerBound.weightInt(e);
					if (l == 0)
						continue;
					net.setFlow(e, l);
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

	}

	static abstract class AbstractImplBasedSourceSink extends AbstractImpl {

		@Override
		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, int source, int sink);

		@Override
		void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig,
				IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.onlyDirected(gOrig);

			final boolean integerFlow = netOrig instanceof FlowNetwork.Int;
			final boolean integerCost = costOrig instanceof WeightFunction.Int;

			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			builder.expectedVerticesNum(gOrig.vertices().size() + 2);
			builder.expectedEdgesNum(gOrig.edges().size() + sources.size() + sinks.size());

			/* Add all original vertices and edges */
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* Add two artificial terminal vertices, a source and a sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			/* Connect the source to the sources with high capacity edges */
			/* Connect the sinks to the sink with high capacity edges */
			Object capacities;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				int[] capacities0 = new int[sources.size() + sinks.size()];
				int capIdx = 0;
				for (int s : sources) {
					builder.addEdge(source, s);
					capacities0[capIdx++] = FlowNetworks.vertexMaxSupply(gOrig, netOrigInt, s);
				}
				for (int t : sinks) {
					builder.addEdge(t, sink);
					capacities0[capIdx++] = FlowNetworks.vertexMaxDemand(gOrig, netOrigInt, t);
				}
				capacities = capacities0;
			} else {
				double[] capacities0 = new double[sources.size() + sinks.size()];
				int capIdx = 0;
				for (int s : sources) {
					builder.addEdge(source, s);
					capacities0[capIdx++] = FlowNetworks.vertexMaxSupply(gOrig, netOrig, s);
				}
				for (int t : sinks) {
					builder.addEdge(t, sink);
					capacities0[capIdx++] = FlowNetworks.vertexMaxDemand(gOrig, netOrig, t);
				}
				capacities = capacities0;
			}

			IndexGraph g = builder.build();

			/*
			 * Create a network for the new graph by storing capacities and flows of the artificial edges and by
			 * reducing the capacities of edges by their lower bound
			 */
			FlowNetwork net;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				net = new FlowNetwork.Int() {
					final int[] caps = (int[]) capacities;
					final int[] flows = new int[caps.length];

					@Override
					public int getCapacityInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getCapacityInt(edge)
								: caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public int getFlowInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getFlowInt(edge)
								: flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, int flow) {
						if (edge < origEdgesThreshold) {
							netOrigInt.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			} else {
				net = new FlowNetwork() {
					final double[] caps = (double[]) capacities;
					final double[] flows = new double[caps.length];

					@Override
					public double getCapacity(int edge) {
						return edge < origEdgesThreshold ? netOrig.getCapacity(edge) : caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public double getFlow(int edge) {
						return edge < origEdgesThreshold ? netOrig.getFlow(edge) : flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, double flow) {
						if (edge < origEdgesThreshold) {
							netOrig.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			}

			WeightFunction cost;
			if (integerCost) {
				WeightFunction.Int costOrigInt = (WeightFunction.Int) costOrig;
				WeightFunction.Int costInt = e -> e < origEdgesThreshold ? costOrigInt.weightInt(e) : 0;
				cost = costInt;
			} else {
				cost = e -> e < origEdgesThreshold ? costOrig.weight(e) : 0;
			}

			/* Compute a min-cost max-flow in the new graph and network */
			computeMinCostMaxFlow(g, net, cost, source, sink);
		}

		@Override
		void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig,
				WeightFunction lowerBound, IntCollection sources, IntCollection sinks) {
			Objects.requireNonNull(gOrig);
			Objects.requireNonNull(netOrig);
			Objects.requireNonNull(costOrig);
			Objects.requireNonNull(lowerBound);

			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.checkLowerBound(gOrig, netOrig, lowerBound);

			final boolean integerFlow = netOrig instanceof FlowNetwork.Int && lowerBound instanceof WeightFunction.Int;
			final boolean integerCost = costOrig instanceof WeightFunction.Int;

			/*
			 * To solve the problem of minimum-cost maximum-flow between a set of sources and sinks, with a flow lower
			 * bound for each edge, we perform a reduction to min-cost max-flow between a single source and a sink sink
			 * without lower bounds. To get rid of the lower bound, remove from each edge capacity its lower bound, and
			 * add/remove supply from the edge endpoints. This reduction is slightly more complicated than the others,
			 * as some vertices (the sources/sinks) require 'infinite' supply, while others (other vertices with supply)
			 * require finite supply. We create a new graph with all the vertices and edges of the original graph, with
			 * addition of a new source and sink, and connect the source to the sources with high capacity edges, the
			 * source to vertices with a positive supply with capacity equal to the supply, the sinks to the sink with
			 * high capacity edges and lastly the vertices with negative supply to the sink with capacity equal to the
			 * supply.
			 */

			/* For each edge with lower bound add/remove supply to the edge endpoints */
			WeightFunction supply = computeSupply(gOrig, netOrig, lowerBound, null);

			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			builder.expectedVerticesNum(gOrig.vertices().size() + 2);
			builder.expectedEdgesNum(gOrig.edges().size() + sources.size() + sinks.size() + gOrig.vertices().size());

			/* Add all original vertices and edges */
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* determine a great enough capacity ('infinite') for edges to sources (from sinks) */

			/* Add two artificial terminal vertices, a source and a sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			/* Connect the source to the sources with high capacity edges */
			/* Connect the sinks to the sink with high capacity edges */
			final List<?> capacities;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				IntList capacities0 = new IntArrayList(sources.size() + sinks.size());
				for (int s : sources) {
					builder.addEdge(source, s);
					capacities0.add(FlowNetworks.vertexMaxSupply(gOrig, netOrigInt, s));
				}
				for (int t : sinks) {
					builder.addEdge(t, sink);
					capacities0.add(FlowNetworks.vertexMaxDemand(gOrig, netOrigInt, t));
				}
				capacities = capacities0;
			} else {
				DoubleList capacities0 = new DoubleArrayList(sources.size() + sinks.size());
				for (int s : sources) {
					builder.addEdge(source, s);
					capacities0.add(FlowNetworks.vertexMaxSupply(gOrig, netOrig, s));
				}
				for (int t : sinks) {
					builder.addEdge(t, sink);
					capacities0.add(FlowNetworks.vertexMaxDemand(gOrig, netOrig, t));
				}
				capacities = capacities0;
			}
			/*
			 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
			 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
			 * connecting a source to a vertex with positive supply or a vertex with negative supply to a sink.
			 */
			final int sourcesSinksThreshold = builder.edges().size();

			/*
			 * Connect the source to all vertices with positive supply and the vertices with negative supply to the sink
			 */
			if (integerFlow) {
				WeightFunction.Int supplyInt = (WeightFunction.Int) supply;
				IntList capacities0 = (IntList) capacities;
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
					int sup = supplyInt.weightInt(v);
					if (sup > 0) {
						builder.addEdge(source, v);
						capacities0.add(sup);
					} else if (sup < 0) {
						builder.addEdge(v, sink);
						capacities0.add(-sup);
					}
				}
			} else {
				DoubleList capacities0 = (DoubleList) capacities;
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
					double sup = supply.weight(v);
					if (sup > 0) {
						builder.addEdge(source, v);
						capacities0.add(sup);
					} else if (sup < 0) {
						builder.addEdge(v, sink);
						capacities0.add(-sup);
					}
				}
			}

			IndexGraph g = builder.build();

			/*
			 * Create a network for the new graph by storing capacities and flows of the artificial edges and by
			 * reducing the capacities of edges by their lower bound
			 */
			FlowNetwork net;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				WeightFunction.Int lowerBoundInt = (WeightFunction.Int) lowerBound;
				net = new FlowNetwork.Int() {
					int[] caps = ((IntArrayList) capacities).elements();
					int[] flows = new int[g.edges().size() - origEdgesThreshold];

					@Override
					public int getCapacityInt(int edge) {
						return edge < origEdgesThreshold
								? netOrigInt.getCapacityInt(edge) - lowerBoundInt.weightInt(edge)
								: caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public int getFlowInt(int edge) {
						if (edge < origEdgesThreshold)
							return netOrigInt.getFlowInt(edge) - lowerBoundInt.weightInt(edge);
						return flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, int flow) {
						if (edge < origEdgesThreshold) {
							netOrigInt.setFlow(edge, flow + lowerBoundInt.weightInt(edge));
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			} else {
				net = new FlowNetwork() {
					double[] caps = ((DoubleArrayList) capacities).elements();
					double[] flows = new double[g.edges().size() - origEdgesThreshold];

					@Override
					public double getCapacity(int edge) {
						return edge < origEdgesThreshold ? netOrig.getCapacity(edge) - lowerBound.weight(edge)
								: caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public double getFlow(int edge) {
						if (edge < origEdgesThreshold)
							return netOrig.getFlow(edge) - lowerBound.weight(edge);
						return flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, double flow) {
						if (edge < origEdgesThreshold) {
							netOrig.setFlow(edge, flow + lowerBound.weight(edge));
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			}

			WeightFunction cost;
			if (integerCost) {
				/*
				 * Create a cost function for the new graph: original edges have their original costs, big negative cost
				 * for edges that connect vertices with supply as we must satisfy them, and zero cost for edges
				 * connecting source-sources or sinks-sink
				 */
				WeightFunction.Int costOrigInt = (WeightFunction.Int) costOrig;
				final int supplyEdgeCost = -hugeCost(gOrig, costOrigInt);
				WeightFunction.Int costInt = e -> {
					if (e < origEdgesThreshold)
						return costOrigInt.weightInt(e); /* original edge */
					if (e < sourcesSinksThreshold)
						return 0; /* edge to source/sink */
					return supplyEdgeCost; /* edge to a non source/sink vertex with non-zero supply */
				};
				cost = costInt;
			} else {
				/*
				 * Create a cost function for the new graph: original edges have their original costs, big negative cost
				 * for edges that connect vertices with supply as we must satisfy them, and zero cost for edges
				 * connecting source-sources or sinks-sink
				 */
				final double supplyEdgeCost = -hugeCost(gOrig, costOrig);
				cost = e -> {
					if (e < origEdgesThreshold)
						return costOrig.weight(e); /* original edge */
					if (e < sourcesSinksThreshold)
						return 0; /* edge to source/sink */
					return supplyEdgeCost; /* edge to a non source/sink vertex with non-zero supply */
				};
			}

			/* Compute a min-cost max-flow in the new graph and network */
			computeMinCostMaxFlow(g, net, cost, source, sink);

			/* assert all supply was provided */
			if (integerFlow) {
				FlowNetwork.Int netInt = (FlowNetwork.Int) net;
				for (int m = g.edges().size(), e = sourcesSinksThreshold; e < m; e++)
					assert netInt.getFlowInt(e) == netInt.getCapacityInt(e);
			} else {
				for (int m = g.edges().size(), e = sourcesSinksThreshold; e < m; e++)
					assert Math.abs(net.getFlow(e) - net.getCapacity(e)) < 1e-9;
			}
		}

		@Override
		void computeMinCostFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig, WeightFunction supply) {
			Objects.requireNonNull(gOrig);
			Objects.requireNonNull(netOrig);
			Objects.requireNonNull(costOrig);
			Objects.requireNonNull(supply);

			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.checkSupply(gOrig, supply);

			final boolean integerFlow = netOrig instanceof FlowNetwork.Int && supply instanceof WeightFunction.Int;
			final boolean integerCost = costOrig instanceof WeightFunction.Int;

			/*
			 * To solve the minimum cost flow of given supply we use a reduction to minimum-cost maximum-flow between
			 * two terminal vertices, source and sink. We add an edge from the source to each vertex with positive
			 * supply with capacity equal to the supply, and an edge from each vertex with negative supply to the sink
			 * with capacity equal to the supply.
			 */

			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			builder.expectedVerticesNum(gOrig.vertices().size() + 2);
			builder.expectedEdgesNum(gOrig.edges().size() + gOrig.vertices().size());

			/* Add all original vertices and edges */
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index greater than this threshold is not an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* Add two artificial vertices, source and sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			/* Connect the source to vertices with positive supply and vertices with negative supply to the sink */
			List<?> capacities;
			if (integerFlow) {
				WeightFunction.Int supplyInt = (WeightFunction.Int) supply;
				IntList capacities0 = new IntArrayList();
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
					int sup = supplyInt.weightInt(v);
					if (sup > 0) {
						builder.addEdge(source, v);
						capacities0.add(sup);
					} else if (sup < 0) {
						builder.addEdge(v, sink);
						capacities0.add(-sup);
					}
				}
				capacities = capacities0;
			} else {
				DoubleList capacities0 = new DoubleArrayList();
				for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
					double sup = supply.weight(v);
					if (sup > 0) {
						builder.addEdge(source, v);
						capacities0.add(sup);
					} else if (sup < 0) {
						builder.addEdge(v, sink);
						capacities0.add(-sup);
					}
				}
				capacities = capacities0;
			}

			IndexGraph g = builder.build();

			/*
			 * Create a network for the new graph by using two new arrays for the artificial edges capacities and flows
			 */
			FlowNetwork net;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				net = new FlowNetwork.Int() {
					int[] caps = ((IntArrayList) capacities).elements();
					int[] flows = new int[capacities.size()];

					@Override
					public int getCapacityInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getCapacityInt(edge)
								: caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public int getFlowInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getFlowInt(edge)
								: flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, int flow) {
						if (edge < origEdgesThreshold) {
							netOrigInt.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			} else {
				net = new FlowNetwork() {
					double[] caps = ((DoubleArrayList) capacities).elements();
					double[] flows = new double[capacities.size()];

					@Override
					public double getCapacity(int edge) {
						return edge < origEdgesThreshold ? netOrig.getCapacity(edge) : caps[edge - origEdgesThreshold];
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public double getFlow(int edge) {
						return edge < origEdgesThreshold ? netOrig.getFlow(edge) : flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, double flow) {
						if (edge < origEdgesThreshold) {
							netOrig.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			}

			/*
			 * All the artificial edges should not have a cost, if its possible to satisfy the supply they will be
			 * saturated anyway
			 */
			WeightFunction cost;
			if (integerCost) {
				WeightFunction.Int costOrigInt = (WeightFunction.Int) costOrig;
				WeightFunction.Int costInt = e -> e < origEdgesThreshold ? costOrigInt.weightInt(e) : 0;
				cost = costInt;
			} else {
				cost = e -> e < origEdgesThreshold ? costOrig.weight(e) : 0;
			}

			/* Compute a minimum-cost maximum-flow between the two artificial vertices */
			computeMinCostMaxFlow(g, net, cost, source, sink);
		}

	}

	static abstract class AbstractImplBasedSupply extends AbstractImpl {

		@Override
		abstract void computeMinCostFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction supply);

		@Override
		void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, int source, int sink) {
			computeMinCostMaxFlow(g, net, cost, IntList.of(source), IntList.of(sink));
		}

		@Override
		void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
				IntCollection sinks) {
			computeMinCostMaxFlow(g, net, cost, null, sources, sinks);
		}

		@Override
		void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig,
				WeightFunction lowerBoundOrig, IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);

			final boolean integerFlow = netOrig instanceof FlowNetwork.Int
					&& (lowerBoundOrig == null || lowerBoundOrig instanceof WeightFunction.Int);
			final boolean integerCost = costOrig instanceof WeightFunction.Int;

			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			builder.expectedVerticesNum(gOrig.vertices().size() + 2);
			builder.expectedEdgesNum(gOrig.edges().size() + sources.size() + sinks.size() + 2);

			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			final int source = builder.addVertex();
			final int sink = builder.addVertex();
			for (int v : sources)
				builder.addEdge(source, v);
			for (int v : sinks)
				builder.addEdge(v, sink);

			/*
			 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
			 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
			 * connecting the super source and the super sink.
			 */
			final int sourcesSinksThreshold = builder.edges().size();

			builder.addEdge(source, sink);
			builder.addEdge(sink, source);

			IndexGraph g = builder.build();

			final double hugeCapacity = FlowNetworks.hugeCapacity(gOrig, netOrig, sources, sinks);
			FlowNetwork net;
			if (integerFlow) {
				FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
				int hugeCapacityInt = (int) hugeCapacity;
				if (hugeCapacityInt != hugeCapacity)
					throw new AssertionError("integer overflow");
				net = new FlowNetwork.Int() {
					int[] flows = new int[builder.edges().size() - origEdgesThreshold];

					@Override
					public int getCapacityInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getCapacityInt(edge) : hugeCapacityInt;
					}

					@Override
					public void setCapacity(int edge, int capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public int getFlowInt(int edge) {
						return edge < origEdgesThreshold ? netOrigInt.getFlowInt(edge)
								: flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, int flow) {
						if (edge < origEdgesThreshold) {
							netOrigInt.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			} else {
				net = new FlowNetwork() {
					double[] flows = new double[builder.edges().size() - origEdgesThreshold];

					@Override
					public double getCapacity(int edge) {
						return edge < origEdgesThreshold ? netOrig.getCapacity(edge) : hugeCapacity;
					}

					@Override
					public void setCapacity(int edge, double capacity) {
						throw new UnsupportedOperationException("capacities are immutable");
					}

					@Override
					public double getFlow(int edge) {
						return edge < origEdgesThreshold ? netOrig.getFlow(edge) : flows[edge - origEdgesThreshold];
					}

					@Override
					public void setFlow(int edge, double flow) {
						if (edge < origEdgesThreshold) {
							netOrig.setFlow(edge, flow);
						} else {
							flows[edge - origEdgesThreshold] = flow;
						}
					}
				};
			}

			WeightFunction supply;
			if (integerFlow) {
				int hugeCapacityInt = (int) hugeCapacity;
				if (hugeCapacityInt != hugeCapacity)
					throw new AssertionError("integer overflow");
				WeightFunction.Int supplyInt = v -> {
					if (v == source)
						return hugeCapacityInt;
					if (v == sink)
						return -hugeCapacityInt;
					return 0;
				};
				supply = supplyInt;
			} else {
				supply = v -> {
					if (v == source)
						return hugeCapacity;
					if (v == sink)
						return -hugeCapacity;
					return 0;
				};
			}

			WeightFunction cost;
			if (integerCost) {
				WeightFunction.Int costOrigInt = (WeightFunction.Int) costOrig;
				final int hugeCost = hugeCost(gOrig, costOrigInt);
				WeightFunction.Int costInt = e -> {
					if (e < origEdgesThreshold)
						return costOrigInt.weightInt(e);
					if (e < sourcesSinksThreshold)
						return 0;
					return hugeCost;
				};
				cost = costInt;
			} else {
				final double hugeCost = hugeCost(gOrig, costOrig);
				cost = e -> {
					if (e < origEdgesThreshold)
						return costOrig.weight(e);
					if (e < sourcesSinksThreshold)
						return 0;
					return hugeCost;
				};
			}

			if (lowerBoundOrig == null) {
				computeMinCostFlow(g, net, cost, supply);
			} else {
				WeightFunction lowerBound;
				if (integerFlow) {
					WeightFunction.Int lowerBoundOrigInt = (WeightFunction.Int) lowerBoundOrig;
					WeightFunction.Int lowerBoundInt = e -> e < origEdgesThreshold ? lowerBoundOrigInt.weightInt(e) : 0;
					lowerBound = lowerBoundInt;
				} else {
					lowerBound = e -> e < origEdgesThreshold ? lowerBoundOrig.weight(e) : 0;
				}
				computeMinCostFlow(g, net, cost, lowerBound, supply);
			}
		}

	}

	static void saturateNegativeCostSelfEdges(IndexGraph g, FlowNetwork net, WeightFunction cost) {
		if (!g.getCapabilities().selfEdges())
			return;
		if (net instanceof FlowNetwork.Int) {
			FlowNetwork.Int netInt = (FlowNetwork.Int) net;
			if (cost instanceof WeightFunction.Int) {
				WeightFunction.Int costInt = (WeightFunction.Int) cost;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					if (g.edgeSource(e) == g.edgeTarget(e) && costInt.weightInt(e) < 0)
						netInt.setFlow(e, netInt.getCapacityInt(e));
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					if (g.edgeSource(e) == g.edgeTarget(e) && cost.weight(e) < 0)
						netInt.setFlow(e, netInt.getCapacityInt(e));
			}
		} else {
			if (cost instanceof WeightFunction.Int) {
				WeightFunction.Int costInt = (WeightFunction.Int) cost;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					if (g.edgeSource(e) == g.edgeTarget(e) && costInt.weightInt(e) < 0)
						net.setFlow(e, net.getCapacity(e));
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					if (g.edgeSource(e) == g.edgeTarget(e) && cost.weight(e) < 0)
						net.setFlow(e, net.getCapacity(e));
			}
		}
	}

}
