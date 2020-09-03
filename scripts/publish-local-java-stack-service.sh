#!/usr/bin/env bash
set -e
PROJECT_BASE_DIR=$(cd $"${BASH_SOURCE%/*}/../" && pwd)

SCRIPT_BASE_DIR="$PROJECT_BASE_DIR/scripts"


OPT_NAMES='hv-:'

ARGS=
HELP=
VERBOSE=


# @main@
SCRIPTS_DIR='scripts'
PUBLISH_SCRIPT='publish-local.sh'
TARGET_PROJECT_DIR="${PROJECT_BASE_DIR}/subprojects/java-stack-service"
TARGET_PUBLISH_SCRIPT="$TARGET_PROJECT_DIR/$SCRIPTS_DIR/$PUBLISH_SCRIPT"

main() {
  if ! [ -f $TARGET_PUBLISH_SCRIPT ]
  then
    run_generate
  fi
  $TARGET_PUBLISH_SCRIPT
}

run_generate() {
  $PROJECT_BASE_DIR/$SCRIPTS_DIR/generate-java-stack-service.sh
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
Usage: ./scripts/publish-local-java-stack-service.sh [OPTION]...
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