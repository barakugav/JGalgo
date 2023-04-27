package com.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

class DynamicTreeBuilderImpl implements DynamicTree.Builder {

	private double maxWeight;
	private boolean maxWeightValid;
	private boolean intWeights;
	private final Set<Class<? extends DynamicTreeExtension>> extensions = new ObjectArraySet<>();

	@Override
	public DynamicTree build() {
		if (!maxWeightValid)
			throw new IllegalStateException("Maximum set was not set. Call setMaxWeight(double)");
		if (intWeights && !(Integer.MIN_VALUE <= maxWeight && maxWeight <= Integer.MAX_VALUE))
			throw new IllegalStateException("Maximum weight is too great for integer weights");
		if (extensions.isEmpty()) {
			if (intWeights)
				return new DynamicTreeSplayInt((int) maxWeight);
			else
				return new DynamicTreeSplay(maxWeight);
		} else {
			List<DynamicTreeSplayExtension> extensions = new ArrayList<>(this.extensions.size());
			for (Class<? extends DynamicTreeExtension> extClass : this.extensions) {
				if (extClass.equals(DynamicTreeExtension.TreeSize.class)) {
					extensions.add(new DynamicTreeSplayExtension.TreeSize());
				} else {
					throw new IllegalArgumentException("unknown extension: " + extClass);
				}
			}

			if (intWeights)
				return new DynamicTreeSplayIntExtended((int) maxWeight, extensions);
			else
				return new DynamicTreeSplayExtended(maxWeight, extensions);
		}
	}

	@Override
	public DynamicTree.Builder setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
		maxWeightValid = true;
		return this;
	}

	@Override
	public DynamicTree.Builder setIntWeights(boolean enable) {
		intWeights = enable;
		return this;
	}

	@Override
	public DynamicTree.Builder addExtension(Class<? extends DynamicTreeExtension> extensionType) {
		extensions.add(Objects.requireNonNull(extensionType));
		return this;
	}

	@Override
	public DynamicTree.Builder removeExtension(Class<? extends DynamicTreeExtension> extensionType) {
		extensions.remove(Objects.requireNonNull(extensionType));
		return this;
	}

}
