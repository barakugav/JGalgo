package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;

public class SplitFindMinArray<K> implements SplitFindMin<K> {

	/*
	 * make O(n), find findMin decreaseKey O(1), split O(logn) amortized.
	 */

	private K[] keys;
	private Block[] blocks;
	private Comparator<? super K> c;

	public SplitFindMinArray() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(Collection<K> keys, Comparator<? super K> c) {
		int elmNum = keys.size();
		this.keys = (K[]) new Object[elmNum];
		blocks = new Block[elmNum];
		this.c = c = c != null ? c : Utils.getDefaultComparator();
		if (elmNum == 0)
			return;

		int i = 0;
		for (K key : keys)
			this.keys[i++] = key;

		Block head = null, tail = null;
		for (int size = elmNum; size > 0;) {
			int blkSize = 1 << Integer.numberOfTrailingZeros(size);
			int from = tail == null ? 0 : tail.to;
			int to = from + blkSize;
			Block blk = newBlock(from, to);
			if (head == null) {
				head = blk;
			}
			tail = blk;
			size -= blkSize;
		}
		newSequence(head, tail);
	}

	@Override
	public int find(int x) {
		return blocks[x].seq.head.from;
	}

	@Override
	public void split(int x) {
		if (x == blocks.length)
			return;
		Block blk = blocks[x];
		Sequence seq = blk.seq;

		if (x == seq.head.from)
			return;

		if (x != blk.from) {
			Block head = null, tail = null;
			for (int size = x - blk.from; size > 0;) {
				int blkSize = 1 << (31 - Integer.numberOfLeadingZeros(size));
				int from = tail == null ? blk.from : tail.to;
				int to = from + blkSize;
				Block newBlk = newBlock(from, to);
				if (head == null)
					head = newBlk;
				tail = newBlk;
				size -= blkSize;
			}
			newSequence(seq.head.from == head.from ? head : seq.head, tail);
		} else
			newSequence(seq.head, blocks[blk.from - 1]);

		Block head = nextBlk(blk), tail = null;
		for (int size = blk.to - x; size > 0;) {
			int blkSize = 1 << (31 - Integer.numberOfLeadingZeros(size));
			int to = head == null ? blk.to : head.from;
			int from = to - blkSize;
			Block newBlk = newBlock(from, to);
			if (tail == null)
				tail = newBlk;
			head = newBlk;
			size -= blkSize;
		}
		newSequence(head, seq.tail.to == tail.to ? tail : seq.tail);

		blk.seq = null;
		seq.head = seq.tail = null;
	}

	private Block nextBlk(Block blk) {
		Block[] blocks = this.blocks;
		int t = blk.to;
		if (t >= blocks.length)
			return null;
		Block next = blocks[t];
		return blk.seq == next.seq ? next : null;
	}

	@Override
	public K getKey(int x) {
		return keys[x];
	}

	@Override
	public int findMin(int x) {
		return blocks[x].seq.min;
	}

	@Override
	public boolean decreaseKey(int x, K newKey) {
		K[] keys = this.keys;
		Comparator<? super K> c = this.c;

		Block blk = blocks[x];
		Sequence seq = blk.seq;
		keys[x] = newKey;

		if (blk.min == x || c.compare(newKey, keys[blk.min]) < 0) {
			blk.min = x;
			if (seq.min == x || c.compare(newKey, keys[seq.min]) < 0) {
				seq.min = x;
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if (blocks == null || blocks.length == 0)
			return "[]";
		StringBuilder s = new StringBuilder();
		s.append('[');
		for (int i = 0;;) {
			Sequence seq = blocks[i].seq;
			s.append(seq);
			i = seq.tail.to;
			if (i >= blocks.length)
				break;
			s.append(", ");
		}
		s.append(']');
		return s.toString();
	}

	private Sequence newSequence(Block head, Block tail) {
		K[] keys = this.keys;
		Block[] blocks = this.blocks;
		Comparator<? super K> c = this.c;

		Sequence seq = new Sequence(head, tail);

		int min = -1;
		for (Block blk = head;;) {
			blk.seq = seq;
			if (min == -1 || c.compare(keys[min], keys[blk.min]) > 0)
				min = blk.min;
			if (blk == tail || blk.to >= blocks.length)
				break;
			blk = blocks[blk.to];
		}
		seq.min = min;

		return seq;
	}

	private Block newBlock(int from, int to) {
		K[] keys = this.keys;
		Block[] blocks = this.blocks;
		Comparator<? super K> c = this.c;

		Block blk = new Block(from, to);

		int min = -1;
		for (int i = from; i < to; i++) {
			blocks[i] = blk;
			if (min == -1 || c.compare(keys[min], keys[i]) > 0)
				min = i;
		}
		blk.min = min;

		return blk;
	}

	private static class Sequence {

		Block head;
		Block tail;
		int min;

		Sequence(Block head, Block tail) {
			this.head = head;
			this.tail = tail;
		}

		@Override
		public String toString() {
			return head == null ? "[cleared]" : "[" + head.from + ", " + tail.to + "]";
		}

	}

	private static class Block {

		Sequence seq;
		final int from;
		final int to;
		int min;

		Block(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public String toString() {
			return "[" + from + ", " + to + "]";
		}

	}

}
