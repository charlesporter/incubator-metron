#!/usr/bin/env bash
ansible-playbook  -i  ./inventory/4node-inc ./../playbooks/ambari_install.yml  \
  -vvvv --skip-tags="solr,sensors"
