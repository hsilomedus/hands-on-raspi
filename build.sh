#!/bin/bash
echo ----------------------------------------------------------------------
echo Building...
echo Cleaning target folder
rm -rf mantarget
echo Create directory
mkdir mantarget
echo Copy libs
cp ./lib/*.jar ./mantarget
echo Compile
javac -d mantarget ./src/*.java
echo Done!
echo ----------------------------------------------------------------------

