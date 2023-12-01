#!/bin/bash

# This script creates a docker config.json file with the auth section
# as an example of what can be passed into GitLab-CI and used in
# conjunction with DOCKER_CONFIG - the config file directory location.

# command line parameter default values
DOCKER_REGISTRY=""
DOCKER_USER=""
DOCKER_PASSWORD=""
WRITE="no"

function parse_params() {
    while getopts r:u:p:hw arg; do
        CLEAN_ARG=`echo ${OPTARG} | tr -d '[:cntrl:]'`
        case $arg in
            h)
                echo "./${0} [-r <Docker registry: eg: docker.io> -u <Docker User: eg: ${USER}@some.where> -p <Docker Password: eg: secret> -w <actually write file>]"
                exit
                ;;
            r)
                DOCKER_REGISTRY="${CLEAN_ARG}"
                ;;
            u)
                DOCKER_USER="${CLEAN_ARG}"
                ;;
            p)
                DOCKER_PASSWORD="${CLEAN_ARG}"
                ;;
            w)
                WRITE="yes"
                ;;
        esac
    done
}

# parse command line parameters if we have some
if [ $# -ge 1 ]; then
    parse_params $@;
fi

# must be either - not both
if [ "${INSERT}" = "yes" ] && [ "${DELETE}" = "yes" ]; then
  echo "Please select -i (inset xhost entries) or -d (delete xhost entries) - not both"
  exit 1
fi

# if neither then effectively noop mode
if [ "${INSERT}" = "no" ] && [ "${DELETE}" = "no" ]; then
  echo "No update selected (insert or delete), noop mode only"
fi

# no network specified then try auto-discovery
if [ -z "${DOCKER_REGISTRY}" ] || [ -z "${DOCKER_USER}" ] || [ -z "${DOCKER_PASSWORD}" ]; then
    echo "Please specify registry (-r), user (-u), and password (-p)"
    exit 1
fi
PASSWD=`echo "${DOCKER_USER}:${DOCKER_PASSWORD}" | tr -d '\n' | base64 -i -w 0`
echo "Selected registry ${DOCKER_REGISTRY} and user ${DOCKER_USER} (base64 encoded credentials: ${PASSWD})"

CONFIG="\
{\n
    \"auths\": {\n
        \"${DOCKER_REGISTRY}\": {\n
            \"auth\": \"${PASSWD}\"\n
        }\n
    }\n
}\n"


# check for noop
if [ "${WRITE}" = "yes" ]; then
    echo "Writing to config.json"
    printf "${CONFIG}" > config.json
fi
echo "Docker config:"
printf "${CONFIG}"
echo "Docker config base64 endcoded:"
printf "${CONFIG}" | base64 -w 0
echo ""

exit 0