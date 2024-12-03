# Configuration Resolver

Resolves configuration properties from a bunch of different sources (files, environment variables, action inputs, etc.) using a hierarchical property precedence and merge.

## An Use Case
Image you have a configuration file (yaml) with:

```yaml
superproperty: super value
```

and you want to be able to "override" this property, from an environment variable, or better, from your workflow input. Then, you can use it as:

```yaml
#... your workflow inputs ...
on:
  workflow_dispatch:
    inputs:
      SUPERPROPERTY:
        description: 'This is your super property'
        required: true
        default: 'DEFAULT' # It means, get the value if found on a file
#... your workflow code ...
- name: Resolve Configuration
  id: config
  uses: ./.github/actions/resolvers/config-resolver
  with:
    files: |
      your_properties_file.yml
    input-vars: "${{ toJSON(github.event.inputs) }}" # it injects all workflows inputs
#... more workflow code ...
```

Then, if you leaves the `SUPERPROPERTY`input to "DEFAULT" when running your workflow, the value of `steps.config.outputs.config.superproperty` will be the value on the file as `super value`. But, if you provided `SUPERPROPERTY` input value to "Override value" then, `steps.config.outputs.config.superproperty` value will be "Override value".

You will found also, an specific high-level output for input properties on `steps.config.outputs.inputs`, so in the latest example, you can get the value from `steps.config.outputs.inputs.SUPERPROPERTY`.

## About Merging precedence

The "merging" precedence it's on the **order** in which you provide the input files to the action. It means, if you have two files with same property defined, the latest provided file will set the property value.

```yaml
# file one.yml
myproperty: value one
# file two.yml
myproperty: value two
# Action call
- name: Resolve Configuration
  id: config
  uses: ./.github/actions/resolvers/config-resolver
  with:
    files: |
      one.yml
      two.yml
# value of myproperty on steps.config.outputs.config.myproperty will be "value two"
```

## [Inputs](#inputs)

- [files](#files)
- [search-string](#search-string)
- [input-vars](#input-vars)
- [indentation-char](#indentation-char)
- [debug](#debug)
- [create-annotations](#create-annotations)

## [Outputs](#outputs)

- [inputs](#inputs)
- [config](#config)

## [Examples usage](#examples)

<h2 style="font-weight: bold" id="inputs">Inputs</h2>

<h3 style="font-weight: bold" id="search-string">search-string</h3>

**Required** Default to "DEFAULT" (literal).
If an input var contains this literal, it means that a value will be searched in configuration files and get that value. **Warning**: if set as literal but value is not found in configuration files, the action **will fail**.
If an input value doesn't contains this literal, then, the value is returned as-is.

<h3 style="font-weight: bold" id="files">files</h3>

**Required** List of files (one on each line) to load into the output JSON object.
It has to be specified as long-string `: |` crlf separated.

<h3 style="font-weight: bold" id="input-vars">input-vars</h3>

**OPTIONAL** List of variables in format KEY=VALUE. Key can contains indentation-char.
**IF** variable contains `input-trigger-string` it means that a value will be searched in configuration files and get that value. **Warning**: if set as literal but value is not found in configuration files, the action **will fail**.
If an input value doesn't contains this `input-trigger-string`, then, the value is returned as-is.

<h3 style="font-weight: bold" id="indentation-char">indentation-char</h3>

**OPTIONAL** Indentation token in input-vars variable names. Default value is "_"

<h3 style="font-weight: bold" id="create-annotations">create-annotations</h3>

**OPTIONAL** Specify if action should create annotations into the run. Default value is `false`. Since theres a limit on the amount of annotations per run, and since as much annotations the hardest to search them into the Run screen, use it carefuly.
Current annotations are:
* Config-Resolver:: Input-Vars (inputs.input-vars)
* Config-Resolver:: Resolved Inputs (outputs.inputs)
* Config-Resolver:: Resolved Configs (outputs.config)

<h3 style="font-weight: bold" id="debug">debug</h3>

**OPTIONAL** Specify debug mode. It adds more output. Take care, it can publish sensitive invormation if it's not properly cypher in your env variables. Default value is `false`.

<h2 style="font-weight: bold" id="outputs">Outputs</h2>

<h3 style="font-weight: bold" id="inputs">inputs</h3>

Each variable with same name as each `input-vars` but with resolved value.

<h3 style="font-weight: bold" id="config">config</h3>

A JSON object that contains all the processed files as JSON objects with replaced properties from input-vars

<h2 style="font-weight: bold" id="examples">Examples usage</h2>

```yaml
# Having jacoco.yml as
# jacoco:
#   enabled: true
#   version: 8
# input-vars
#    JACOCO_VERIONS=9
- name: Get configurations
  id: test-config
  uses: ./.github/actions/config-resolver
  with:
    input-vars: "${{ toJSON(github.event.inputs) }}"
    indentation-char: "_"
    files: |
      code/config_test/e2e/test_config.yml
      code/config_test/e2e/jacoco.yml
      code/platform.yml
      code/application.yml
  
- name: Using a configuration
  if: ${{ fromJSON(steps.test-config.outputs.config.jacoco).enabled }} # it will get true
  run: |
    # it will get 9 instead of 8
    <some_command> ${{ fromJSON(steps.test-config.outputs.config.jacoco).version }}
```

## Developer

### Build

```shell
npm run prepare
```
it will update everything under dist folder where the "real" action will be.

### Publish

After `prepare` makes sure that you commit `dist` folder changes as part of your commits. Then, just push to the repo.