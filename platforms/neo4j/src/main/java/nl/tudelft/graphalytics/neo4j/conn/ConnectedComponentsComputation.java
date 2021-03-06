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
package nl.tudelft.graphalytics.neo4j.conn;

import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Implementation of the connected components algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class ConnectedComponentsComputation {

	public static final String COMPONENT = "COMPONENT";
	private final GraphDatabaseService graphDatabase;

	/**
	 * @param graphDatabase graph database representing the input graph
	 */
	public ConnectedComponentsComputation(GraphDatabaseService graphDatabase) {
		this.graphDatabase = graphDatabase;
	}

	/**
	 * Executes the connected components algorithm by setting the COMPONENT property of all nodes to the smallest node
	 * ID in each component.
	 */
	public void run() {
		// Initialize the component property of all nodes to their own ID
		initializeComponents();

		// Repeatedly assign to each node the minimum component IDs of their neighbours, until convergence
		boolean finished = false;
		while (!finished) {
			finished = true;
			try (Transaction transaction = graphDatabase.beginTx()) {
				for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
					long componentId = getComponentId(node);
					if (componentId != (long) node.getProperty(COMPONENT)) {
						node.setProperty(COMPONENT, componentId);
						finished = false;
					}
				}
				transaction.success();
			}
		}
	}

	private void initializeComponents() {
		try (Transaction transaction = graphDatabase.beginTx()) {
			for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
				node.setProperty(COMPONENT, node.getProperty(Neo4jConfiguration.ID_PROPERTY));
			}
			transaction.success();
		}
	}

	private long getComponentId(Node node) {
		long smallestComponentId = (long) node.getProperty(COMPONENT);
		for (Relationship edge : node.getRelationships(Neo4jConfiguration.EDGE, Direction.BOTH)) {
			long componentId = (long) edge.getOtherNode(node).getProperty(COMPONENT);
			if (componentId < smallestComponentId)
				smallestComponentId = componentId;
		}
		return smallestComponentId;
	}

}
