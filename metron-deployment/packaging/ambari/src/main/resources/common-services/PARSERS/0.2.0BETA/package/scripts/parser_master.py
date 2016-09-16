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

from resource_management.core.exceptions import ComponentIsNotRunning
from resource_management.core.logger import Logger
from resource_management.libraries.script import Script

from commands import Commands


class ParserMaster(Script):
    def get_component_name(self):
        # TODO add this at some point - currently will cause problems with hdp-select
        # return "parser-master"
        pass

    def install(self, env):
        from params import params
        env.set_params(params)
        commands = Commands(params)
        commands.setup_repo()
        Logger.info('Install RPM packages')
        self.install_packages(env)

    def configure(self, env, upgrade_type=None, config_dir=None):
        from params import params
        env.set_params(params)

    def start(self, env, upgrade_type=None):
        from params import params
        env.set_params(params)
        commands = Commands(params)
        if not commands.is_configured():
            commands.init_parsers()
            commands.init_kafka_topics()
            commands.init_parser_config()
            commands.set_configured()
        commands.start_parser_topologies()

    def stop(self, env, upgrade_type=None):
        from params import params
        env.set_params(params)
        commands = Commands(params)
        commands.stop_parser_topologies()

    def status(self, env):
        from params import status_params
        env.set_params(status_params)
        commands = Commands(status_params)
        if not commands.topologies_running():
            raise ComponentIsNotRunning()

    def restart(self, env):
        from params import params
        env.set_params(params)
        commands = Commands(params)
        commands.restart_parser_topologies()


if __name__ == "__main__":
    ParserMaster().execute()
