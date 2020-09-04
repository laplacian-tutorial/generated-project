#!/usr/bin/env bash
set -e
PROJECT_BASE_DIR=$(cd $"${BASH_SOURCE%/*}/../" && pwd)

SCRIPT_BASE_DIR="$PROJECT_BASE_DIR/scripts"


OPT_NAMES='hv-:'

ARGS=
HELP=
VERBOSE=


# @main@
DEPLOYMENT_BASE_DIR=$PROJECT_BASE_DIR/dest/development/
main () {
  build_apps
  register_container_images
  deploy_with_terraform
  migrate_test_data
}

build_apps() {
  (cd $DEPLOYMENT_BASE_DIR/tutorial-api-default-datasource-migrate
    ./gradlew build
  )
  (cd $DEPLOYMENT_BASE_DIR/tutorial-api
    ./gradlew build
  )
}




register_container_images() {
  (cd $DEPLOYMENT_BASE_DIR
    docker-compose build
    docker-compose push
  )
}

deploy_with_terraform() {
  (cd $DEPLOYMENT_BASE_DIR/terraform
    terraform init
    terraform apply -auto-approve
  )
}

migrate_test_data() {
  local datastore_ip=
  datastore_ip=$(cd $DEPLOYMENT_BASE_DIR/terraform && terraform output tutorial_test_db_ip)
  (cd $DEPLOYMENT_BASE_DIR/tutorial-api-default-datasource-migrate
    java  \
      -Ddatasource.url="jdbc:postgresql://$datastore_ip:5432/tutorial_db" \
      -Ddatasource.username="test" \
      -Ddatasource.password="secret" \
      -jar ./build/libs/db-migrate-*.jar
  )
}
# @main@

# @+additional-declarations@
# @additional-declarations@

parse_args() {
  while getopts $OPT_NAMES OPTION;
  do
    case $OPTION in
    -)
      case $OPTARG in
      help)
        HELP='yes';;
      verbose)
        VERBOSE='yes';;
      *)
        echo "ERROR: Unknown OPTION --$OPTARG" >&2
        exit 1
      esac
      ;;
    h) HELP='yes';;
    v) VERBOSE='yes';;
    esac
  done
  ARGS=$@
}

show_usage () {
cat << 'END'
Usage: ./scripts/deploy-on-development.sh [OPTION]...
  -h, --help
    Displays how to use this command.
  -v, --verbose
    Displays more detailed command execution information.
END
}

parse_args "$@"

! [ -z $VERBOSE ] && set -x
! [ -z $HELP ] && show_usage && exit 0
main