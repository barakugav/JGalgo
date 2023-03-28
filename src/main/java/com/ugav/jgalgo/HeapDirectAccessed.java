package com.ugav.jgalgo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public interface HeapDirectAccessed<E> extends Heap<E> {

	/**
	 * Find the handle of an element in the heap
	 *
	 * @param e an element in the heap
	 * @return the handle of the element or null if the element is not in the heap
	 */
	default Handle<E> findHanlde(E e) {
		Comparator<? super E> c = comparator();
		for (Handle<E> p : handles()) {
			if (c.compare(e, p.get()) == 0)
				return p;
		}
		return null;
	}

	/**
	 * Find the handle of the minimal element in the heap
	 *
	 * @return handle of the minimal element
	 * @throws IllegalStateException if the heap is empty
	 */
	public Handle<E> findMinHandle();

	/**
	 * Decrease the key of an element in the heap
	 *
	 * @param handle handle of an inserted element
	 * @param e      new key
	 */
	public void decreaseKey(Handle<E> handle, E e);

	/**
	 * Remove an element from the heap by its handle
	 *
	 * @param handle handle of an inserted element
	 */
	public void removeHandle(Handle<E> handle);

	/**
	 * Get a collection view of the handles of the heap.
	 *
	 * @return view of all handles in the heap
	 */
	public Set<Handle<E>> handles();

	@Override
	default Iterator<E> iterator() {
		return new Iterator<>() {
			final Iterator<Handle<E>> it = handles().iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public E next() {
				return it.next().get();
			}
		};
	}

	/**
	 * Object associated with an element in a heap. Allow specific operations to
	 * perform directly on the element without searching. Not supported by all
	 * implementations.
	 */
	public static interface Handle<E> {

		/**
		 * Get the element this handle is associated with
		 *
		 * @return the element value
		 */
		public E get();

	}

	@FunctionalInterface
	public static interface Builder extends Heap.Builder {
		@Override
		<E> HeapDirectAccessed<E> build(Comparator<? super E> cmp);
	}

}
