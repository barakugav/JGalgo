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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

class BinarySearchTrees {

	private BinarySearchTrees() {}

	static <E, N extends Node<E, N>> N find(N root, Comparator<? super E> c, E e) {
		return findOrNeighbor(root, c, e, NeighborType.None);
	}

	static <E, N extends Node<E, N>> N findOrSmaller(N root, Comparator<? super E> c, E e) {
		return findOrNeighbor(root, c, e, NeighborType.Predecessor);
	}

	static <E, N extends Node<E, N>> N findOrGreater(N root, Comparator<? super E> c, E e) {
		return findOrNeighbor(root, c, e, NeighborType.Successor);
	}

	private static enum NeighborType {
		None, Predecessor, Successor,
	}

	private static <E, N extends Node<E, N>> N findOrNeighbor(N root, Comparator<? super E> c, E e,
			NeighborType neighborType) {
		if (root == null)
			return null;
		BiFunction<NeighborType, N, N> onLeftChildMissing = (nType, p) -> {
			switch (nType) {
				case None:
					return null;
				case Predecessor:
					return getPredecessor(p);
				case Successor:
					return p;
				default:
					throw new IllegalArgumentException("Unexpected value: " + neighborType);
			}
		};
		BiFunction<NeighborType, N, N> onRightChildMissing = (nType, p) -> {
			switch (nType) {
				case None:
					return null;
				case Predecessor:
					return p;
				case Successor:
					return getSuccessor(p);
				default:
					throw new IllegalArgumentException("Unexpected value: " + neighborType);
			}
		};
		if (c == null) {
			for (N p = root;;) {
				int cmp = Utils.cmpDefault(e, p.data);
				if (cmp == 0)
					return p;
				if (cmp < 0) {
					if (!p.hasLeftChild())
						return onLeftChildMissing.apply(neighborType, p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return onRightChildMissing.apply(neighborType, p);
					p = p.right;
				}
			}
		} else {
			for (N p = root;;) {
				int cmp = c.compare(e, p.data);
				if (cmp == 0)
					return p;
				if (cmp < 0) {
					if (!p.hasLeftChild())
						return onLeftChildMissing.apply(neighborType, p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return onRightChildMissing.apply(neighborType, p);
					p = p.right;
				}
			}
		}
	}

	static <E, N extends Node<E, N>> N findSmaller(N root, Comparator<? super E> c, E e) {
		if (root == null)
			return null;
		if (c == null) {
			for (N p = root;;) {
				int cmp = Utils.cmpDefault(e, p.data);
				if (cmp <= 0) {
					if (!p.hasLeftChild())
						return getPredecessor(p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return p;
					p = p.right;
				}
			}
		} else {
			for (N p = root;;) {
				int cmp = c.compare(e, p.data);
				if (cmp <= 0) {
					if (!p.hasLeftChild())
						return getPredecessor(p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return p;
					p = p.right;
				}
			}
		}
	}

	static <E, N extends Node<E, N>> N findGreater(N root, Comparator<? super E> c, E e) {
		if (root == null)
			return null;
		if (c == null) {
			for (N p = root;;) {
				int cmp = Utils.cmpDefault(e, p.data);
				if (cmp >= 0) {
					if (!p.hasRightChild())
						return getSuccessor(p);
					p = p.right;
				} else {
					if (!p.hasLeftChild())
						return p;
					p = p.left;
				}
			}
		} else {
			for (N p = root;;) {
				int cmp = c.compare(e, p.data);
				if (cmp >= 0) {
					if (!p.hasRightChild())
						return getSuccessor(p);
					p = p.right;
				} else {
					if (!p.hasLeftChild())
						return p;
					p = p.left;
				}
			}
		}
	}

	static <E, N extends Node<E, N>> N findMin(N root) {
		for (N p = root;; p = p.left)
			if (!p.hasLeftChild())
				return p;
	}

	static <E, N extends Node<E, N>> N findMax(N root) {
		for (N p = root;; p = p.right)
			if (!p.hasRightChild())
				return p;
	}

	static <E, N extends Node<E, N>> N getPredecessor(N n) {
		return getPredecessorInSubtree(n, null);
	}

	private static <E, N extends Node<E, N>> N getPredecessorInSubtree(N n, N subtreeRoot) {
		/* predecessor in left sub tree */
		if (n.hasLeftChild())
			for (N p = n.left;; p = p.right)
				if (!p.hasRightChild())
					return p;

		/* predecessor is some ancestor */
		N subtreeParent = subtreeRoot != null ? subtreeRoot.parent : null;
		for (N p = n; p.parent != subtreeParent; p = p.parent)
			if (p.isRightChild())
				return p.parent;
		return null;
	}

	static <E, N extends Node<E, N>> N getSuccessor(N n) {
		return getSuccessorInSubtree(n, null);
	}

	private static <E, N extends Node<E, N>> N getSuccessorInSubtree(N n, N subtreeRoot) {
		/* successor in right sub tree */
		if (n.hasRightChild())
			for (N p = n.right;; p = p.left)
				if (!p.hasLeftChild())
					return p;

		/* successor is some ancestor */
		N subtreeParent = subtreeRoot != null ? subtreeRoot.parent : null;
		for (N p = n; p.parent != subtreeParent; p = p.parent)
			if (p.isLeftChild())
				return p.parent;
		return null;
	}

	static <E, N extends Node<E, N>> void insert(N root, Comparator<? super E> c, N n) {
		if (c == null) {
			for (N parent = root;;) {
				int cmp = Utils.cmpDefault(n.data, parent.data);
				if (cmp <= 0) {
					if (!parent.hasLeftChild()) {
						parent.left = n;
						n.parent = parent;
						return;
					}
					parent = parent.left;
				} else {
					if (!parent.hasRightChild()) {
						parent.right = n;
						n.parent = parent;
						return;
					}
					parent = parent.right;
				}
			}
		} else {
			for (N parent = root;;) {
				int cmp = c.compare(n.data, parent.data);
				if (cmp <= 0) {
					if (!parent.hasLeftChild()) {
						parent.left = n;
						n.parent = parent;
						return;
					}
					parent = parent.left;
				} else {
					if (!parent.hasRightChild()) {
						parent.right = n;
						n.parent = parent;
						return;
					}
					parent = parent.right;
				}
			}
		}
	}

	static <E, N extends Node<E, N>> void clear(N root) {
		for (N p = root; p != null;) {
			for (;;) {
				if (p.hasLeftChild()) {
					p = p.left;
					continue;
				}
				if (p.hasRightChild()) {
					p = p.right;
					continue;
				}
				break;
			}
			N parent = p.parent;
			p.clear();
			p = parent;
		}
	}

	static <E, N extends Node<E, N>> void swap(N n1, N n2) {
		if (n2 == n1.parent) {
			N temp = n1;
			n1 = n2;
			n2 = temp;
		}
		if (n1 == n2.parent) {
			if (n1.isLeftChild()) {
				n1.parent.left = n2;
			} else if (n1.isRightChild()) {
				n1.parent.right = n2;
			}
			if (n1.left == n2) {
				N right = n1.right;
				if ((n1.left = n2.left) != null)
					n1.left.parent = n1;
				if ((n1.right = n2.right) != null)
					n1.right.parent = n1;
				n2.left = n1;
				if ((n2.right = right) != null)
					n2.right.parent = n2;
			} else {
				assert n1.right == n2;
				N left = n1.left;
				if ((n1.left = n2.left) != null)
					n1.left.parent = n1;
				if ((n1.right = n2.right) != null)
					n1.right.parent = n1;
				if ((n2.left = left) != null)
					n2.left.parent = n2;
				n2.right = n1;
			}
			n2.parent = n1.parent;
			n1.parent = n2;

		} else {
			if (n1.isLeftChild()) {
				n1.parent.left = n2;
			} else if (n1.isRightChild()) {
				n1.parent.right = n2;
			}
			if (n2.isLeftChild()) {
				n2.parent.left = n1;
			} else if (n2.isRightChild()) {
				n2.parent.right = n1;
			}

			N parent = n1.parent;
			N left = n1.left;
			N right = n1.right;
			n1.parent = n2.parent;
			if ((n1.left = n2.left) != null)
				n1.left.parent = n1;
			if ((n1.right = n2.right) != null)
				n1.right.parent = n1;
			n2.parent = parent;
			if ((n2.left = left) != null)
				n2.left.parent = n2;
			if ((n2.right = right) != null)
				n2.right.parent = n2;
		}
	}

	static class Node<E, N extends Node<E, N>> {
		E data;
		N parent;
		N right;
		N left;

		Node(E e) {
			this.data = e;
			parent = right = left = null;
		}

		void clear() {
			parent = left = right = null;
			data = null;
		}

		@Override
		public String toString() {
			return "<" + data + ">";
		}

		boolean isRoot() {
			return parent == null;
		}

		boolean isLeftChild() {
			return !isRoot() && this == parent.left;
		}

		boolean isRightChild() {
			return !isRoot() && this == parent.right;
		}

		boolean hasLeftChild() {
			return left != null;
		}

		boolean hasRightChild() {
			return right != null;
		}
	}

	static class BSTIterator<E, N extends Node<E, N>> implements Iterator<N> {

		private final N subtreeRoot;
		private N n;

		BSTIterator(N subtreeRoot) {
			this.subtreeRoot = subtreeRoot;
			n = subtreeRoot == null ? null : findMin(subtreeRoot);
		}

		@Override
		public boolean hasNext() {
			return n != null;
		}

		@Override
		public N next() {
			if (!hasNext())
				throw new NoSuchElementException();
			N ret = n;
			n = getSuccessorInSubtree(n, subtreeRoot);
			return ret;
		}

	}

}
