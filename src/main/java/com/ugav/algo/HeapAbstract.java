package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class HeapAbstract<E> extends AbstractCollection<E> implements Heap<E> {

	@Override
	public boolean add(E e) {
		insert(e);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Object e : c)
			if (remove(e))
				modified = true;
		return modified;
	}

	@Override
	public E extractMin() {
		E min = findMin();
		remove(min);
		return min;
	}

	@Override
	public void meld(Heap<? extends E> h) {
		if (h == this)
			return;
		addAll(h);
		h.clear();
	}

	@Override
	public boolean isHandlesSupported() {
		return false;
	}

	@Override
	public Handle<E> findHanlde(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Handle<E> findMinHandle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		throw new UnsupportedOperationException();
	}

}
