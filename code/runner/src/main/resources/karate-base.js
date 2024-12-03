function fn() {
  function read(file) {
    try {
      return karate.read(file);
    } catch (e) {
      karate.logger.debug(">> karate.read() >> File not found: " + file + " >> Exception: " + e);
      return {};
    }
  }

  karate.log('>> karate.tools >> Running Feature[', karate.feature.name, '] Scenario [', karate.scenario.name, '] ...')

  // Config - Common
  karate.set(read('classpath:config.yml'));
  // Config - Environment
  karate.set(read(`classpath:config-${karate.env}.yml`));
  // Config - Environment Secrets
  karate.set(read(`classpath:config-${karate.env}-secrets.yml`));

  // Reporting Flags
  let showLogOption = false;
  let showAllStepsOption = true;
  if (karate.properties['karate.report.options']) {
    if (karate.properties['karate.report.options'].includes('--showLog true')) { showLogOption = true }
    if (karate.properties['karate.report.options'].includes('--showLog false')) { showLogOption = false }
    if (karate.properties['karate.report.options'].includes('--showAllStepsOption true')) { showAllStepsOption = true }
    if (karate.properties['karate.report.options'].includes('--showAllStepsOption false')) { showAllStepsOption = false }
  }
  karate.logger.debug('>> karate.tools >> Report Verbosity >> showLog[', showLogOption, '] showAllSteps[', showAllStepsOption, ']');

  // Karate - Report Verbosity
  // showLog:boolean, default true
  //    HTTP requests and responses (including headers) will appear in the HTML report
  // showAllSteps:boolean, default true
  //    If false, any step that starts with * instead of Given, When, Then etc. will not appear in the HTML report.
  //    The print step is an exception
  karate.configure('report', { showLog: showLogOption, showAllSteps: showAllStepsOption });

  let mocks;
  // Karate - Local Mock Server
  if (karate.env === 'local') {
    karate.logger.debug('>> karate.tools >> Mock Server Configuration');
    // Mocks Before Everything Hook for the entire test suite.
    // Mocks Not to be executed unless is 'local' env and 'mock.templates.standalone' property set to true
    // Default set to standAloneMocks
    let standAloneMocks = true;
    if(karate.properties['karate.options']) {
      standAloneMocks = !karate.properties['karate.options'].includes('~@mock.templates.standalone')
      karate.logger.debug(">> karate.tools >> karate.options['@mock.templates.standalone'] >> " + standAloneMocks);
    }
    if (standAloneMocks) {
      karate.logger.debug('>> karate.tools >> karate.callSingle("classpath:mocks/mock-templates-standalone.feature")');
      mocks = karate.callSingle("classpath:mocks/mock-templates-standalone.feature");
      karate.logger.debug('>> karate.tools >> karate.callSingle("classpath:mocks/mock-templates-standalone.feature") DONE');
    }
  }

  return {
    cache: Java.type('dev.inditex.karate.test.KarateCache'),
    utils: karate.call('classpath:karate-utils.js'),
    mocks
  }
}
