#!/bin/bash
mvn clean cobertura:cobertura surefire-report:report-only
open target/site/surefire-report.html
open target/site/cobertura/index.html
