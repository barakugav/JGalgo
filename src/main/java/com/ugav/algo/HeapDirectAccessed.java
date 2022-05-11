package com.ugav.algo;

public interface HeapDirectAccessed<E> extends Heap<E> {

	/**
	 * Find the handle of an element in the heap
	 *
	 * @param e an element in the heap
	 * @return the handle of the element or null if the element is not in the heap
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public Handle<E> findHanlde(E e);

	/**
	 * Find the handle of the minimal element in the heap
	 *
	 * @return handle of the minimal element
	 * @throws IllegalStateException         if the heap is empty
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public Handle<E> findMinHandle();

	/**
	 * Decrease the key of an element in the heap
	 *
	 * @param handle handle of an inserted element
	 * @param e      new key
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public void decreaseKey(Handle<E> handle, E e);

	/**
	 * Remove an element from the heap by its handle
	 *
	 * @param handle handle of an inserted element
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public void removeHandle(Handle<E> handle);

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

}
