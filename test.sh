#!/usr/bin/env bash

java -version

if command -v gnome-open
then
	OPEN="gnome-open"
elif command -v xdg-open
then
	OPEN="xdg-open"
else
	OPEN="open"
fi

# use UTC timezone for JVM
BUILD_OPTS="$BUILD_OPTS -Duser.timezone=GMT"

MVN_CMD="mvn clean jacoco:prepare-agent test"

MVN_CMD="$MVN_CMD site jxr:jxr"

if [ -z "$1" ]; then
	MVN_CMD="$MVN_CMD $BUILD_OPTS"
else
	MVN_CMD="$MVN_CMD -Dtest=$1 $BUILD_OPTS"
fi

echo $MVN_CMD
$MVN_CMD

$OPEN target/site/index.html > /dev/null 2>&1
