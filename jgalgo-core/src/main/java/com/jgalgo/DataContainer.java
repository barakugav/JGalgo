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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import it.unimi.dsi.fastutil.booleans.AbstractBooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import it.unimi.dsi.fastutil.bytes.AbstractByteList;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterators;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import it.unimi.dsi.fastutil.chars.AbstractCharList;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharIterators;
import it.unimi.dsi.fastutil.chars.CharListIterator;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterators;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.AbstractFloatList;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterators;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.shorts.AbstractShortList;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterators;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;

abstract class DataContainer<E> {
	final IDStrategyImpl idStrat;

	DataContainer(IDStrategyImpl idStrat) {
		this.idStrat = Objects.requireNonNull(idStrat);
	}

	abstract int capacity();

	abstract void expand(int newCapacity);

	abstract void clear(int idx);

	abstract void clear();

	abstract void swap(int idx1, int idx2);

	abstract Collection<E> values();

	abstract Class<E> getTypeClass();

	abstract DataContainer<E> copy(IDStrategyImpl idStrat);

	int size() {
		return idStrat.size();
	}

	@Override
	public int hashCode() {
		return values().hashCode();
	}

	@Override
	public String toString() {
		return values().toString();
	}

	void checkIdx(int idx) {
		if (!(0 <= idx && idx < idStrat.size()))
			throw new IndexOutOfBoundsException(idx);
	}

	static <D> DataContainer<D> newInstance(IDStrategyImpl idStart, Class<? super D> type, D defVal) {
		@SuppressWarnings("rawtypes")
		DataContainer container;
		if (type == byte.class) {
			byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
			container = new DataContainer.Byte(idStart, defVal0);

		} else if (type == short.class) {
			short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
			container = new DataContainer.Short(idStart, defVal0);

		} else if (type == int.class) {
			int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
			container = new DataContainer.Int(idStart, defVal0);

		} else if (type == long.class) {
			long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
			container = new DataContainer.Long(idStart, defVal0);

		} else if (type == float.class) {
			float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
			container = new DataContainer.Float(idStart, defVal0);

		} else if (type == double.class) {
			double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
			container = new DataContainer.Double(idStart, defVal0);

		} else if (type == boolean.class) {
			boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
			container = new DataContainer.Bool(idStart, defVal0);

		} else if (type == char.class) {
			char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
			container = new DataContainer.Char(idStart, defVal0);

		} else {
			container = new DataContainer.Obj<>(idStart, defVal, type);
		}
		@SuppressWarnings("unchecked")
		DataContainer<D> container0 = container;
		return container0;
	}

	static class Obj<E> extends DataContainer<E> {

		private Object[] weights;
		private final E defaultVal;
		private final ObjectCollection<E> values;
		private final Class<E> type;

		Obj(IDStrategyImpl idStrat, E defVal, Class<E> type) {
			super(idStrat);

			defaultVal = defVal;
			weights = ObjectArrays.EMPTY_ARRAY;
			Arrays.fill(weights, defaultVal);

			this.type = Objects.requireNonNull(type);
			values = new AbstractObjectList<>() {
				@Override
				public int size() {
					return DataContainer.Obj.super.size();
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public ObjectListIterator<E> iterator() {
					return (ObjectListIterator) ObjectIterators.wrap(weights, 0, size());
				}

				@SuppressWarnings("unchecked")
				@Override
				public E get(int index) {
					checkIdx(index);
					return (E) weights[index];
				}
			};
		}

		@SuppressWarnings("unchecked")
		public E get(int idx) {
			checkIdx(idx);
			return (E) weights[idx];
		}

		void set(int idx, E weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		E defaultVal() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			ObjectArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		Collection<E> values() {
			return values;
		}

		@Override
		Class<E> getTypeClass() {
			return type;
		}

		@Override
		DataContainer.Obj<E> copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Obj<E> copy = new DataContainer.Obj<>(idStrat, defaultVal, type);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof DataContainer.Obj<?>))
				return false;
			DataContainer.Obj<?> o = (DataContainer.Obj<?>) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Byte extends DataContainer<java.lang.Byte> {

		private byte[] weights;
		private final byte defaultVal;
		private final ByteCollection values;

		Byte(IDStrategyImpl idStrat, byte defVal) {
			super(idStrat);

			weights = ByteArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractByteList() {

				@Override
				public int size() {
					return DataContainer.Byte.super.size();
				}

				@Override
				public ByteListIterator iterator() {
					return ByteIterators.wrap(weights, 0, size());
				}

				@Override
				public byte getByte(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		byte getByte(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, byte weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		byte defaultValByte() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			ByteArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		ByteCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Byte> getTypeClass() {
			return byte.class;
		}

		@Override
		DataContainer.Byte copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Byte copy = new DataContainer.Byte(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Byte))
				return false;
			DataContainer.Byte o = (DataContainer.Byte) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Short extends DataContainer<java.lang.Short> {

		private short[] weights;
		private final short defaultVal;
		private final ShortCollection values;

		Short(IDStrategyImpl idStrat, short defVal) {
			super(idStrat);

			weights = ShortArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractShortList() {

				@Override
				public int size() {
					return DataContainer.Short.super.size();
				}

				@Override
				public ShortListIterator iterator() {
					return ShortIterators.wrap(weights, 0, size());
				}

				@Override
				public short getShort(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		short getShort(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, short weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		short defaultValShort() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			ShortArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		ShortCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Short> getTypeClass() {
			return short.class;
		}

		@Override
		DataContainer.Short copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Short copy = new DataContainer.Short(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Short))
				return false;
			DataContainer.Short o = (DataContainer.Short) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Int extends DataContainer<Integer> {

		private int[] weights;
		private final int defaultVal;
		private final IntCollection values;

		Int(IDStrategyImpl idStrat, int defVal) {
			super(idStrat);

			weights = IntArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractIntList() {

				@Override
				public int size() {
					return DataContainer.Int.super.size();
				}

				@Override
				public IntListIterator iterator() {
					return IntIterators.wrap(weights, 0, size());
				}

				@Override
				public int getInt(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		int getInt(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, int weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		int defaultValInt() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			IntArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		IntCollection values() {
			return values;
		}

		@Override
		Class<Integer> getTypeClass() {
			return int.class;
		}

		@Override
		DataContainer.Int copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Int copy = new DataContainer.Int(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Int))
				return false;
			DataContainer.Int o = (DataContainer.Int) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Long extends DataContainer<java.lang.Long> {

		private long[] weights;
		private final long defaultVal;
		private final LongCollection values = new AbstractLongList() {

			@Override
			public int size() {
				return DataContainer.Long.super.size();
			}

			@Override
			public LongListIterator iterator() {
				return LongIterators.wrap(weights, 0, size());
			}

			@Override
			public long getLong(int index) {
				checkIdx(index);
				return weights[index];
			}
		};

		Long(IDStrategyImpl idStrat, long defVal) {
			super(idStrat);

			weights = LongArrays.EMPTY_ARRAY;
			defaultVal = defVal;
		}

		Long(DataContainer.Long orig, IDStrategyImpl idStrat) {
			super(idStrat);
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();

			weights = Arrays.copyOf(orig.weights, idStrat.size());
			defaultVal = orig.defaultVal;
		}

		long getLong(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, long weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		long defaultValLong() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			LongArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		LongCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Long> getTypeClass() {
			return long.class;
		}

		@Override
		DataContainer.Long copy(IDStrategyImpl idStrat) {
			return new DataContainer.Long(this, idStrat);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Long))
				return false;
			DataContainer.Long o = (DataContainer.Long) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Float extends DataContainer<java.lang.Float> {

		private float[] weights;
		private final float defaultVal;
		private final FloatCollection values;

		Float(IDStrategyImpl idStrat, float defVal) {
			super(idStrat);

			weights = FloatArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractFloatList() {

				@Override
				public int size() {
					return DataContainer.Float.super.size();
				}

				@Override
				public FloatListIterator iterator() {
					return FloatIterators.wrap(weights, 0, size());
				}

				@Override
				public float getFloat(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		float getFloat(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, float weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		float defaultValFloat() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			FloatArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		FloatCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Float> getTypeClass() {
			return float.class;
		}

		@Override
		DataContainer.Float copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Float copy = new DataContainer.Float(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Float))
				return false;
			DataContainer.Float o = (DataContainer.Float) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Double extends DataContainer<java.lang.Double> {

		private double[] weights;
		private final double defaultVal;
		private final DoubleCollection values;

		Double(IDStrategyImpl idStrat, double defVal) {
			super(idStrat);

			weights = DoubleArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractDoubleList() {

				@Override
				public int size() {
					return DataContainer.Double.super.size();
				}

				@Override
				public DoubleListIterator iterator() {
					return DoubleIterators.wrap(weights, 0, size());
				}

				@Override
				public double getDouble(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		double getDouble(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, double weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		double defaultValDouble() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			DoubleArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		DoubleCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Double> getTypeClass() {
			return double.class;
		}

		@Override
		DataContainer.Double copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Double copy = new DataContainer.Double(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Double))
				return false;
			DataContainer.Double o = (DataContainer.Double) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Bool extends DataContainer<Boolean> {

		private final BitSet weights;
		private int capacity;
		private final boolean defaultVal;
		private final BooleanCollection values = new AbstractBooleanList() {

			@Override
			public int size() {
				return DataContainer.Bool.super.size();
			}

			@Override
			public BooleanListIterator iterator() {
				return new BooleanListIterator() {
					int idx = 0;

					@Override
					public boolean hasNext() {
						return idx < size();
					}

					@Override
					public boolean nextBoolean() {
						if (!hasNext())
							throw new NoSuchElementException();
						return weights.get(idx++);
					}

					@Override
					public boolean previousBoolean() {
						if (!hasPrevious())
							throw new NoSuchElementException();
						return weights.get(--idx);
					}

					@Override
					public boolean hasPrevious() {
						return idx > 0;
					}

					@Override
					public int nextIndex() {
						return idx;
					}

					@Override
					public int previousIndex() {
						return idx - 1;
					}
				};
			}

			@Override
			public boolean getBoolean(int index) {
				checkIdx(index);
				return weights.get(index);
			}
		};

		Bool(DataContainer.Bool orig, IDStrategyImpl idStrat) {
			super(idStrat);
			defaultVal = orig.defaultVal;
			weights = (BitSet) orig.weights.clone();
		}

		Bool(IDStrategyImpl idStrat, boolean defVal) {
			super(idStrat);

			defaultVal = defVal;
			weights = new BitSet();
		}

		boolean getBool(int idx) {
			checkIdx(idx);
			return weights.get(idx);
		}

		void set(int idx, boolean weight) {
			checkIdx(idx);
			weights.set(idx, weight);
		}

		boolean defaultValBool() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return capacity;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = capacity;
			assert oldCapacity < newCapacity;
			capacity = newCapacity;
			if (defaultVal)
				weights.set(oldCapacity, newCapacity);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			boolean temp = weights.get(idx1);
			weights.set(idx1, weights.get(idx2));
			weights.set(idx2, temp);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights.set(idx, defaultVal);
		}

		@Override
		void clear() {
			weights.set(0, capacity, defaultVal);
		}

		@Override
		BooleanCollection values() {
			return values;
		}

		@Override
		Class<Boolean> getTypeClass() {
			return boolean.class;
		}

		@Override
		DataContainer.Bool copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			return new DataContainer.Bool(this, idStrat);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Bool))
				return false;
			DataContainer.Bool o = (DataContainer.Bool) other;
			return size() == o.size() && weights.equals(o.weights);
		}
	}

	static class Char extends DataContainer<Character> {

		private char[] weights;
		private final char defaultVal;
		private final CharCollection values;

		Char(IDStrategyImpl idStrat, char defVal) {
			super(idStrat);

			weights = CharArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractCharList() {

				@Override
				public int size() {
					return DataContainer.Char.super.size();
				}

				@Override
				public CharListIterator iterator() {
					return CharIterators.wrap(weights, 0, size());
				}

				@Override
				public char getChar(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		char getChar(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, char weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		char defaultValChar() {
			return defaultVal;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultVal);
		}

		@Override
		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			CharArrays.swap(weights, idx1, idx2);
		}

		@Override
		void clear(int idx) {
			// checkIdx(idx);
			weights[idx] = defaultVal;
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size(), defaultVal);
		}

		@Override
		CharCollection values() {
			return values;
		}

		@Override
		Class<Character> getTypeClass() {
			return char.class;
		}

		@Override
		DataContainer.Char copy(IDStrategyImpl idStrat) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Char copy = new DataContainer.Char(idStrat, defaultVal);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Char))
				return false;
			DataContainer.Char o = (DataContainer.Char) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Manager {

		final Map<Object, DataContainer<?>> containers = new Object2ObjectArrayMap<>();
		private int containersCapacity;

		Manager(int initCapacity) {
			containersCapacity = initCapacity;
		}

		Manager(Manager orig, IDStrategyImpl idStrat) {
			this(idStrat.size());
			for (var entry : orig.containers.entrySet())
				containers.put(entry.getKey(), entry.getValue().copy(idStrat));
		}

		Manager copy(IDStrategyImpl idStrat) {
			return new Manager(this, idStrat);
		}

		void addContainer(Object key, DataContainer<?> container) {
			DataContainer<?> oldContainer = containers.put(key, container);
			if (oldContainer != null)
				throw new IllegalArgumentException("Two weights types with the same key: " + key);
			if (containersCapacity > container.capacity())
				container.expand(containersCapacity);
		}

		void ensureCapacity(int capacity) {
			if (capacity <= containersCapacity)
				return;
			int newCapacity = Math.max(Math.max(2, 2 * containersCapacity), capacity);
			for (DataContainer<?> container : containers.values())
				container.expand(newCapacity);
			containersCapacity = newCapacity;
		}

		void swapElements(int idx1, int idx2) {
			for (DataContainer<?> container : containers.values())
				container.swap(idx1, idx2);
		}

		void clearElement(int idx) {
			for (DataContainer<?> container : containers.values())
				container.clear(idx);
		}

		void clearContainers() {
			for (DataContainer<?> container : containers.values())
				container.clear();
		}

	}

}