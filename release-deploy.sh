#!/bin/bash
pushd ../maven-repo
REPO_HOME=`pwd`
mvn clean release:prepare release:perform -Darguments="-DaltDeploymentRepository=release-repo::default::file:$REPO_HOME"
