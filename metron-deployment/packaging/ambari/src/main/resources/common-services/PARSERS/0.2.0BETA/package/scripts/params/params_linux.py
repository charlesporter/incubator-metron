#!/usr/bin/env python
"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""

import functools
import os

from resource_management.libraries.functions import conf_select
from resource_management.libraries.functions import format
from resource_management.libraries.functions import get_kinit_path
from resource_management.libraries.functions import stack_select
from resource_management.libraries.functions.default import default
from resource_management.libraries.functions.get_not_managed_resources import get_not_managed_resources
from resource_management.libraries.resources.hdfs_resource import HdfsResource
from resource_management.libraries.script import Script

# server configurations
config = Script.get_config()

hostname = config['hostname']
#print(config['configurations'])
metron_home = config['configurations']['metron-parsers']['metron_home']
parsers = config['configurations']['metron-parsers']['parsers']
metron_user = config['configurations']['metron-parsers']['metron_user']
metron_group = config['configurations']['metron-parsers']['metron_group']
metron_zookeeper_config_dir = config['configurations']['metron-parsers']['metron_zookeeper_config_dir']
#metron_zookeeper_config_path = metron_home + "/" + metron_zookeeper_config_dir
metron_zookeeper_config_path = format('{metron_home}/{metron_zookeeper_config_dir}')
configured_flag_file = metron_zookeeper_config_path + '/../metron_is_configured'
yum_repo_type = 'local'

# hadoop params
stack_root = Script.get_stack_root()
hadoop_home_dir = stack_select.get_hadoop_dir("home")
hadoop_bin_dir = stack_select.get_hadoop_dir("bin")
hadoop_conf_dir = conf_select.get_hadoop_conf_dir()
kafka_home = os.path.join(stack_root, "current", "kafka-broker")
kafka_bin_dir = os.path.join(kafka_home, "bin")

# zookeeper
zk_hosts = default("/clusterHostInfo/zookeeper_hosts", [])
has_zk_host = not len(zk_hosts) == 0
zookeeper_quorum = None
if has_zk_host:
    if 'zoo.cfg' in config['configurations'] and 'clientPort' in config['configurations']['zoo.cfg']:
        zookeeper_clientPort = config['configurations']['zoo.cfg']['clientPort']
    else:
        zookeeper_clientPort = '2181'
    zookeeper_quorum = (':' + zookeeper_clientPort + ',').join(config['clusterHostInfo']['zookeeper_hosts'])
    # last port config
    zookeeper_quorum += ':' + zookeeper_clientPort

# Kafka
kafka_hosts = default("/clusterHostInfo/kafka_broker_hosts", [])
has_kafka_host = not len(kafka_hosts) == 0
kafka_brokers = None
if has_kafka_host:
    if 'port' in config['configurations']['kafka-broker']:
        kafka_broker_port = config['configurations']['kafka-broker']['port']
    else:
        kafka_broker_port = '6667'
    kafka_brokers = (':' + kafka_broker_port + ',').join(config['clusterHostInfo']['kafka_broker_hosts'])
    kafka_brokers += ':' + kafka_broker_port

metron_apps_dir = config['configurations']['metron-parsers']['metron_apps_hdfs_dir']

local_grok_patterns_dir = format("{metron_home}/patterns")
hdfs_grok_patterns_dir = format("{metron_apps_dir}/patterns")

# for create_hdfs_directory
security_enabled = config['configurations']['cluster-env']['security_enabled']
hostname = config["hostname"]
hdfs_user_keytab = config['configurations']['hadoop-env']['hdfs_user_keytab']
hdfs_user = config['configurations']['hadoop-env']['hdfs_user']
hdfs_principal_name = config['configurations']['hadoop-env']['hdfs_principal_name']
smokeuser_principal = config['configurations']['cluster-env']['smokeuser_principal_name']
kinit_path_local = get_kinit_path(default('/configurations/kerberos-env/executable_search_paths', None))
hdfs_site = config['configurations']['hdfs-site']
default_fs = config['configurations']['core-site']['fs.defaultFS']
dfs_type = default("/commandParams/dfs_type", "")

# create partial functions with common arguments for every HdfsResource call
# to create/delete hdfs directory/file/copyfromlocal we need to call params.HdfsResource in code
HdfsResource = functools.partial(
    HdfsResource,
    user=hdfs_user,
    hdfs_resource_ignore_file="/var/lib/ambari-agent/data/.hdfs_resource_ignore",
    security_enabled=security_enabled,
    keytab=hdfs_user_keytab,
    kinit_path_local=kinit_path_local,
    hadoop_bin_dir=hadoop_bin_dir,
    hadoop_conf_dir=hadoop_conf_dir,
    principal_name=hdfs_principal_name,
    hdfs_site=hdfs_site,
    default_fs=default_fs,
    immutable_paths=get_not_managed_resources(),
    dfs_type=dfs_type
)
