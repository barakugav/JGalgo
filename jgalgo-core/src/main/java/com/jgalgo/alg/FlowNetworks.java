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

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

class FlowNetworks {

	@SuppressWarnings("unchecked")
	static <V, E> FlowNetwork<V, E> createFromEdgeWeights(WeightsDouble<E> capacities, WeightsDouble<E> flows) {
		if (capacities instanceof IWeightsDouble && flows instanceof IWeightsDouble) {
			return (FlowNetwork<V, E>) new FlowNetworks.NetImplEdgeIWeights((IWeightsDouble) capacities,
					(IWeightsDouble) flows);
		} else {
			return new FlowNetworks.NetImplEdgeWeights<>(capacities, flows);
		}
	}

	static IFlowNetwork createFromEdgeWeights(IWeightsDouble capacities, IWeightsDouble flows) {
		return new FlowNetworks.NetImplEdgeIWeights(capacities, flows);
	}

	static class NetImplEdgeWeights<V, E> implements FlowNetwork<V, E> {

		final WeightsDouble<E> capacities;
		final WeightsDouble<E> flows;
		static final double EPS = 0.0001;

		NetImplEdgeWeights(WeightsDouble<E> capacities, WeightsDouble<E> flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		@Override
		public double getCapacity(E edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(E edge, double capacity) {
			Assertions.Flows.positiveCapacity(capacity);
			capacities.set(edge, capacity);
		}

		@Override
		public double getFlow(E edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(E edge, double flow) {
			double capacity = getCapacity(edge);
			Assertions.Flows.flowLessThanCapacity(flow, capacity, EPS);
			flows.set(edge, Math.min(flow, capacity));
		}
	}

	static class NetImplEdgeWeightsInt<V, E> implements FlowNetworkInt<V, E> {

		final WeightsInt<E> capacities;
		final WeightsInt<E> flows;

		NetImplEdgeWeightsInt(WeightsInt<E> capacities, WeightsInt<E> flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		static <V, E> FlowNetworkInt<V, E> addWeightsAndCreateNet(Graph<V, E> g) {
			WeightsInt<E> capacities = g.addEdgesWeights("_capacity", int.class);
			WeightsInt<E> flows = g.addEdgesWeights("_flow", int.class);
			return new NetImplEdgeWeightsInt<>(capacities, flows);
		}

		static <V, E> FlowNetworkInt<V, E> createExternalWeightsAndCreateNet(Graph<V, E> g) {
			WeightsInt<E> capacities = Weights.createExternalEdgesWeights(g, int.class);
			WeightsInt<E> flows = Weights.createExternalEdgesWeights(g, int.class);
			return new NetImplEdgeWeightsInt<>(capacities, flows);
		}

		@Override
		public int getCapacityInt(E edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(E edge, int capacity) {
			Assertions.Flows.positiveCapacity(capacity);
			capacities.set(edge, capacity);
		}

		@Override
		public int getFlowInt(E edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(E edge, int flow) {
			int capacity = getCapacityInt(edge);
			Assertions.Flows.flowLessThanCapacity(flow, capacity);
			flows.set(edge, Math.min(flow, capacity));
		}
	}

	static class NetImplEdgeIWeights implements IFlowNetwork {

		final IWeightsDouble capacities;
		final IWeightsDouble flows;
		static final double EPS = 0.0001;

		NetImplEdgeIWeights(IWeightsDouble capacities, IWeightsDouble flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		@Override
		public double getCapacity(int edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			Assertions.Flows.positiveCapacity(capacity);
			capacities.set(edge, capacity);
		}

		@Override
		public double getFlow(int edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(int edge, double flow) {
			double capacity = getCapacity(edge);
			Assertions.Flows.flowLessThanCapacity(flow, capacity, EPS);
			flows.set(edge, Math.min(flow, capacity));
		}
	}

	static class NetImplEdgeIWeightsInt implements IFlowNetworkInt {

		final IWeightsInt capacities;
		final IWeightsInt flows;

		NetImplEdgeIWeightsInt(IWeightsInt capacities, IWeightsInt flows) {
			this.capacities = Objects.requireNonNull(capacities);
			this.flows = Objects.requireNonNull(flows);
		}

		static IFlowNetworkInt addWeightsAndCreateNet(IntGraph g) {
			IWeightsInt capacities = g.addEdgesWeights("_capacity", int.class);
			IWeightsInt flows = g.addEdgesWeights("_flow", int.class);
			return new NetImplEdgeIWeightsInt(capacities, flows);
		}

		static IFlowNetworkInt createExternalWeightsAndCreateNet(IntGraph g) {
			IWeightsInt capacities = IWeights.createExternalEdgesWeights(g, int.class);
			IWeightsInt flows = IWeights.createExternalEdgesWeights(g, int.class);
			return new NetImplEdgeIWeightsInt(capacities, flows);
		}

		@Override
		public int getCapacityInt(int edge) {
			return capacities.get(edge);
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			Assertions.Flows.positiveCapacity(capacity);
			capacities.set(edge, capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return flows.get(edge);
		}

		@Override
		public void setFlow(int edge, int flow) {
			int capacity = getCapacityInt(edge);
			Assertions.Flows.flowLessThanCapacity(flow, capacity);
			flows.set(edge, Math.min(flow, capacity));
		}
	}

	static class ResidualGraph {
		final IndexGraph gOrig;
		final IndexGraph g;
		final int[] edgeRef;
		final int[] twin;

		ResidualGraph(IndexGraph gOrig, IndexGraph g, int[] edgeRef, int[] twin) {
			this.gOrig = gOrig;
			this.g = g;
			this.edgeRef = edgeRef;
			this.twin = twin;
		}

		boolean isOriginalEdge(int e) {
			int eOrig = edgeRef[e];
			return eOrig != -1 && g.edgeSource(e) == gOrig.edgeSource(eOrig);
		}

		static class Builder {

			private final IndexGraphBuilder gBuilder;
			private final IndexGraph gOrig;
			private final IntArrayList edgeRef;
			private final IntArrayList twin;

			Builder(IndexGraph gOrig) {
				this.gOrig = Objects.requireNonNull(gOrig);
				gBuilder = IndexGraphBuilder.newDirected();
				edgeRef = new IntArrayList(gOrig.edges().size() * 2);
				twin = new IntArrayList(gOrig.edges().size() * 2);
			}

			void addAllOriginalEdges() {
				assert gBuilder.vertices().isEmpty();
				gBuilder.expectedVerticesNum(gOrig.vertices().size());
				for (int n = gOrig.vertices().size(), u = 0; u < n; u++) {
					int vBuilder = gBuilder.addVertex();
					assert u == vBuilder;
				}

				assert gBuilder.edges().isEmpty();
				gBuilder.expectedEdgesNum(gOrig.edges().size() * 2);
				for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
					int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
					if (u != v)
						addEdge(u, v, e);
				}
			}

			int addVertex() {
				return gBuilder.addVertex();
			}

			void addEdge(int u, int v, int e) {
				int e1Builder = gBuilder.addEdge(u, v);
				int e2Builder = gBuilder.addEdge(v, u);
				assert e1Builder == edgeRef.size();
				edgeRef.add(e);
				assert e2Builder == edgeRef.size();
				edgeRef.add(e);
				assert e1Builder == twin.size();
				twin.add(e2Builder);
				assert e2Builder == twin.size();
				twin.add(e1Builder);
			}

			ResidualGraph build() {
				IndexGraphBuilder.ReIndexedGraph reindexedGraph = gBuilder.reIndexAndBuild(false, true);
				IndexGraph g = reindexedGraph.graph();
				final int m = g.edges().size();
				int[] edgeRefTemp = edgeRef.elements();
				int[] twinTemp = twin.elements();
				int[] edgeRef = new int[m];
				int[] twin = new int[m];
				if (reindexedGraph.edgesReIndexing().isPresent()) {
					IndexGraphBuilder.ReIndexingMap eIdxMap = reindexedGraph.edgesReIndexing().get();
					for (int eBuilder = 0; eBuilder < m; eBuilder++) {
						edgeRef[eBuilder] = edgeRefTemp[eIdxMap.reIndexedToOrig(eBuilder)];
						twin[eBuilder] = eIdxMap.origToReIndexed(twinTemp[eIdxMap.reIndexedToOrig(eBuilder)]);
					}
				} else {
					for (int eBuilder = 0; eBuilder < m; eBuilder++) {
						edgeRef[eBuilder] = edgeRefTemp[eBuilder];
						twin[eBuilder] = twinTemp[eBuilder];
					}
				}
				return new ResidualGraph(gOrig, g, edgeRef, twin);
			}

		}

	}

	private static class IndexNetFromObjNet<E> implements IFlowNetwork {

		private final FlowNetwork<?, E> idNet;
		final IndexIdMap<E> eiMap;

		IndexNetFromObjNet(FlowNetwork<?, E> idNet, IndexIdMap<E> eiMap) {
			if (idNet instanceof NetImplEdgeIWeights || idNet instanceof NetImplEdgeIWeightsInt)
				throw new IllegalArgumentException("net is already an index flow network");
			this.idNet = Objects.requireNonNull(idNet);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		FlowNetwork<?, E> idNet() {
			return idNet;
		}

		@Override
		public double getCapacity(int edge) {
			return idNet.getCapacity(eiMap.indexToId(edge));
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			idNet.setCapacity(eiMap.indexToId(edge), capacity);
		}

		@Override
		public double getFlow(int edge) {
			return idNet.getFlow(eiMap.indexToId(edge));
		}

		@Override
		public void setFlow(int edge, double flow) {
			idNet.setFlow(eiMap.indexToId(edge), flow);
		}
	}

	private static class IndexNetFromINet implements IFlowNetwork {

		private final IFlowNetwork idNet;
		final IndexIntIdMap eiMap;

		IndexNetFromINet(IFlowNetwork idNet, IndexIntIdMap eiMap) {
			if (idNet instanceof NetImplEdgeIWeights || idNet instanceof NetImplEdgeIWeightsInt)
				throw new IllegalArgumentException("net is already an index flow network");
			this.idNet = Objects.requireNonNull(idNet);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		IFlowNetwork idNet() {
			return idNet;
		}

		@Override
		public double getCapacity(int edge) {
			return idNet.getCapacity(eiMap.indexToIdInt(edge));
		}

		@Override
		public void setCapacity(int edge, double capacity) {
			idNet.setCapacity(eiMap.indexToIdInt(edge), capacity);
		}

		@Override
		public double getFlow(int edge) {
			return idNet.getFlow(eiMap.indexToIdInt(edge));
		}

		@Override
		public void setFlow(int edge, double flow) {
			idNet.setFlow(eiMap.indexToIdInt(edge), flow);
		}
	}

	private static class IndexNetFromObjNetInt<E> extends IndexNetFromObjNet<E> implements IFlowNetworkInt {

		IndexNetFromObjNetInt(FlowNetworkInt<?, E> idNet, IndexIdMap<E> eiMap) {
			super(idNet, eiMap);
		}

		@Override
		FlowNetworkInt<?, E> idNet() {
			return (FlowNetworkInt<?, E>) super.idNet();
		}

		@Override
		public int getCapacityInt(int edge) {
			return idNet().getCapacityInt(eiMap.indexToId(edge));
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			idNet().setCapacity(eiMap.indexToId(edge), capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return idNet().getFlowInt(eiMap.indexToId(edge));
		}

		@Override
		public void setFlow(int edge, int flow) {
			idNet().setFlow(eiMap.indexToId(edge), flow);
		}

		@Deprecated
		@Override
		public double getCapacity(int edge) {
			return IFlowNetworkInt.super.getCapacity(edge);
		}

		@Deprecated
		@Override
		public void setCapacity(int edge, double capacity) {
			IFlowNetworkInt.super.setCapacity(edge, capacity);
		}

		@Deprecated
		@Override
		public double getFlow(int edge) {
			return IFlowNetworkInt.super.getFlow(edge);
		}

		@Deprecated
		@Override
		public void setFlow(int edge, double flow) {
			IFlowNetworkInt.super.setFlow(edge, flow);
		}
	}

	private static class IndexNetFromINetInt extends IndexNetFromINet implements IFlowNetworkInt {

		IndexNetFromINetInt(IFlowNetworkInt idNet, IndexIntIdMap eiMap) {
			super(idNet, eiMap);
		}

		@Override
		IFlowNetworkInt idNet() {
			return (IFlowNetworkInt) super.idNet();
		}

		@Override
		public int getCapacityInt(int edge) {
			return idNet().getCapacityInt(eiMap.indexToIdInt(edge));
		}

		@Override
		public void setCapacity(int edge, int capacity) {
			idNet().setCapacity(eiMap.indexToIdInt(edge), capacity);
		}

		@Override
		public int getFlowInt(int edge) {
			return idNet().getFlowInt(eiMap.indexToIdInt(edge));
		}

		@Override
		public void setFlow(int edge, int flow) {
			idNet().setFlow(eiMap.indexToIdInt(edge), flow);
		}

		@Deprecated
		@Override
		public double getCapacity(int edge) {
			return IFlowNetworkInt.super.getCapacity(edge);
		}

		@Deprecated
		@Override
		public void setCapacity(int edge, double capacity) {
			IFlowNetworkInt.super.setCapacity(edge, capacity);
		}

		@Deprecated
		@Override
		public double getFlow(int edge) {
			return IFlowNetworkInt.super.getFlow(edge);
		}

		@Deprecated
		@Override
		public void setFlow(int edge, double flow) {
			IFlowNetworkInt.super.setFlow(edge, flow);
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> IFlowNetwork indexNetFromNet(FlowNetwork<V, E> net, IndexIdMap<E> eiMap) {
		if (net instanceof NetImplEdgeIWeightsInt) {
			/* Create a network from the underlying index weights containers */
			NetImplEdgeIWeightsInt net0 = (NetImplEdgeIWeightsInt) net;
			IWeightsInt capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, (IndexIdMap<Integer>) eiMap);
			IWeightsInt flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, (IndexIdMap<Integer>) eiMap);
			return new NetImplEdgeIWeightsInt(capacityWeights, flowWeights);

		} else if (net instanceof NetImplEdgeIWeights) {
			/* Create a network from the underlying index weights containers */
			NetImplEdgeIWeights net0 = (NetImplEdgeIWeights) net;
			IWeightsDouble capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, (IndexIdMap<Integer>) eiMap);
			IWeightsDouble flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, (IndexIdMap<Integer>) eiMap);
			return new NetImplEdgeIWeights(capacityWeights, flowWeights);

		} else if (net instanceof NetImplEdgeWeightsInt) {
			/* Create a network from the underlying index weights containers */
			NetImplEdgeWeightsInt<V, E> net0 = (NetImplEdgeWeightsInt<V, E>) net;
			IWeightsInt capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			IWeightsInt flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeIWeightsInt(capacityWeights, flowWeights);

		} else if (net instanceof NetImplEdgeWeights) {
			/* Create a network from the underlying index weights containers */
			NetImplEdgeWeights<V, E> net0 = (NetImplEdgeWeights<V, E>) net;
			IWeightsDouble capacityWeights = IndexIdMaps.idToIndexWeights(net0.capacities, eiMap);
			IWeightsDouble flowWeights = IndexIdMaps.idToIndexWeights(net0.flows, eiMap);
			return new NetImplEdgeIWeights(capacityWeights, flowWeights);

		} else if (net instanceof IFlowNetworkInt && eiMap instanceof IndexIntIdMap) {
			/* Unknown int weight function, return a mapped wrapper */
			IFlowNetworkInt netInt = (IFlowNetworkInt) net;
			IndexIntIdMap eiMap0 = (IndexIntIdMap) eiMap;
			return new IndexNetFromINetInt(netInt, eiMap0);

		} else if (net instanceof IFlowNetwork && eiMap instanceof IndexIntIdMap) {
			/* Unknown weight function, return a mapped wrapper */
			IFlowNetwork netInt = (IFlowNetwork) net;
			IndexIntIdMap eiMap0 = (IndexIntIdMap) eiMap;
			return new IndexNetFromINet(netInt, eiMap0);

		} else if (net instanceof FlowNetworkInt) {
			/* Unknown int weight function, return a mapped wrapper */
			FlowNetworkInt<V, E> netInt = (FlowNetworkInt<V, E>) net;
			return new IndexNetFromObjNetInt<>(netInt, eiMap);

		} else {
			/* Unknown weight function, return a mapped wrapper */
			return new IndexNetFromObjNet<>(net, eiMap);
		}
	}

	static double hugeCapacity(IndexGraph g, IFlowNetwork net, IntCollection sources, IntCollection sinks) {
		if (net instanceof IFlowNetworkInt)
			return hugeCapacityLong(g, (IFlowNetworkInt) net, sources, sinks);

		double sourcesOutCapacity = 0;
		double sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += net.getCapacity(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += net.getCapacity(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static int hugeCapacity(IndexGraph g, IFlowNetworkInt net, IntCollection sources, IntCollection sinks) {
		long hugeCapacity = hugeCapacityLong(g, net, sources, sinks);
		int hugeCapacityInt = (int) hugeCapacity;
		if (hugeCapacityInt != hugeCapacity)
			throw new AssertionError("integer overflow, huge capacity can't fit in 32bit int");
		return hugeCapacityInt;
	}

	static long hugeCapacityLong(IndexGraph g, IFlowNetworkInt net, IntCollection sources, IntCollection sinks) {
		long sourcesOutCapacity = 0;
		long sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += net.getCapacityInt(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += net.getCapacityInt(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static double vertexMaxSupply(IndexGraph g, IFlowNetwork net, int v) {
		if (net instanceof IFlowNetworkInt)
			return vertexMaxSupply(g, (IFlowNetworkInt) net, v);

		double maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += net.getCapacity(e);
		return maxSupply;
	}

	static int vertexMaxSupply(IndexGraph g, IFlowNetworkInt net, int v) {
		long maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += net.getCapacityInt(e);
		int maxSupplyInt = (int) maxSupply;
		if (maxSupplyInt != maxSupply)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxSupplyInt;
	}

	static double vertexMaxDemand(IndexGraph g, IFlowNetwork net, int v) {
		if (net instanceof IFlowNetworkInt)
			return vertexMaxDemand(g, (IFlowNetworkInt) net, v);

		double maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += net.getCapacity(e);
		return maxDemand;
	}

	static int vertexMaxDemand(IndexGraph g, IFlowNetworkInt net, int v) {
		long maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += net.getCapacityInt(e);
		int maxDemandInt = (int) maxDemand;
		if (maxDemandInt != maxDemand)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxDemandInt;
	}

}
