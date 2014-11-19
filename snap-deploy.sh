#!/bin/bash
mvn -DaltDeploymentRepository=snapshot-repo::default::file:../maven-repo/snapshots clean deploy
