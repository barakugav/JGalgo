package com.jgalgo;

import java.util.Arrays;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrays;

abstract class DynamicTreeSplayExtension implements DynamicTreeExtension {

	final ExtensionData data;
	DynamicTree dt;

	private DynamicTreeSplayExtension(ExtensionData data) {
		this.data = data;
	}

	void setDynamicTreeAlgo(DynamicTree dt) {
		if (this.dt != null)
			throw new IllegalStateException("extension was already used in some dynamic tree");
		this.dt = Objects.requireNonNull(dt);
	}

	void splay(SplayTree.Node<?, ?> node) {
		if (dt instanceof DynamicTreeSplay)
			((DynamicTreeSplay) dt).splay((DynamicTreeSplay.SplayNode) node);
		else if (dt instanceof DynamicTreeSplayInt)
			((DynamicTreeSplayInt) dt).splay((DynamicTreeSplayInt.SplayNode) node);
		else
			throw new IllegalStateException();
	}

	abstract void initNode(SplayTree.Node<?, ?> n);

	abstract void beforeCut(SplayTree.Node<?, ?> n);

	abstract void afterLink(SplayTree.Node<?, ?> n);

	abstract void beforeRotate(SplayTree.Node<?, ?> n);

	static class TreeSize extends DynamicTreeSplayExtension.Int implements DynamicTreeExtension.TreeSize {

		/**
		 * Create a new Tree Size extensions.
		 * <p>
		 * Each instance of this extension should be used in a single dynamic tree object.
		 */
		TreeSize() {}

		@Override
		public int getTreeSize(DynamicTree.Node node) {
			SplayTree.Node<?, ?> n = (SplayTree.Node<?, ?>) node;
			splay(n);
			return getNodeData(n);
		}

		@Override
		void initNode(SplayTree.Node<?, ?> n) {
			setNodeData(n, 1);
		}

		@Override
		void beforeCut(SplayTree.Node<?, ?> n) {
			setNodeData(n, getNodeData(n) - getNodeData(n.right));
		}

		@Override
		void afterLink(SplayTree.Node<?, ?> n) {
			SplayTree.Node<?, ?> parent = (SplayTree.Node<?, ?>) ((DynamicTree.Node) n).getParent();
			setNodeData(parent, getNodeData(parent) + getNodeData(n));
		}

		@Override
		void beforeRotate(SplayTree.Node<?, ?> n) {
			SplayTree.Node<?, ?> parent = n.parent;
			int nSize = getNodeData(n);
			int parentOldSize = getNodeData(parent);

			int parentNewSize;
			if (n.isLeftChild()) {
				parentNewSize = parentOldSize - nSize + (n.hasRightChild() ? getNodeData(n.right) : 0);
			} else {
				assert n.isRightChild();
				parentNewSize = parentOldSize - nSize + (n.hasLeftChild() ? getNodeData(n.left) : 0);
			}
			setNodeData(parent, parentNewSize);

			setNodeData(n, parentOldSize);
		}

	}

	private static abstract class Int extends DynamicTreeSplayExtension {

		Int() {
			super(new ExtensionData.Int());
		}

		private ExtensionData.Int data() {
			return (ExtensionData.Int) data;
		}

		int getNodeData(SplayTree.Node<?, ?> n) {
			return data().get(((SplayNodeExtended) n).idx());
		}

		void setNodeData(SplayTree.Node<?, ?> n, int data) {
			data().set(((SplayNodeExtended) n).idx(), data);
		}
	}

	static abstract class ExtensionData {
		abstract void swap(int idx1, int idx2);

		abstract void clear(int idx);

		abstract void expand(int newCapacity);

		static class Int extends ExtensionData {
			private int[] data;

			Int() {
				data = IntArrays.EMPTY_ARRAY;
			}

			int get(int idx) {
				return data[idx];
			}

			void set(int idx, int d) {
				data[idx] = d;
			}

			@Override
			void swap(int idx1, int idx2) {
				int temp = data[idx1];
				data[idx1] = data[idx2];
				data[idx2] = temp;
			}

			@Override
			void clear(int idx) {
				data[idx] = 0;
			}

			@Override
			void expand(int newCapacity) {
				data = Arrays.copyOf(data, newCapacity);
			}
		}
	}

	static interface SplayNodeExtended {
		int idx();
	}

}
