/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.PlatformBenchmarkResult;
import nl.tudelft.graphalytics.domain.NestedConfiguration;

/**
 * The common interface for any platform that implements the Graphalytics benchmark suite. It
 * defines the API that must be provided by a platform to be compatible with the Graphalytics
 * benchmark driver. The driver uses the {@link #uploadGraph(Graph, String) uploadGraph} and
 * {@link #deleteGraph(String) deleteGraph} functions to ensure the right graphs are loaded,
 * and uses {@link #executeAlgorithmOnGraph(Algorithm, Graph, Object) executeAlgorithmOnGraph}
 * to trigger the executing of various algorithms on each graph.
 *
 * @author Tim Hegeman
 */
public interface Platform {

	/**
	 * Called before executing algorithms on a graph to allow the platform driver to import a graph.
	 * This may include uploading to a distributed filesystem, importing in a graph database, etc.
	 * The platform driver must ensure that this dataset remains available for multiple calls to
	 * {@link #executeAlgorithmOnGraph(Algorithm, Graph, Object) executeAlgorithmOnGraph}, until
	 * the removal of the graph is triggered using {@link #deleteGraph(String) deleteGraph}.
	 *
	 * @param graph         information on the graph to be uploaded
	 * @param graphFilePath the path of the graph data
	 * @throws Exception if any exception occurred during the upload
	 */
	void uploadGraph(Graph graph, String graphFilePath) throws Exception;

	/**
	 * Called to trigger the executing of an algorithm on a specific graph. The execution of this
	 * method is timed as part of the benchmarking process. The benchmark driver guarantees that the
	 * graph has been uploaded using the {@link #uploadGraph(Graph, String) uploadGraph} method, and
	 * that it has not been removed by a corresponding call to {@link #deleteGraph(String)
	 * deleteGraph}.
	 *
	 * @param algorithm  the algorithm to execute
	 * @param graph      the graph to execute the algorithm on
	 * @param parameters the algorithm- and graph-specific parameters
	 * @return a PlatformBenchmarkResult object detailing the execution of the algorithm
	 * @throws PlatformExecutionException if any exception occurred during the execution of the algorithm, or if
	 *                                    the platform otherwise failed to complete the algorithm successfully
	 */
	PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters)
			throws PlatformExecutionException;

	/**
	 * Called by the benchmark driver to signal when a graph may be removed from the system. The
	 * driver guarantees that every graph that is uploaded using {@link #uploadGraph(Graph, String)
	 * uploadGraph} is removed using exactly one corresponding call to this method.
	 *
	 * @param graphName the name of the graph to remove (see {@link Graph#getName()})
	 */
	void deleteGraph(String graphName);

	/**
	 * A unique identifier for the platform, used to name benchmark results, etc.
	 * This should be the same as the platform name used to compile and run the benchmark
	 * for this platform, for consistency.
	 *
	 * @return the unique name of the platform
	 */
	public String getName();

	/**
	 * Returns a PlatformConfiguration object which describes the configuration of the platform
	 * in detail. This information should include all configuration options explicitly set by the user
	 * or the platform driver, especially those options that can affect performance. The configuration
	 * details are used by the Graphalytics core to include in the generated benchmark reports.
	 *
	 * @return the configuration of the platform
	 */
	public NestedConfiguration getPlatformConfiguration();

}
