// ............................................................................
// IMPORTS
// ............................................................................
const core = require('@actions/core');
const stringUtils = require('./stringUtils')
/**
 * 
 * @param {*} name Name of the input
 * @param {*} options 
 * @returns 
 */
function getInputAsArray(name, options = core.InputOptions){
    const inputValue = 
    (stringUtils.isNullOrEmptyOrWhiteSpace)?core.getInput(name):core.getInput(name, options);
    return stringUtils.getMultilineStringAsArray(inputValue)
}

module.exports = { getInputAsArray };