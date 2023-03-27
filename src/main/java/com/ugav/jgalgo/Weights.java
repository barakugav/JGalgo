package com.ugav.jgalgo;

public interface Weights<E> {

	public E get(int id);

	public void set(int id, E data);

	public E defaultVal();

	public void clear();

	public static interface Int extends Weights<Integer>, EdgeWeightFunc.Int {

		public int getInt(int key);

		@Deprecated
		@Override
		default Integer get(int key) {
			return Integer.valueOf(getInt(key));
		}

		public void set(int key, int weight);

		@Deprecated
		@Override
		default void set(int key, Integer data) {
			set(key, data.intValue());
		}

		public int defaultValInt();

		@Deprecated
		@Override
		default Integer defaultVal() {
			return Integer.valueOf(defaultValInt());
		}

		@Override
		default int weightInt(int key) {
			return getInt(key);
		}
	}

	public static interface Long extends Weights<java.lang.Long>, EdgeWeightFunc {

		public long getLong(int key);

		@Deprecated
		@Override
		default java.lang.Long get(int key) {
			return java.lang.Long.valueOf(getLong(key));
		}

		public void set(int key, long weight);

		@Deprecated
		@Override
		default void set(int key, java.lang.Long data) {
			set(key, data.longValue());
		}

		public long defaultValLong();

		@Deprecated
		@Override
		default java.lang.Long defaultVal() {
			return java.lang.Long.valueOf(defaultValLong());
		}

		@Override
		default double weight(int key) {
			return getLong(key);
		}
	}

	public static interface Double extends Weights<java.lang.Double>, EdgeWeightFunc {

		public double getDouble(int key);

		@Deprecated
		@Override
		default java.lang.Double get(int key) {
			return java.lang.Double.valueOf(getDouble(key));
		}

		public void set(int key, double weight);

		@Deprecated
		@Override
		default void set(int key, java.lang.Double data) {
			set(key, data.doubleValue());
		}

		public double defaultValDouble();

		@Deprecated
		@Override
		default java.lang.Double defaultVal() {
			return java.lang.Double.valueOf(defaultValDouble());
		}

		@Override
		default double weight(int key) {
			return getDouble(key);
		}
	}

	public static interface Bool extends Weights<Boolean> {

		public boolean getBool(int key);

		@Deprecated
		@Override
		default Boolean get(int key) {
			return Boolean.valueOf(getBool(key));
		}

		public void set(int key, boolean weight);

		@Deprecated
		@Override
		default void set(int key, Boolean data) {
			set(key, data.booleanValue());
		}

		public boolean defaultValBool();

		@Deprecated
		@Override
		default Boolean defaultVal() {
			return Boolean.valueOf(defaultValBool());
		}
	}

	public static interface Factory {
		<E> Weights<E> ofObjs();

		Weights.Int ofInts();

		Weights.Long ofLongs();

		Weights.Double ofDoubles();

		Weights.Bool ofBools();

		Object getDefVal();

		Factory defVal(Object defVal);

		default Factory defVal(int defVal) {
			return defVal(Integer.valueOf(defVal));
		}

		default Factory defVal(long defVal) {
			return defVal(java.lang.Long.valueOf(defVal));
		}

		default Factory defVal(double defVal) {
			return defVal(java.lang.Double.valueOf(defVal));
		}

		default Factory defVal(boolean defVal) {
			return defVal(Boolean.valueOf(defVal));
		}
	}

	/**
	 * The default vertices weight key of the bipartite property.
	 * <p>
	 * A bipartite graph is a graph in which the vertices are partitioned into two
	 * sets V1,V2 and there are no edges between two vertices u,v if they are both
	 * in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
	 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
	 * {@link #DefaultBipartiteWeightKey}. To use a different key, the algorithms
	 * expose a {@code setBipartiteVerticesWeightKey(Object)} function.
	 */
	public static final Object DefaultBipartiteWeightKey = new Object() {
		@Override
		public String toString() {
			return "DefaultBipartiteVerticesWeightKey";
		}
	};

}