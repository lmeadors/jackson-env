#!/bin/bash

OPEN=`which xdg-open`
if [[ -z $OPEN ]]; then
    OPEN=`which open`
fi

mvn clean cobertura:cobertura surefire-report:report-only
$OPEN target/site/surefire-report.html
$OPEN target/site/cobertura/index.html
