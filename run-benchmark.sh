#!/bin/bash

if [ "$#" -lt "1" ]; then
	echo "Usage: ${BASH_SOURCE[0]} <platform>"
	exit 1
fi

PLATFORM=$1

cd $(dirname ${BASH_SOURCE[0]})

mvn package install -pl platforms/$PLATFORM -am -Dmaven.repo.local="$(pwd)/.m2/"
mvn -q dependency:build-classpath -pl platforms/$PLATFORM -Dmdep.outputFile="$(pwd)/platforms/$PLATFORM/.maven-classpath" -Dmaven.repo.local="$(pwd)/.m2/"

CLASSPATH=$(find $(pwd)/platforms/$PLATFORM/target/graphalytics-platforms-*-jar-with-dependencies.jar):$(cat platforms/$PLATFORM/.maven-classpath):$HADOOP_HOME/etc/hadoop/

java -cp $CLASSPATH org.tudelft.graphalytics.Graphalytics $PLATFORM
