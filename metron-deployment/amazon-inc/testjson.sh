#!/usr/bin/env bash
ANSIBLE_KEEP_REMOTE_FILES=1  ansible-playbook -i localhost ./test.yml  -vvvv