#!/bin/bash
source classpath.sh
rm -rf bin/
mkdir bin/
javac `find src/ | grep \.java | tr "\n" " "` -cp $CLASSPATH -d bin
