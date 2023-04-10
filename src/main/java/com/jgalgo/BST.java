package com.jgalgo;

public interface BST<E> extends HeapReferenceable<E> {

	/**
	 * Find the maximum element in the tree.
	 *
	 * @return the maximum element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public E findMax();

	/**
	 * Extract the maximum element in the tree.
	 *
	 * @return the maximum element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public E extractMax();

	/**
	 * Find maximal element in the tree and return a reference to it.
	 *
	 * @return a reference to the maximal element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public HeapReference<E> findMaxRef();

	/**
	 * Search for an element in the tree or the greatest element strictly smaller
	 * (predecessor) than it if it's not found.
	 *
	 * @param e the search element
	 * @return reference to the searched element or it's predecessor if the element
	 *         is not found, or null if there is no predecessor
	 */
	public HeapReference<E> findOrSmaller(E e);

	/**
	 * Search for an element in the tree or the smallest element strictly greater
	 * (successor) than it if it's not found.
	 *
	 * @param e the search element
	 * @return reference to the searched element or it's successor if the element is
	 *         not found, or null if there is no successor
	 */
	public HeapReference<E> findOrGreater(E e);

	/**
	 * Find the greatest element strictly smaller than an element.
	 *
	 * @param e an element
	 * @return reference to the predecessor element with strictly smaller value or
	 *         null if no such exists
	 */
	public HeapReference<E> findSmaller(E e);

	/**
	 * Find the smallest element strictly greater than an element.
	 *
	 * @param e an element
	 * @return reference to the successor element with strictly greater value or
	 *         null if no such exists
	 */
	public HeapReference<E> findGreater(E e);

	/**
	 * Get the predecessor of a node in the tree.
	 *
	 * <p>
	 * The predecessor node depends on the tree structure. If there are no duplicate
	 * values, the predecessor is the greatest value strictly smaller than the given
	 * element. If there are duplicate values, it may be smaller or equal.
	 *
	 * @param ref reference to an element in the tree
	 * @return reference to the predecessor element in the tree, that is an element
	 *         smaller or equal to the given referenced element, or null if no such
	 *         predecessor exists
	 */
	public HeapReference<E> getPredecessor(HeapReference<E> ref);

	/**
	 * Finds the successor of an element in the tree.
	 *
	 * <p>
	 * The successor node depends on the tree structure. If there are no duplicate
	 * values, the successor is the smallest value strictly greater than the given
	 * element. If there are duplicate values, it may be greater or equal.
	 *
	 * @param ref reference to an element in the tree
	 * @return reference to the successor element in the tree, that is an element
	 *         greater or equal to the given referenced element, or null if no such
	 *         successor exists
	 */
	public HeapReference<E> getSuccessor(HeapReference<E> ref);

	/**
	 * Split the current BST into two different BSTs with elements strictly smaller
	 * and greater or equal than an element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be greater or equal than
	 * the given element, and the returned new tree will contain elements strictly
	 * smaller than the given element.
	 *
	 * @param e a pivot element
	 * @return new tree with elements strictly smaller than the given element
	 */
	public BST<E> splitSmaller(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller or equal
	 * and strictly greater than an element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be smaller or equal than
	 * the given element, and the returned new tree will contain elements strictly
	 * greater than the given element.
	 *
	 * @param e a pivot element
	 * @return new tree with elements strictly greater than the given element
	 */
	public BST<E> splitGreater(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller and
	 * bigger than an element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be smaller or equal to
	 * the given element, and the returned new tree will contain elements greater
	 * than the given element. If the tree contains duplications of the given
	 * element, the elements in the returned tree will be to greater or equal
	 * (rather than strictly greater). To split a tree more precisely, use
	 * {@link #splitSmaller(Object)} or {@link #splitGreater(Object)}.
	 *
	 * @param ref given element in the tree
	 * @return new tree with elements greater (greater or equal if duplicate
	 *         elements of the given element exists) than the given element
	 */
	public BST<E> split(HeapReference<E> ref);

}
