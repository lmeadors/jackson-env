#!/bin/bash
#pushd ../maven-repo
#REPO_HOME=`pwd`
#popd
mvn clean release:prepare release:perform
