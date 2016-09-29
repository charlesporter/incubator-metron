#!/usr/bin/env bash

ANSIBLE_KEEP_REMOTE_FILES=1 ansible-playbook -i ./inventory/4node-inc ./../playbooks/metron_install.yml  \
  -vvv --skip-tags="solr,sensors"


