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
package nl.tudelft.graphalytics.mapreducev2.conversion;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reduces a list of edges in both directions for a single vertex to a single line of output.
 *
 * @author Tim Hegeman
 */
public class DirectedVertexReducer extends Reducer<LongWritable, EdgeData, NullWritable, Text> {

	private Text outValue = new Text();

	@Override
	protected void reduce(LongWritable key, Iterable<EdgeData> values, Context context)
			throws IOException, InterruptedException {
		// Fill separate buffers for incoming and outgoing edges
		StringBuffer sbIn = new StringBuffer();
		StringBuffer sbOut = new StringBuffer();

		// Loop through the messages and add them to the buffers
		boolean foundIn = false, foundOut = false;
		for (EdgeData edge : values) {
			if (edge.getTargetId() == key.get()) {
				// Ignore, this self-edge was added to force this vertex's existence
			} else if (edge.isOutgoing()) {
				if (foundOut)
					sbOut.append(',');
				sbOut.append(edge.getTargetId());
				foundOut = true;
			} else {
				if (foundIn)
					sbIn.append(',');
				sbIn.append(edge.getTargetId());
				foundIn = true;
			}
		}
		
		// Combine the vertex ID and neighbour lists using Marcin's format
		StringBuffer out = new StringBuffer(key.toString());
		out.append("\t#")
			.append(sbIn.toString())
			.append("\t@")
			.append(sbOut.toString());
		if (!foundOut)
			out.append('\t');
		
		// Output the constructed line
		outValue.set(out.toString());
		context.write(NullWritable.get(), outValue);
	}
	
}
