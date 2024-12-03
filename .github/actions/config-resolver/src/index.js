// ............................................................................
// IMPORTS
// ............................................................................
const core = require('@actions/core');
const actionUtils = require('./actionUtils');
const configResolver = require('./configResolver');

// ............................................................................
// METHODS
// ............................................................................
/**
 * most @actions toolkit packages have async methods
 */
async function run() {
  try {
    // Input Parameters processing
    //
    const inputVars = JSON.parse(core.getInput('input-vars', {required: false}) || '""');
    const indentationChar = core.getInput('indentation-char') || '_';
    const searchString = core.getInput('search-string') || 'DEFAULT';
    const files = actionUtils.getInputAsArray('files');
    const createAnnotations = core.getInput('create-annotations') || false;
    const debug = core.getInput('debug') || false;
    // Input parameters debug
    //
    if (debug) {
      console.log(`[DEBUG] action() arg input-vars: ${JSON.stringify(inputVars,null,2)}`);
      console.log(`[DEBUG] action() arg indentation-char: ${indentationChar}`);
      console.log(`[DEBUG] action() arg search-string: ${searchString}`);
      console.log(`[DEBUG] action() arg files: ${files}`);
      console.log(`[DEBUG] action() arg create-annotations: ${createAnnotations}`);
    }

    // Resolve properties
    //
    var properties = configResolver.resolve(files, inputVars, indentationChar, searchString, false, debug);

    // Persistent Log / Notices / Annotation
    if (createAnnotations) {
      core.notice(JSON.stringify(inputVars,null,2), { "title": "Config-Resolver:: Input-Vars (inputs.input-vars)"});
      core.notice(JSON.stringify(properties.inputs,null,2), { "title": "Config-Resolver:: Resolved Inputs (outputs.inputs)"});
      core.notice(JSON.stringify(properties.config,null,2), { "title": "Config-Resolver:: Resolved Configs (outputs.config)"});
    }

    // Outputs
    //
    core.setOutput("config", JSON.stringify(properties.config));
    core.setOutput("inputs", JSON.stringify(properties.inputs));
  } catch (error) {
    core.setFailed(error.message);
  }
}
// ............................................................................
// Action Entrypoint
// ............................................................................
run();  // Just invoke our function

module.exports = { run };