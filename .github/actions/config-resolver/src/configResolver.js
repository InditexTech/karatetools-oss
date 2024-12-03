// ............................................................................
// IMPORTS
// ............................................................................
const dot = require('dot-object');
const yaml = require('js-yaml');
const fs = require('fs');
const merge = require('lodash.merge');
const cloneDeep = require('lodash.clonedeep');

// ............................................................................
// FUNCTIONS
// ............................................................................
/**
 * 
 * @param {Array<string>} files Array of file paths
 * @param {*} vars JSON Object with Key-Value pair
 * @param {boolean} indentationChar The indentation char
 * @param {boolean} debug Show debug information
 * @returns {config: JSONObject, inputs: JSONObject} returns configuration properties files into config
 *  and inputs with overriden (or not) values in inputs property.
 */
function resolve(files, vars, indentationChar = '_', searchString='DEFAULT', failOnNotFound=false, verbose=false){
    let config = {};
    let inputs = {};

    if(verbose) {
      console.log(`[DEBUG] resolve() arg 'files' with value '${files}'`);
      console.log(`[DEBUG] resolve() arg 'vars' with value '${JSON.stringify(vars)}'`);
      console.log(`[DEBUG] resolve() arg 'searchString' with value '${searchString}'`);
      console.log(`[DEBUG] resolve() arg 'indentationChar' with value '${indentationChar}'`);
    }
    const replacer = new RegExp(indentationChar, 'g'); // this is required because node12 
                                                     // doesn't support string.replaceAll
    // Checks
    if (!Array.isArray(files)) {
        throw new Error("Parameter files it's not an Array<string>");
    }
    if (verbose) console.log(`[DEBUG] Process working directory ${process.cwd()}`);
    // Process files
    files.forEach( file => {
        if (verbose) console.log(`[DEBUG] Processing file ${file}`);
        try {
          if (!fs.existsSync(file)) throw new Error(`Specified file doesn't exists (${file}).`);
          let fileContent = fs.readFileSync(`${file}`, 'utf8');
          if (verbose) console.log(`[DEBUG] File content successfully readed.`);
          let fileAsJSON = yaml.load(fileContent);
          if (verbose) console.log(`[DEBUG] File content processed successfully: ${JSON.stringify(fileAsJSON)}`);
          config = merge(config, fileAsJSON);
          if (verbose) console.log(`[DEBUG] Config variable after assignment: ${JSON.stringify(config)}`);
        } catch (error) {
          if (failOnNotFound) {
            console.error(error.message);
            throw(error);
          }
          else 
          {
            console.log(`[ERROR] Error processing file (${file}) but failOnNotFound is set to false. Ignoring file and continue.`);
          }
        }
    });

    if (verbose) {
      console.log(`[DEBUG] All configs before replacements: ${JSON.stringify(config)}`);
      console.log(`[DEBUG] All inputs before replacements: ${JSON.stringify(vars)}`);
    }
    // Replacements
    //
    // Configs output object: It first, replaces _ by . in inputVars
    // and then use dot-object library to transforms them to 
    // dot notation and finally as JSONObject
    // NOTE: We need to make a copy of vars, since it's a JSON object
    // which can be modifier altering the original variable from the caller
 
    let varsClone = cloneDeep(vars);
    for (var varName in varsClone) {
      var originalName = varName; // save the name !
      var inputValue = varsClone[originalName]; // save the original value

      // transform the variable
      // it convert var name to dotted notation in object varsClone
      var dotName = varName.replace(replacer, ".").toLowerCase();
      dot.move(varName, dotName, varsClone); 
      
      // if DEFAULT replace input var by properties
      //    if no property exists, FAIL
      // if not DEFAULT leave the current value as-is
      if (inputValue == searchString) {
        // search for value in config properties from files
        var valueInConfig = dot.pick(dotName, config);
        // if not found, fail
        if (valueInConfig === null || valueInConfig === undefined) {
          throw new Error(
            `Input variable ${originalName} is set to ${searchString} but doesn't have a 
            default value in properties retrieved from files.`);
        }
        else {
          // if found, replace input in varsClone
          dot.set(dotName, valueInConfig, varsClone, true);
        }
      }
      // In any case, the input needs to be set
      inputs[originalName] = dot.pick(dotName, varsClone);
    }
    // Now, it's just to merge them :)
    config = merge(config, varsClone);
    // Inputs output object,
    // Once we have all properties files
    if (verbose) console.debug(`[DEBUG] resolved config: ${JSON.stringify(config)}`);
    if (verbose) console.debug(`[DEBUG] resolved inputs: ${JSON.stringify(inputs)}`);
    // Return
    //
    return { config, inputs };
}

module.exports = { resolve };