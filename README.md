# Graphalytics: Graph Analytics Benchmark for Big Data Frameworks

Graphalytics is a benchmark for graph processing frameworks.

## Getting started

A packaged version of Graphalytics is currently available on the @Large server,
in `/data/graphalytics/`. Packages are named `graphalytics-platforms-${platform}-${version}-bin.tar.gz`,
so you can download the latest version for the platform you wish to benchmark. Currently,
only MapReduce version 2 (labeled `mapreducev2`) and Giraph (`giraph`) are available.
After unpacking the distribution, there are three steps to prepare the benchmark:

 1. Add graphs to the benchmark (see "How to add graphs to Graphalytics?").
 2. Edit the Graphalytics configuration (see "How to configure Graphalytics?").
 3. Ensure the platform under test is configured and running (see "Platform-specific information").

To run the benchmark, execute the launch script (e.g., for MapReduce v2):

```
./run-benchmark.sh mapreducev2
```

After the benchmark has completed, the results can be found in `${platform}-report`

## How to add graphs to Graphalytics?

Prepared graphs can be found on the @Large server in `/data/graphalytics/graphs/`.
To add (a subset of) these graphs to your local copy of the Graphalytics benchmark,
first download the `/data/graphalytics/graphs/config` directory and merge it
with the `<graphalytics>/config` folder. Next, download some or all graph files
and store them locally (we will assume in `/local/graphs/` for this guide).
Finally, you must edit the `graphs.root-directory` property in `<graphalytics>/config/graphs.properties`
file to point to the graphs you have downloaded, e.g.:

```
graphs.root-directory = /local/graphs/
```

If you did not download all graphs, you need to specify which graphs you have by editing
the `graphs.names` property. For example:

```
graphs.names = ldbc-1, ldbc-3, ldbc-10
```

to select only the three smallest LDBC graphs.

## How to configure Graphalytics?

You can specify in the Graphalytics configuration a subset of graphs and algorithms to run.
By default, all algorithms are run on all the available graphs. This can be changed by
creating a "run" properties file in `<graphalytics>/config/runs/`. See
`<graphalytics>/config/runs/example.properties` for an example. A particular run can be
selected by editing `<graphalytics>/config/benchmark.properties` and including a different
file from the `runs` subdirectory.

## Platform-specific information

### MapReduce V2

The `mapreducev2` benchmark runs on Hadoop version 2.4.1 or later (may work for earlier versions,
this has not been verified). Before launching the benchmark, ensure Hadoop is operational and in
either pseudo-distributed or distributed mode. Next, edit the `mapreducev2`-specific configuration
file: `<graphalytics>/config/mapreducev2.properties`. Set the `hadoop.home` property to the directory
of your Hadoop installation (i.e., the HADOOP_HOME environment variable) and `mapreducev2.reducer-count`
to an appropriate number of reducers for your Hadoop deployment (note: variable number of reducers
per graph/algorithm is not yet supported).

### Giraph

The `giraph` benchmark runs on Hadoop version 2.4.1 or later (again, earlier versions have not been
attempted) and requires ZooKeeper (tested with 3.3.3+). The benchmark includes a copy of Giraph
version 1.1.0 compiled to run in "pure YARN" mode (i.e., without the MapReduce layer). Before
launching the benchmark, ensure Hadoop is running in either pseudo-distributed or distributed mode,
and ensure that the ZooKeeper service is running. Note that the Giraph benchmark will use MapReduce
jobs to preprocess the input graphs, so MapReduce must be properly configured. Next, edit
`<graphalytics>/config/giraph.properties` and change the following settings:

 - `hadoop.home`: Set to the root of your Hadoop installation (HADOOP_HOME).
 - `giraph.zoo-keeper-address`: Set to the hostname and port on which ZooKeeper is running.
 - `giraph.preprocessing.num-reducers`: Set to an appropriate number of reducers for the MR cluster.
 - `giraph.job.heap-size`: Set to the amount of memory (in MB) each worker should have.
 - `giraph.job.worker-count`: Set to an appropriate number of workers for the Hadoop cluster.
   Note that Giraph launches an Application Master (512 MB) and `worker-count + 1` containers
   of size `giraph.job.heap-size`.
