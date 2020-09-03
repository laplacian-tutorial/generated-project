version: '3'
networks:
  frontend:
  backend:

services:
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