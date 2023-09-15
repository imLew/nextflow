/*
 * Copyright 2013-2023, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.cli.v2

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.cli.CliOptions
import nextflow.cli.CmdInspect
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

/**
 * CLI `inspect` sub-command (v2)
 *
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@Slf4j
@CompileStatic
@Command(
    name = 'inspect',
    description = 'Inspect process settings in a pipeline project'
)
class InspectCmd extends AbstractCmd implements CmdInspect.Options {

    @ParentCommand
    private Launcher launcher

    @Parameters(index = '0', description = 'Project name or repository url')
    String pipeline

    @Parameters(index = '1..*', description = 'Pipeline script args')
    List<String> args = []

    @Option(names = ['-concretize'], description = "Build the container images resolved by the inspect command")
    boolean concretize

    @Option(names = ['-c','-config'], hidden = true)
    List<String> runConfig

    @Option(names = ['-format'], description = "Inspect output format. Can be 'json' or 'config'")
    String format = 'json'

    @Option(names = ['-i','-ignore-errors'], description = 'Ignore errors while inspecting the pipeline')
    boolean ignoreErrors

    @Option(names = '-params-file', description = 'Load script parameters from a JSON/YAML file')
    String paramsFile

    @Option(names = ['-profile'], description = 'Use the given configuration profile(s)')
    String profile

    @Option(names = ['-r','-revision'], description = 'Revision of the project to inspect (either a git branch, tag or commit SHA number)')
    String revision

    private List<String> pipelineArgs = null

    private Map<String,String> pipelineParams = null

    /**
     * Get the list of pipeline args.
     */
    @Override
    List<String> getArgs() {
        if( pipelineArgs == null ) {
            pipelineArgs = ParamsHelper.parseArgs(args)
            pipelineParams = ParamsHelper.parseParams(args, pipelineArgs)
        }

        return pipelineArgs
    }

    /**
     * Get the map of pipeline params.
     */
    @Override
    Map<String,String> getParams() {
        if( pipelineParams == null ) {
            pipelineArgs = ParamsHelper.parseArgs(args)
            pipelineParams = ParamsHelper.parseParams(args, pipelineArgs)
        }

        return pipelineParams
    }

    @Override
    String getLauncherCli() {
        launcher.cliString
    }

    @Override
    CliOptions getLauncherOptions() {
        launcher.options
    }

    @Override
    void run() {
        new CmdInspect(this).run()
    }

}
