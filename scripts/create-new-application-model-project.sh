#!/usr/bin/env bash
set -e
PROJECT_BASE_DIR=$(cd $"${BASH_SOURCE%/*}/../" && pwd)

SCRIPT_BASE_DIR="$PROJECT_BASE_DIR/scripts"


OPT_NAMES='hv-:'

ARGS=
HELP=
VERBOSE=
PROJECT_NAME=application-model
PROJECT_VERSION=0.0.1
NAMESPACE=laplacian.tutorial


# @main@
SUBPROJECTS_DIR=
NEW_PROJECTS_MODEL_FILE=

main() {
  SUBPROJECTS_DIR="model/project/subprojects/laplacian-tutorial"
  NEW_PROJECTS_MODEL_FILE="$PROJECT_BASE_DIR/$SUBPROJECTS_DIR/$PROJECT_NAME.yaml"

  create_subproject_model_file
  update_project
  show_next_action_message
}

create_subproject_model_file() {
  mkdir -p $(dirname $NEW_PROJECTS_MODEL_FILE)
cat <<EOF > $NEW_PROJECTS_MODEL_FILE
_description: &description
  en: |
    The $PROJECT_NAME project.

project:
  subprojects:
  - group: 'laplacian-tutorial'
    type: 'application-model'
    name: '$PROJECT_NAME'
    namespace: '$NAMESPACE'
    description: *description
    version: '$PROJECT_VERSION'
#   source_repository:
#     url: https://github.com/laplacian-tutorial/$PROJECT_NAME.git
    model_files:
    - dest/model
EOF
}

hyphenize() {
  local str=$1
  echo  ${str//[_.: ]/-}
}

update_project() {
  $SCRIPT_BASE_DIR/generate.sh
}

show_next_action_message() {
  echo ""
  echo "Created the new project's definition at: $NEW_PROJECTS_MODEL_FILE"
  echo "1. Edit this file if you need."
  echo "2. Run ./scripts/generate-$(hyphenize ${PROJECT_NAME}).sh to generate the project's content."
  echo ""
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
      project-name)
        PROJECT_NAME=("${!OPTIND}"); OPTIND=$(($OPTIND+1));;
      project-version)
        PROJECT_VERSION=("${!OPTIND}"); OPTIND=$(($OPTIND+1));;
      namespace)
        NAMESPACE=("${!OPTIND}"); OPTIND=$(($OPTIND+1));;
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

read_user_input() {
  local input=
  read -p "Enter project-name${PROJECT_NAME:+$(printf ' [%s]' $PROJECT_NAME)}: " input
  PROJECT_NAME=${input:-"$PROJECT_NAME"}
  read -p "Enter project-version${PROJECT_VERSION:+$(printf ' [%s]' $PROJECT_VERSION)}: " input
  PROJECT_VERSION=${input:-"$PROJECT_VERSION"}
  read -p "Enter namespace${NAMESPACE:+$(printf ' [%s]' $NAMESPACE)}: " input
  NAMESPACE=${input:-"$NAMESPACE"}
}

show_usage () {
cat << 'END'
Usage: ./scripts/create-new-application-model-project.sh [OPTION]...
  -h, --help
    Displays how to use this command.
  -v, --verbose
    Displays more detailed command execution information.
  --project-name [VALUE]
    New project's name (Default: application-model)
  --project-version [VALUE]
    The initial version number (Default: 0.0.1)
  --namespace [VALUE]
    Namespace (Default: laplacian.tutorial)
END
}

parse_args "$@"
read_user_input

! [ -z $VERBOSE ] && set -x
! [ -z $HELP ] && show_usage && exit 0
main