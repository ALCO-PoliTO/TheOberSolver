#!/bin/bash
echo "Starting the OberSolver..."

BASEPATH="/Users/gabrieledragotto/Applications/IBM/ILOG/CPLEX_Studio127"

LIBRARYPATH=$LIBRARYPATH:$BASEPATH/cplex/bin/x86-64_osx:$BASEPATH/cpoptimizer/bin/x86-64_osx
DYLD_LIBRARY_PATH=$BASEPATH/cplex/bin/x86-64_osx:$BASEPATH/cpoptimizer/bin/x86-64_osx
CLASSPATH=$BASEPATH/cplex/lib/cplex.jar:$BASEPATH/cpoptimizer/lib/ILOG.CP.jar

export CLASSPATH=$CLASSPATH
export LIBRARYPATH=$LIBRARYPATH
export DYLD_LIBRARY_PATH=.:$DYLD_LIBRARY_PATH:${DYLD_LIBRARY_PATH}

java -jar Obersolver.jar -Djava.library.path=$LIBRARYPATH -classpath=$CLASSPATH
