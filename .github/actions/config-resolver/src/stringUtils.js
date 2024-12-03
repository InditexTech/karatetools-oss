function isEmptyOrWhiteSpace(str){
    return str === null || str === undefined || str.match(/^ *$/) !== null;
}

function isNullOrEmptyOrWhiteSpace(str){
    return str === null || str === undefined || str.match(/^ *$/) !== null;
}

/**
 * Returns str1 or str2 based on logic
 * @param {*} str1 
 * @param {*} str2 
 * @returns str2 if str1 is null, empty or whitespace OR equals to 'DEFAULT'.
 */
function oneOrAnother(str1, str2, debug=false){
    if (debug){
        console.log(`[DEBUG] oneOrAnother for str1(${str1}) and str2(${str2})`);
    }
    return (isNullOrEmptyOrWhiteSpace(str1) || str1 == 'DEFAULT')?str2:str1;
}

function getMultilineStringAsArray(str){
    return str 
            .split("\n")
            .map(s => s.trim())
            .filter(x => x !== "");
}
module.exports = { 
    isEmptyOrWhiteSpace, 
    isNullOrEmptyOrWhiteSpace, 
    oneOrAnother,
    getMultilineStringAsArray };