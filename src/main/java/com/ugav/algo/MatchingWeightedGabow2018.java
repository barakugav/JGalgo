package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Utils.NullList;
import com.ugav.algo.Utils.QueueIntFixSize;

public class MatchingWeightedGabow2018 implements MatchingWeighted {

	public static boolean debug_print = false; // TODO

	private static void debugPrint(String s) {
		if (debug_print)
			System.out.print(s);
	}

	/*
	 * O(mn + n^2logn)
	 */

	private MatchingWeightedGabow2018() {
	}

	private static final MatchingWeightedGabow2018 INSTANCE = new MatchingWeightedGabow2018();

	public static MatchingWeightedGabow2018 getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		return new Worker<>(g, w).calcMaxMatching(false);

	}

	@Override
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
		if (g.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		return new Worker<>(g, w).calcMaxMatching(true);
	}

	private static class Worker<E> {

		private final Graph<E> g;
		private final WeightFunction<E> w;

		private final Edge<E>[] matched;

		private final Blossom<E>[] blossoms;
		private final UnionFind find0;
		private final Blossom<E>[] find0Blossoms;
		private final SplitFindMin<EdgeEvent<E>> find1;
		private final int[] vToFind1Idx;
		private int find1IdxNext;
		private final Blossom<E>[] find1Blossoms;
		private int blossomVisitIdx;

		private double deltaTotal;
		/* y0(u) */
		private final double[] dualValBase;

		private final EdgeEvent<E>[] vToGrowEvent;
		private final Heap<EdgeEvent<E>> growEvents;
		private final Heap<EdgeEvent<E>> blossomEvents;
		private final Heap<Blossom<E>> expandEvents;

		private static class Blossom<E> {

			int base;
			Blossom<E> parent;
			Blossom<E> child;
			Blossom<E> left;
			Blossom<E> right;
			Edge<E> toLeftEdge;
			Edge<E> toRightEdge;

			int root;
			Edge<E> treeParentEdge;
			boolean isEven;

			int find1SeqBegin;
			int find1SeqEnd;

			double z0;
			double delta0;
			double delta1;
			double deltaOdd;

			int lastVisitIdx;

			Heap.Handle<EdgeEvent<E>> growHandle;
			double expandDelta;
			Heap.Handle<Blossom<E>> expandHandle;

			boolean isSingleton() {
				return child == null;
			}

			@Override
			public String toString() {
				return "" + (root == -1 ? 'X' : isEven ? 'E' : 'O') + base;
			}

		}

		private static class EdgeEvent<E> {
			final Edge<E> e;
			final double slack;

			EdgeEvent(Edge<E> e, double slack) {
				this.e = e;
				this.slack = slack;
				if (slack < 0)
					System.out.print(""); // TODO
			}

			@Override
			public String toString() {
				return "" + e + "[" + slack + "]";
			}
		}

		private void debugStream() {
			if (!debug_print)
				return;
			debugPrint(" " + Arrays.asList(blossoms).stream().map(b -> "" + dualVal(b.base))
					.collect(Collectors.joining(", ", "[", "]")));
			debugPrint(" " + Arrays.asList(blossoms).stream().mapMulti((Blossom<E> b, Consumer<Blossom<E>> next) -> {
				for (; b != null; b = b.parent)
					next.accept(b);
			}).distinct().filter(b -> !b.isSingleton()).map(b -> "" + b + " " + zb(b))
					.collect(Collectors.joining(", ", "[", "]")));
		}

		Worker(Graph<E> g, WeightFunction<E> w) {
			this.g = g;
			this.w = w;

			int n = g.vertices();
			matched = new Edge[n];

			blossoms = new Blossom[n];
			find0 = new UnionFindArray(n);
			find0Blossoms = new Blossom[n];
			find1 = new SplitFindMinArray<>();
			vToFind1Idx = new int[n];
			find1Blossoms = new Blossom[n];
			blossomVisitIdx = 0;

			deltaTotal = 0;
			dualValBase = new double[n];

			vToGrowEvent = new EdgeEvent[n];
			growEvents = new HeapFibonacci<>((e1, e2) -> Double.compare(growEventsKey(e1), growEventsKey(e2)));
			blossomEvents = new HeapFibonacci<>((e1, e2) -> Double.compare(e1.slack, e2.slack));
			expandEvents = new HeapFibonacci<>((b1, b2) -> Double.compare(b1.expandDelta, b2.expandDelta));
		}

		private Collection<Edge<E>> calcMaxMatching(boolean perfect) {
			debugPrint("  == begin\n"); // TODO remove
			int n = g.vertices();

			double maxWeight = Double.MIN_VALUE;
			for (Edge<E> e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e));
			double delta1Threshold = maxWeight / 2;
			for (int u = 0; u < n; u++)
				dualValBase[u] = delta1Threshold;

			for (int u = 0; u < n; u++) {
				Blossom<E> b = blossoms[u] = new Blossom<>();
				b.base = u;
				b.z0 = maxWeight * 1024; // TODO remove
			}

			Comparator<EdgeEvent<E>> edgeSlackBarComparator = (e1, e2) -> {
				return e2 == null ? -1 : e1 == null ? 1 : Double.compare(e1.slack, e2.slack);
			};

			mainLoop: for (;;) {
				// Init unmatched blossoms as even and all other as out
				debugPrint("roots:"); // TODO
				find0.clear();
				for (int u = 0; u < n; u++)
					find0.make();
				Arrays.fill(vToFind1Idx, -1);
				find1.init(new NullList<>(n), edgeSlackBarComparator);
				find1IdxNext = 0;
				forEachTopBlossom(b -> {
					if (matched[b.base] != null) {
						// Out blossom
						find1InitIndexing(b);
						find1Split(b);
						b.delta1 = deltaTotal;

					} else {
						// Unmatched even blossom
						b.root = b.base;
						b.isEven = true;
						b.delta0 = deltaTotal;
						int base = b.base;
						forEachVertexInBlossom(b, u -> {
							blossoms[u].isEven = true;
							find0.union(base, u);
						});
						find0Blossoms[find0.find(base)] = b;
						debugPrint(" " + b); // TODO
					}
				});
				debugPrint("\n");
				// Insert grow and blossom events into heaps
				forEachTopBlossom(U -> {
					if (matched[U.base] == null) {
						forEachVertexInBlossom(U, this::insertGrowEventsFromVertex);
						forEachVertexInBlossom(U, this::insertBlossomEventsFromVertex);
					}
				});

				currentSearch: for (;;) {
					double delta1 = delta1Threshold;
					if (!growEvents.isEmpty() && growEventsKey(growEvents.findMin()) < deltaTotal) {
						EdgeEvent<E> e = growEvents.findMin();
						double slackBar = e.slack;
						double slack = growEventsKey(e);
						System.out.print("");
					}
					double delta2 = growEvents.isEmpty() ? Double.MAX_VALUE : growEventsKey(growEvents.findMin());
					double delta3 = blossomEvents.isEmpty() ? Double.MAX_VALUE : blossomEvents.findMin().slack / 2;
					double delta4 = expandEvents.isEmpty() ? Double.MAX_VALUE : expandEvents.findMin().expandDelta;

					if (!growEvents.isEmpty())
						debugPrint("    min grow event " + growEvents.findMin() + " "
								+ growEventsKey(growEvents.findMin()) + "\n");
					if (!blossomEvents.isEmpty()) {
						debugPrint("    min blossom event " + blossomEvents.findMin() + " "
								+ blossomEvents.findMin().slack / 2 + "\n");
//						debugPrint(
//								"    min blossom event " + blossomEvents.findMin() + " "
//										+ blossomEvents.findMin().slack / 2 + " " + blossomEvents.stream()
//												.map(b -> "" + b + " " + b.slack / 2).collect(Collectors.toList())
//										+ "\n");
					}
					if (!expandEvents.isEmpty())
						debugPrint("    min expand event " + expandEvents.findMin() + " "
								+ expandEvents.findMin().expandDelta + "\n");

					double delta = Math.min(delta2, Math.min(delta3, delta4));
					if (debug_print && delta == 1.5)
						System.out.print(""); // TODO
					if (delta == Double.MAX_VALUE || (!perfect && delta1 <= delta)) {
						debugPrint("");
						break mainLoop;
					}
					if (debug_print && delta == delta3 && todoRemoveEn()) {// TODO
						debugPrint("min ");
						todoRemove(blossomEvents.findMin().e);
					}
					assert0(delta >= deltaTotal);
					debugPrint("dual " + delta + "(+" + (delta - deltaTotal) + ")");
					deltaTotal = delta;
					debugStream();
					debugPrint("\n");

					if (delta == delta2)
						growStep();
					else if (delta == delta3) {
						assert0(deltaTotal == blossomEvents.findMin().slack / 2);
						Edge<E> e = blossomEvents.extractMin().e;
						todoRemove(e);
						assert0(isEven(e.u()) && isEven(e.v()));

						if (find0(e.u()).root == find0(e.v()).root)
							blossomStep(e);
						else {
							augmentStep(e);
							break currentSearch;
						}
					} else if (delta == delta4)
						expandStep();
					else
						assert0(false);
				}

				// Update dual values
				for (int u = 0; u < n; u++)
					dualValBase[u] = dualVal(u);
				forEachBlossom(b -> {
					if (!b.isSingleton())
						b.z0 = zb(b);
					b.delta0 = b.delta1 = b.deltaOdd = 0;
				});
				delta1Threshold -= deltaTotal;
				deltaTotal = 0;

				// Reset blossom tree and heaps
				forEachBlossom(b -> {
					b.root = -1;
					b.treeParentEdge = null;
					b.isEven = false;
					b.find1SeqBegin = b.find1SeqEnd = 0;
					b.growHandle = null;
					b.expandDelta = 0;
					b.expandHandle = null;
				});
				Arrays.fill(vToGrowEvent, null);
				growEvents.clear();
				blossomEvents.clear();
				expandEvents.clear();
			}

			return getMatchedEdges();
		}

		private List<Edge<E>> getMatchedEdges() { // TODO
			List<Edge<E>> res = new ArrayList<>();
			int n = g.vertices();
			for (int u = 0; u < n; u++)
				if (matched[u] != null && u < matched[u].v())
					res.add(matched[u]);
			return res;
		}

		private void growStep() {
			// Grow step
			assert0(deltaTotal == growEventsKey(growEvents.findMin()));
			Edge<E> e = growEvents.extractMin().e;

			Blossom<E> U = find0(e.u()), V = find1(e.v());
			assert0(!V.isEven && !isInTree(V));

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = e.twin();
			V.isEven = false; // TODO remove
			V.delta1 = deltaTotal;
			assert0(V.growHandle.get().e == e);
			V.growHandle = null;
			if (!V.isSingleton()) {
				V.expandDelta = V.z0 / 2 + V.delta1;
				V.expandHandle = expandEvents.insert(V);
			}
			debugPrint("[" + find0(V.root) + "] add " + V); // TODO

			// Immediately add it's matched edge and vertex as even vertex
			Edge<E> matchedEdge = matched[V.base];
			V = topBlossom(matchedEdge.v());
			V.root = U.root;
			V.treeParentEdge = matchedEdge.twin();
			if (V.growHandle != null) {
				growEvents.removeHandle(V.growHandle);
				V.growHandle = null;
			}
			makeEven(V);
			debugPrint(", " + V + "\n"); // TODO
		}

		private void blossomStep(Edge<E> e) {
			assert0(isEven(e.u()) && isEven(e.v()));
			Blossom<E> U = find0(e.u()), V = find0(e.v());
			if (U == V)
				return; // Edge in same blossom, ignore
			debugPrint("new blossom"); // TODO remove

			Blossom<E> base = lcaInSearchTree(U, V);
			Blossom<E> newb = new Blossom<>();
			newb.root = base.root;
			newb.treeParentEdge = base.treeParentEdge;
			newb.isEven = true;
			newb.base = base.base;
			newb.child = base;
			newb.delta0 = deltaTotal;
			QueueIntFixSize unionQueue = new QueueIntFixSize(g.vertices() + 1);
			QueueIntFixSize scanQueue = new QueueIntFixSize(g.vertices());
			for (Blossom<E> p : new Blossom[] { U, V }) {
				boolean prevIsRight = p == U;
				Blossom<E> prev = p == U ? V : U;
				Edge<E> toPrevEdge = p == U ? e : e.twin();

				while (true) {
					// handle even sub blossom
					assert0(p.isEven);
					if (!p.isSingleton())
						p.z0 = zb(p); // TODO not sure this is true
					p.parent = newb;
					connectSubBlossoms(p, prev, toPrevEdge, !prevIsRight);
					unionQueue.push(p.base);

					if (p == base)
						break;
					debugPrint(" " + p); // TODO remove
					prev = p;
					toPrevEdge = matched[p.base].twin();
					assert0(matched[p.base] == p.treeParentEdge);
					p = topBlossom(toPrevEdge.u());

					// handle odd vertex
					assert0(!p.isEven);
					debugPrint(" " + p); // TODO remove
					p.deltaOdd += deltaTotal - p.delta1;
					if (!p.isSingleton())
						p.z0 = zb(p); // TODO not sure this is true
//					p.deltaOdd = 0;
					p.parent = newb;
					connectSubBlossoms(p, prev, toPrevEdge, !prevIsRight);
					forEachVertexInBlossom(p, v -> {
						blossoms[v].isEven = true;
						unionQueue.push(v);
						scanQueue.push(v);
					});
					p.delta0 = deltaTotal;
					if (!p.isSingleton()) {
						expandEvents.removeHandle(p.expandHandle);
						p.expandHandle = null;
					}

					prev = p;
					toPrevEdge = p.treeParentEdge.twin();
					p = topBlossom(toPrevEdge.u());
				}
			}
			debugPrint(" " + base + " as " + newb + "\n"); // TODO remove
			while (!unionQueue.isEmpty())
				find0.union(newb.base, unionQueue.pop());
			find0Blossoms[find0.find(newb.base)] = newb;

			while (!scanQueue.isEmpty()) {
				int u = scanQueue.pop();
				insertGrowEventsFromVertex(u);
				insertBlossomEventsFromVertex(u);
			}
		}

		private void makeEven(Blossom<E> V) {
			V.isEven = true;
			V.delta0 = deltaTotal;

			int base = V.base;
			forEachVertexInBlossom(V, v -> {
				blossoms[v].isEven = true;
				find0.union(base, v);
			});
			find0Blossoms[find0.find(base)] = V;
			forEachVertexInBlossom(V, this::insertGrowEventsFromVertex);
			forEachVertexInBlossom(V, this::insertBlossomEventsFromVertex);
		}

		private void expandStep() {
			assert0(deltaTotal == expandEvents.findMin().expandDelta);
			Blossom<E> topBlossom = expandEvents.extractMin();

			assert0(topBlossom.root != -1 && !topBlossom.isEven && !topBlossom.isSingleton() && zb(topBlossom) <= 0);
			debugPrint("expand blossom " + topBlossom + " ["); // TODO remove

			// Remove parent pointer from all children
			for (Blossom<E> p = topBlossom.child;;) {
				debugPrint(" " + p); // TODO
				p.parent = null;
				p = p.right;
				if (p == topBlossom.child)
					break;
			}
			debugPrint("]"); // TODO
			final Blossom<E> b = subBlossom(topBlossom.treeParentEdge.u(), null);
			final Blossom<E> subBase = subBlossom(topBlossom.base, null);
			topBlossom.deltaOdd += deltaTotal - topBlossom.delta1;
			topBlossom.delta0 = deltaTotal;
			debugPrint(" deltaOdd=" + topBlossom.deltaOdd + " <" + b + ", " + subBase + ">"); // TODO

			// Iterate over sub blossom that should stay in the tree
			boolean left = matched[b.toLeftEdge.u()] == b.toLeftEdge;
			for (Blossom<E> p = b;;) {
				// sub blossom odd
				p.root = topBlossom.root;
				p.treeParentEdge = left ? p.toRightEdge : p.toLeftEdge;
				p.isEven = false;
				p.delta1 = deltaTotal;
				p.deltaOdd = topBlossom.deltaOdd + topBlossom.z0 / 2;
				find1Split(p);
				assert0(p.expandHandle == null);
				if (!p.isSingleton()) {
					p.expandDelta = p.z0 / 2 + p.delta1;
					p.expandHandle = expandEvents.insert(p);
				}
				debugPrint(" " + p); // TODO
				if (p == subBase)
					break;
				p = left ? p.left : p.right;

				// sub blossom even
				p.root = topBlossom.root;
				p.treeParentEdge = left ? p.toRightEdge : p.toLeftEdge;
				p.deltaOdd = topBlossom.deltaOdd + topBlossom.z0 / 2;
				makeEven(p);
				debugPrint(" " + p); // TODO
				p = left ? p.left : p.right;
			}
			b.treeParentEdge = topBlossom.treeParentEdge;

			// Iterate over sub blossom that should not stay in the tree
			debugPrint(" ("); // TODO
			for (Blossom<E> p = subBase;;) {
				p = left ? p.left : p.right;
				if (p == b)
					break;
				p.root = -1;
				p.treeParentEdge = null;
				p.isEven = false;
				find1Split(p);
				p.deltaOdd = topBlossom.deltaOdd + topBlossom.z0 / 2;
				assert0(p.growHandle == null);
				EdgeEvent<E> inEdgeEvent = find1.getKey(find1.findMin(vToFind1Idx[p.base]));
				if (inEdgeEvent != null)
					p.growHandle = growEvents.insert(inEdgeEvent);
				debugPrint(" " + p); // TODO

				p = left ? p.left : p.right;
				assert0(p != b);
				p.root = -1;
				p.treeParentEdge = null;
				p.isEven = false;
				find1Split(p);
				p.deltaOdd = topBlossom.deltaOdd + topBlossom.z0 / 2;
				assert0(p.growHandle == null);
				inEdgeEvent = find1.getKey(find1.findMin(vToFind1Idx[p.base]));
				if (inEdgeEvent != null)
					p.growHandle = growEvents.insert(inEdgeEvent);
				debugPrint(" " + p); // TODO
			}
			debugPrint(")\n"); // TODO
			for (Blossom<E> p = b;;) {
				Blossom<E> next = p.left;
				p.right = p.left = null;
				p.toRightEdge = p.toLeftEdge = null;
				if (next == b)
					break;
				p = next;
			}

//			debugPrint(" " + Arrays.asList(blossoms).stream().map(q -> "" + dualVal(q.base))
//					.collect(Collectors.joining(", ", "[", "]")));
//			debugPrint(" " + Arrays.asList(blossoms).stream().mapMulti((Blossom<E> q, Consumer<Blossom<E>> next) -> {
//				for (; q != null; q = q.parent)
//					next.accept(q);
//			}).distinct().filter(q -> !q.isSingleton()).map(q -> "" + q + " " + zb(q))
//					.collect(Collectors.joining(", ", "[", "]")));
//			debugPrint("\n"); // TODO
		}

		private void augmentStep(Edge<E> bridge) {

			Blossom<E> U = topBlossom(bridge.u()), V = topBlossom(bridge.v());
			for (Blossom<E> b : new Blossom[] { U, V }) {
				assert0(b.isEven);
				Edge<E> e = null;
				for (int u = b == U ? bridge.u() : bridge.v();;) {
					assert0(b.isEven);
					augmentPath(b, u);
					if (e != null) {
						matched[e.u()] = e;
						matched[e.v()] = e.twin();
					}
					if (b.treeParentEdge == null)
						break;
					// Odd
					b = topBlossom(b.treeParentEdge.v());
					assert0(!b.isEven);
					u = b.treeParentEdge.u();
					augmentPath(b, u);

					// Even
					e = b.treeParentEdge;
					u = e.v();
					b = topBlossom(e.v());
				}
			}
			matched[bridge.u()] = bridge;
			matched[bridge.v()] = bridge.twin();

			debugPrint("aug " + bridge + " " + getMatchedEdges() + "\n");
		}

		private boolean isEven(int v) {
			return blossoms[v].isEven;
		}

		private boolean isInTree(int v) {
			return topBlossom(v).root != -1;
		}

		private boolean isInTree(Blossom<E> b) {
			return b.parent != null ? isInTree(b.base) : b.root != -1;
		}

		private Blossom<E> find0(int v) {
			return find0Blossoms[find0.find(v)];
		}

		private Blossom<E> find1(int v) {
			int idx = vToFind1Idx[v];
			return idx < 0 ? null : find1Blossoms[find1.find(idx)];
		}

		private int find1InitIndexing(Blossom<E> b) {
			b.find1SeqBegin = find1IdxNext;
			if (b.child == null) {
				b.isEven = false;
				vToFind1Idx[b.base] = find1IdxNext++;
			} else {
				for (Blossom<E> sub = b.child;;) {
					find1InitIndexing(sub);
					sub = sub.right;
					if (sub == b.child)
						break;
				}
			}
			return b.find1SeqEnd = find1IdxNext;
		}

		private void find1Split(Blossom<E> b) {
			int begin = b.find1SeqBegin, end = b.find1SeqEnd;
			Blossom<E> b1 = begin > 0 ? find1Blossoms[find1.find(begin - 1)] : null;
			Blossom<E> b2 = end < find1Blossoms.length ? find1Blossoms[find1.find(end)] : null;

			if (begin > 0) {
				find1.split(begin);
				find1Blossoms[find1.find(begin - 1)] = b1;
			}
			if (end < find1Blossoms.length) {
				find1.split(end);
				find1Blossoms[find1.find(end)] = b2;
			}
			find1Blossoms[find1.find(b.find1SeqBegin)] = b;
		}

		private Blossom<E> topBlossom(int v) {
			return isEven(v) ? find0(v) : find1(v);
		}

		private Blossom<E> subBlossom(int v, Blossom<E> parent) {
			assert0(blossoms[v] != null);
			return subBlossom(blossoms[v], parent);
		}

		private static <E> Blossom<E> subBlossom(Blossom<E> b, Blossom<E> parent) {
			while (b.parent != parent)
				b = b.parent;
			return b;
		}

		private void forEachBlossom(Consumer<Blossom<E>> f) {
			int n = g.vertices();
			int visitIdx = ++blossomVisitIdx;
			for (int v = 0; v < n; v++) {
				for (Blossom<E> b = blossoms[v]; b.lastVisitIdx != visitIdx; b = b.parent) {
					b.lastVisitIdx = visitIdx;
					f.accept(b);
					if (b.parent == null)
						break;
				}
			}
		}

		private void forEachTopBlossom(Consumer<Blossom<E>> f) {
			int n = g.vertices();
			int visitIdx = ++blossomVisitIdx;
			for (int v = 0; v < n; v++) {
				for (Blossom<E> b = blossoms[v]; b.lastVisitIdx != visitIdx; b = b.parent) {
					b.lastVisitIdx = visitIdx;
					if (b.parent == null) {
						if (b.child != null) {
							// Mark children as visited in case blossom expand
							for (Blossom<E> c = b.child;;) {
								c.lastVisitIdx = visitIdx;
								c = c.left;
								if (c == b.child)
									break;
							}
						}
						f.accept(b);
						break;
					}
				}
			}
		}

		private static <E> void forEachVertexInBlossom(Blossom<E> b, IntConsumer f) {
			if (b.child == null) {
				f.accept(b.base);
				return;
			}
			for (Blossom<E> sub = b.child;;) {
				forEachVertexInBlossom(sub, f);
				sub = sub.right;
				if (sub == b.child)
					break;
			}
		}

		private double zb(Blossom<E> b) {
			assert0(!b.isSingleton());
			double zb = b.z0;
			if (b.parent == null && b.root != -1)
				zb += 2 * (b.isEven ? +(deltaTotal - b.delta0) : -(deltaTotal - b.delta1));
			return zb;
		}

		private double growEventsKey(EdgeEvent<E> event) {
			int v = event.e.v();
			assert0(!isEven(v));
			return find1(v).deltaOdd + event.slack;
		}

		private void insertGrowEventsFromVertex(int u) {
			double Yu = deltaTotal + dualVal(u);
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (isEven(v))
					continue;
//				if (e.u() == 55 && e.v() == 23)
//					System.out.print(""); // TODO
				double slackBar = Yu + dualValBase[v] - w.weight(e);
//				if (e.u() == 55 && e.v() == 23)
//					System.out.print("#" + slackBar + "#"); // TODO
				if (vToGrowEvent[v] == null || slackBar < vToGrowEvent[v].slack) {
					EdgeEvent<E> event = vToGrowEvent[v] = new EdgeEvent<>(e, slackBar);
					if (!find1.decreaseKey(vToFind1Idx[v], event))
						continue;
					assert0(find1.getKey(find1.findMin(vToFind1Idx[v])) == event);

					Blossom<E> V = find1(v);
					if (!isInTree(V)) {
						if (V.growHandle == null)
							V.growHandle = growEvents.insert(event);
						else
							growEvents.decreaseKey(V.growHandle, event);
					}
				}
			}
		}

		private static final boolean todoRemoveEn = false;

		boolean todoRemoveEn() {
			return todoRemoveEn && debug_print;
		}

		private void todoRemove(Edge<E> e) { // TODO
			if (e.u() == 3 && e.v() == 1)
				System.out.print("");
			double Yu = deltaTotal + dualVal(e.u());
			double Yv = deltaTotal + dualVal(e.v());
			double slackBar = Yu + Yv - w.weight(e);
			double delta0 = slackBar / 2;
			if (todoRemoveEn())
				System.out.println("" + e + ": " + deltaTotal + " " + dualVal(e.u()) + " " + dualVal(e.v()) + " "
						+ slackBar + " " + delta0);
		}

		private void insertBlossomEventsFromVertex(int u) {
			assert0(isEven(u));
			Blossom<E> U = find0(u);
			double Yu = deltaTotal + dualVal(u);
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (!isEven(v))
					continue;
				Blossom<E> V = find0(v);
				if (U == V)
					continue;
				double Yv = deltaTotal + dualVal(v);
				double slackBar = Yu + Yv - w.weight(e);

				assert0(slackBar >= 0);
				blossomEvents.insert(new EdgeEvent<>(e, slackBar));
//				todoRemove(e);
			}
		};

		private void augmentPath(Blossom<E> b, int u) {
			if (b.base == u)
				return;

			final Blossom<E> b0 = subBlossom(u, b);
			final Blossom<E> bk = subBlossom(b.base, b);
			boolean left = matched[b0.toLeftEdge.u()] == b0.toLeftEdge;

			int newBase = u;
			Edge<E> e = null;
			for (Blossom<E> p = b0;;) {
				augmentPath(p, u);
				if (e != null) {
					matched[e.u()] = e;
					matched[e.v()] = e.twin();
				}
				if (p == bk) {
					b.base = newBase;
					return;
				}
				if (left) {
					p = p.left;
					u = p.toLeftEdge.u();
				} else {
					p = p.right;
					u = p.toRightEdge.u();
				}
				assert0(p != bk);
				augmentPath(p, u);

				if (left) {
					e = p.toLeftEdge;
					u = p.toLeftEdge.v();
					p = p.left;
				} else {
					e = p.toRightEdge;
					u = p.toRightEdge.v();
					p = p.right;
				}
			}
		}

		private List<Edge<E>> findAugPath(Blossom<E> b, int u) {
			if (b.base == u)
				return new ArrayList<>(0);
			final Blossom<E> b0 = subBlossom(u, b);
			final Blossom<E> bk = subBlossom(b.base, b);
			boolean left = matched[b0.toLeftEdge.u()] == b0.toLeftEdge;
			List<Edge<E>> path = new ArrayList<>();
			for (Blossom<E> p = b0;;) {
				path.addAll(findAugPath(p, u));
				if (p == bk)
					return path;
				if (left) {
					path.add(p.toLeftEdge);
					p = p.left;
					u = p.toLeftEdge.u();
				} else {
					path.add(p.toRightEdge);
					p = p.right;
					u = p.toRightEdge.u();
				}
				assert0(p != bk);
				path.addAll(reverse(findAugPath(p, u)));
				if (left) {
					path.add(p.toLeftEdge);
					u = p.toLeftEdge.v();
					p = p.left;
				} else {
					path.add(p.toRightEdge);
					u = p.toRightEdge.v();
					p = p.right;
				}
			}
		}

		private static <E> List<E> reverse(List<E> l) {
			Collections.reverse(l);
			return l;
		}

		private double dualVal(int v) {
			Blossom<E> b = find1(v);
			double deltaB = b == null ? 0 : b.deltaOdd;
			double val = dualValBase[v] + deltaB;

			if (b == null)
				// v was part of an even blossom from the beginning of the current search
				val -= deltaTotal;
			else if (b.root != -1)
				// v was part of an out blossom, b is max blossom before v became even
				val += isEven(v) ? -(deltaTotal - b.delta0) : +(deltaTotal - b.delta1);
			return val;
		}

		private static <E> void connectSubBlossoms(Blossom<E> left, Blossom<E> right, Edge<E> leftToRightEdge,
				boolean reverse) {
			if (reverse) {
				Blossom<E> temp = left;
				left = right;
				right = temp;
				leftToRightEdge = leftToRightEdge.twin();
			}
			left.right = right;
			left.toRightEdge = leftToRightEdge;
			right.left = left;
			right.toLeftEdge = leftToRightEdge.twin();
		}

		private Blossom<E> lcaInSearchTree(Blossom<E> b1, Blossom<E> b2) {
			int visitIdx = ++blossomVisitIdx;
			for (Blossom<E>[] bs = new Blossom[] { b1, b2 };;) {
				if (bs[0] == null && bs[1] == null)
					return null;
				for (int i = 0; i < bs.length; i++) {
					Blossom<E> b = bs[i];
					if (b == null)
						continue;
					if (b.lastVisitIdx == visitIdx)
						return b;
					b.lastVisitIdx = visitIdx;
					bs[i] = b.treeParentEdge == null ? null : topBlossom(b.treeParentEdge.v());
				}
			}
		}

		private Blossom<E> lcaInBlossomTree(Blossom<E> b1, Blossom<E> b2) {
			int visitIdx = ++blossomVisitIdx;
			for (Blossom<E>[] bs = new Blossom[] { b1, b2 };;) {
				if (bs[0] == null && bs[1] == null)
					return null;
				for (int i = 0; i < bs.length; i++) {
					Blossom<E> b = bs[i];
					if (b == null)
						continue;
					if (b.lastVisitIdx == visitIdx)
						return b;
					b.lastVisitIdx = visitIdx;
					bs[i] = b.parent;
				}
			}
		}

		private String blossomsStr() { // TODO
			return Arrays.stream(blossoms).map(b -> {
				Blossom<E> top = topBlossom(b.base);
				return "" + (b.root == -1 ? 'X' : top.isEven ? 'E' : 'O') + top.base;
			}).collect(Collectors.toList()).toString();
		}

		// TODO remove
		private static void assert0(boolean condition) {
			if (!condition)
				throw new InternalError();
		}

	}

}
