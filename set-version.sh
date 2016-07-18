#!/bin/sh
#mvn tycho-versions:set-version -Dversion=$1
mvn versions:set -DnewVersion=$1
