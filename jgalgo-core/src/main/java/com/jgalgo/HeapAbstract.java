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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;

abstract class HeapAbstract<E> extends AbstractCollection<E> implements Heap<E> {

	final Comparator<? super E> c;

	HeapAbstract(Comparator<? super E> c) {
		this.c = c;
	}

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
	public void meld(Heap<? extends E> h) {
		if (h == this)
			return;
		addAll(h);
		h.clear();
	}

	@Override
	public Comparator<? super E> comparator() {
		return c;
	}

	int compare(E e1, E e2) {
		return c == null ? Utils.cmpDefault(e1, e2) : c.compare(e1, e2);
	}

}
