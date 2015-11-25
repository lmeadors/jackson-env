#!/bin/bash
#pushd ../maven-repo
#REPO_HOME=`pwd`
#popd
mvn clean jgitflow:release-start jgitflow:release-finish deploy
