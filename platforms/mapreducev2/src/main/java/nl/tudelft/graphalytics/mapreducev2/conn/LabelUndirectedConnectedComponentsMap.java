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
package nl.tudelft.graphalytics.mapreducev2.conn;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Marcin Biczak
 */
public class LabelUndirectedConnectedComponentsMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    private String id;
    private String label;
    private String[] neighbours;

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        StringBuilder neighbours = new StringBuilder();
        this.readNode(value.toString(), reporter);

        output.collect(new Text(this.id), new Text(this.label));
        for(int i=0; i<this.neighbours.length; i++) {
            output.collect(new Text(this.neighbours[i]), new Text(this.label));
            if(i == 0)
                neighbours.append(this.neighbours[i]);
            else
                neighbours.append(",").append(this.neighbours[i]);

            //report progress
            if(i % 1000 == 0) reporter.progress();
        }

        reporter.progress();

        output.collect(new Text(this.id), new Text("$" + neighbours));
    }

    public void readNode(String line, Reporter reporter) throws IOException {
        StringTokenizer basicTokenizer = new StringTokenizer(line, "$");
        if(basicTokenizer.countTokens() == 2) {
            String basics = basicTokenizer.nextToken();
            String neighbours = basicTokenizer.nextToken();

            // ID and Label
            basicTokenizer = new StringTokenizer(basics, "\t");
            if (basicTokenizer.countTokens() == 2) {
            	this.id = basicTokenizer.nextToken();
            	this.label = basicTokenizer.nextToken();
            } else {
            	this.id = basicTokenizer.nextToken();
            	this.label = this.id;
            }

            // Neighbours
            StringTokenizer neighTokenizer = new StringTokenizer(neighbours,", \t");
            this.neighbours = new String[neighTokenizer.countTokens()];
            int i=0;
            while (neighTokenizer.hasMoreTokens()) {
                this.neighbours[i] = neighTokenizer.nextToken();

                if(i % 1000 == 0) reporter.progress();

                i++;
            }
        } else {
        	basicTokenizer = new StringTokenizer(line, ", \t");
        	int tokenCount = basicTokenizer.countTokens(); 
        	if (tokenCount >= 1) {
        		this.label = this.id = basicTokenizer.nextToken();
        		this.neighbours = new String[tokenCount - 1];
        		int i=0;
                while (basicTokenizer.hasMoreTokens()) {
                    this.neighbours[i] = basicTokenizer.nextToken();

                    if(i % 1000 == 0) reporter.progress();

                    i++;
                }
        	} else {
                throw new IOException("ConnCompRecord requires 2 basicTokens as patter got "+basicTokenizer.countTokens());
            }
    	} 
    }
}

