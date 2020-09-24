#!/usr/bin/env bash

set -e
set -x

java \
  -Djava.security.egd=file:/dev/./urandom \
  ${IMAGE_VERSION:+ -Dimage.version=}${IMAGE_VERSION} \
  -jar /app/api.jar \
  ${DATASOURCE_URL:+ --spring.r2dbc.url=}${DATASOURCE_URL} \
  ${DATASOURCE_USER:+ --spring.r2dbc.username=}${DATASOURCE_USER} \
  ${DATASOURCE_PASS:+ --spring.r2dbc.password=}${DATASOURCE_PASS} \
  ${REST_CLIENT_GOOGLE_SHEETS_API_BASE_URL:+ --rest_client.google_sheets_api.base_url=}${REST_CLIENT_GOOGLE_SHEETS_API_BASE_URL} \
  ${REST_CLIENT_GOOGLE_SHEETS_API_API_KEY:+ --rest_client.google_sheets_api.api_key=}${REST_CLIENT_GOOGLE_SHEETS_API_API_KEY} \
  ${SEARCH_ENGINE_CLIENT_DEFAULT_BASE_URL:+ --search_engine_client.default.endpoints=}${SEARCH_ENGINE_CLIENT_DEFAULT_ENDPOINTS} \