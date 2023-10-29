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
package com.jgalgo.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;

/**
 * A reader that reads Graphs from files/IO-reader.
 *
 * <pre> {@code
 * Graph g = GraphReader.newInstance("gml").readGraph("graph1.gml");
 * System.out.println("g's vertices are: " + g.vertices());
 * }</pre>
 *
 * @see    GraphWriter
 * @author Barak Ugav
 */
public interface GraphReader {

	/**
	 * Read a graph from an I/O reader.
	 *
	 * @param  reader an I/O reader that contain a graph description
	 * @return        a new graph read from the reader
	 */
	default IntGraph readGraph(Reader reader) {
		return readIntoBuilder(reader).build();
	}

	/**
	 * Read a graph from a file.
	 *
	 * @param  file a file that contain a graph description
	 * @return      a new graph read from the file
	 */
	default IntGraph readGraph(File file) {
		try (Reader reader = new FileReader(file, GraphIO.JGALGO_CHARSET)) {
			return readGraph(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Read a graph from a file, given a path to it.
	 *
	 * @param  path a path to a file that contain a graph description
	 * @return      a new graph read from the file
	 */
	default IntGraph readGraph(String path) {
		try (Reader reader = new FileReader(path, GraphIO.JGALGO_CHARSET)) {
			return readGraph(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Read a graph from an I/O reader into a {@link IntGraphBuilder}.
	 *
	 * @param  reader an I/O reader that contain a graph description
	 * @return        a graph builder containing the vertices and edge read from the reader
	 */
	IntGraphBuilder readIntoBuilder(Reader reader);

	/**
	 * Get new {@link GraphReader} instance by a format name.
	 * <p>
	 * Any one of the following formats is supported: ['csv', 'dimacs', 'gexf', 'gml', 'graph6', 'space6', 'graphml',
	 * 'leda']
	 *
	 * @param  format the name of the format
	 * @return        a reader that can read graphs of the given format
	 */
	static GraphReader newInstance(String format) {
		return GraphFormat.getInstanceByName(format).newReader();
	}

}
