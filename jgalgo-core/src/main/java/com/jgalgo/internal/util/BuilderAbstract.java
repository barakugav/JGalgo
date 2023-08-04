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
package com.jgalgo.internal.util;

public interface BuilderAbstract<BuilderType> {

	/**
	 * <b>[TL;DR Don't call me!]</b> Set an option.
	 * <p>
	 * The builder might support different options to customize its implementation. These options never change the
	 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
	 * because they are not part of the API and may change in the future.
	 * <p>
	 * These options are mainly for debug and benchmark purposes.
	 *
	 * @param  key   the option key
	 * @param  value the option value
	 * @return       this builder
	 */
	@SuppressWarnings("unchecked")
	default BuilderType setOption(String key, Object value) {
		return (BuilderType) this;
	}

}
