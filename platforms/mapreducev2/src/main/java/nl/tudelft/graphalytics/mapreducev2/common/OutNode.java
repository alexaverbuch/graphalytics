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
package nl.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Marcin Biczak
 */
public class OutNode implements WritableComparable<OutNode> {
    private String id;
    private Vector<Edge> outEdges;
    private final char ignoreChar = '#';

    public OutNode() {}

    public OutNode(String id, Vector<Edge> outEdges) {
        this.id = id;
        this.outEdges = outEdges;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Vector<Edge> getOutEdges() { return this.outEdges; }

    public void setOutEdges(Vector<Edge> outEdges) { this.outEdges = outEdges; }

    public int compareTo(OutNode o) {
        return (this.getId().compareTo(o.getId()) != 0)
            ? this.getId().compareTo(o.getId())
            : this.compareEdges(o.getOutEdges());
    }

    public void write(DataOutput dataOutput) throws IOException {
        String nodeAsString = this.toString();
        dataOutput.writeBytes(nodeAsString);
    }

    public void readFields(DataInput input) throws IOException {
        String nodeLine = input.readLine();
        this.readFields(nodeLine);
    }

    public void readFields(String nodeLine) throws IOException {
        if(nodeLine.charAt(0) != this.ignoreChar) {
            StringTokenizer tokenizer = new StringTokenizer(nodeLine, "@");
            if(tokenizer.countTokens() == 2) {
                this.setId(tokenizer.nextToken());
                StringTokenizer outTokenizer = new StringTokenizer(tokenizer.nextToken(), ",");
                Vector<Edge> tmpEdgeList = new Vector<Edge>();
                while(outTokenizer.hasMoreElements()) {
                    tmpEdgeList.add(new Edge(this.id, outTokenizer.nextToken()));
                }
                this.setOutEdges(tmpEdgeList);
            }
            else
                throw new IOException("Error while reading. File format not supported.");
        }
    }

    public int compareEdges(Vector<Edge> o) {
        if(this.getOutEdges().size() < o.size())
            return -1;
        else if (this.getOutEdges().size() > o.size())
            return 1;
        else {
            Iterator thisIter = this.getOutEdges().iterator();
            Iterator oIter = o.iterator();
            while (thisIter.hasNext()) {
                Edge thisEdge = (Edge) thisIter.next();
                Edge oEdge = (Edge) oIter.next();
                if(thisEdge.compareTo(oEdge) != 0)
                    return thisEdge.compareTo(oEdge);
            }

        }

        return 0;
    }

    public String toString(){
        return this.toText()+" \n";
    }

    public Text toText() {
        boolean isFirst = true;
        StringBuilder result = new StringBuilder();
        result.append(this.getId()).append("@");
        if(this.getOutEdges().size() == 0)
            result.append(","); //putting ',' so that the readFields won't fail
        else {
            Iterator<Edge> iterator = this.getOutEdges().iterator();
            while(iterator.hasNext()) {
                Edge edge = iterator.next();
                if(isFirst) {
                    result.append(edge.getDest());
                    isFirst = false;
                } else
                    result.append(",").append(edge.getDest());
            }
        }

        return new Text(result.toString());
    }

    public OutNode copy() {
        OutNode newObj = new OutNode();
        newObj.setId(this.getId());
        newObj.setOutEdges(this.getOutEdges());
        return newObj;
    }
}

