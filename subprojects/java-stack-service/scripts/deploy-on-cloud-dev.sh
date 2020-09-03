version: '3'
networks:
  frontend:
  backend:

services:
# @+services|laplacian-arch.java-stack-service.project-template-1.0.0!dest/{each environments as environment}/{hyphen environment.name}/{each environment.deployments as deployment}/{with deployment.component as component}/{if (eq component.type 'springboot2_api_service')}/docker-compose_services_.yaml.hbs@
#
  # tutorial_api
  #
  tutorial-api-default-datasource-migrate:
    build:
      context: ./tutorial-api-default-datasource-migrate
      dockerfile: Dockerfile
    container_name: tutorial-api-default-datasource-migrate
    environment:
    - 'DATASOURCE_URL=jdbc:postgresql://tutorial-test-db:5432/tutorial_db'
    - 'DATASOURCE_USER=test'
    - 'DATASOURCE_PASS=secret'
    networks:
    - backend

  tutorial-api:
    build:
      context: ./tutorial-api
      dockerfile: Dockerfile
    container_name: tutorial-api
    ports:
    - '8080:8080'
    expose:
    - '8080'
    environment:
    - 'DATASOURCE_URL=r2dbc:pool:postgresql://tutorial-test-db:5432/tutorial_db'
    - 'DATASOURCE_USER=test'
    - 'DATASOURCE_PASS=secret'
    networks:
    - backend
# @services|laplacian-arch.java-stack-service.project-template-1.0.0!dest/{each environments as environment}/{hyphen environment.name}/{each environment.deployments as deployment}/{with deployment.component as component}/{if (eq component.type 'springboot2_api_service')}/docker-compose_services_.yaml.hbs@
# @+services|laplacian-arch.java-stack-service.project-template-1.0.0!dest/{each environments as environment}/{hyphen environment.name}/{each environment.deployments as deployment}/{with deployment.component as component}/{if (eq component.type 'postgres_test_db')}/docker-compose_services_.yaml.hbs@
#
  # tutorial_test_db
  #
  tutorial-test-db:
    image: postgres
    container_name: tutorial-test-db
    ports:
    - '5432:5432'
    expose:
    - '5432'
    environment:
    - 'POSTGRES_USER=test'
    - 'POSTGRES_PASSWORD=secret'
    - 'POSTGRES_DB=tutorial_db'
    networks:
    - backend
# @services|laplacian-arch.java-stack-service.project-template-1.0.0!dest/{each environments as environment}/{hyphen environment.name}/{each environment.deployments as deployment}/{with deployment.component as component}/{if (eq component.type 'postgres_test_db')}/docker-compose_services_.yaml.hbs@
# @+services@
# @services@