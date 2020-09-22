#!/usr/bin/env bash

set -e
set -x

java  \
  ${IMAGE_VERSION:+ -Dimage.version=}${IMAGE_VERSION} \
  ${DATASOURCE_URL:+ -Ddatasource.url=}${DATASOURCE_URL} \
  ${DATASOURCE_USER:+ -Ddatasource.username=}${DATASOURCE_USER} \
  ${DATASOURCE_PASS:+ -Ddatasource.password=}${DATASOURCE_PASS} \
  -jar /app/db-migrate.jar