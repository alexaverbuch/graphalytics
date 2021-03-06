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
package nl.tudelft.graphalytics.mapreducev2.bfs;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;
import nl.tudelft.graphalytics.mapreducev2.bfs.BreadthFirstSearchConfiguration.NODE_STATUS;

import static nl.tudelft.graphalytics.mapreducev2.bfs.BreadthFirstSearchConfiguration.SOURCE_VERTEX_KEY;

/**
 * Job specification for breadth-first search on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchJob extends MapReduceJob<BreadthFirstSearchParameters> {
	
	private boolean directed;
	private boolean finished = false;
	
	public BreadthFirstSearchJob(String inputPath, String intermediatePath,
			String outputPath, BreadthFirstSearchParameters parameters, boolean directed) {
		super(inputPath, intermediatePath, outputPath, parameters);
		this.directed = directed;
	}

	@Override
	protected Class<?> getMapOutputKeyClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getMapOutputValueClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getOutputKeyClass() {
		return NullWritable.class;
	}

	@Override
	protected Class<?> getOutputValueClass() {
		return Text.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends InputFormat> getInputFormatClass() {
		return TextInputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends OutputFormat> getOutputFormatClass() {
		return TextOutputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Mapper> getMapperClass() {
		return (directed ?
				DirectedBreadthFirstSearchMap.class :
				UndirectedBreadthFirstSearchMap.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return GenericBreadthFirstSearchReducer.class;
	}

	@Override
	protected boolean isFinished() {
		return finished;
	}
	
	@Override
	protected void setConfigurationParameters(JobConf jobConfiguration) {
		super.setConfigurationParameters(jobConfiguration);
		jobConfiguration.set(SOURCE_VERTEX_KEY, Long.toString(getParameters().getSourceVertex()));
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		Counters jobCounters = jobExecution.getCounters();
    	long nodesVisisted = jobCounters.getCounter(NODE_STATUS.VISITED);
    	if (nodesVisisted == 0)
    		finished = true;
    	
    	System.out.println("\n************************************");
        System.out.println("* BFS Iteration "+ getIteration() +" FINISHED *");
        System.out.println("************************************\n");
	}

}
