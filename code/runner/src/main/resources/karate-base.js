function fn() {
  function read(file) {
    try {
      return karate.read(file);
    } catch (e) {
      karate.logger.debug(">> karate.read() >> File not found: " + file + " >> Exception: " + e);
      return {};
    }
  }
  var config = {};
  // Initialize Cache
  config.cache = Java.type('dev.inditex.karate.test.KarateCache');
  // Initialize Utils
  config.utils = karate.read('classpath:karate-utils.js');

  karate.log('>> karate.tools >> Running Feature[', karate.feature.name, '] Scenario [', karate.scenario.name, '] ... ')

  // Config - Common
  karate.set(read('classpath:config.yml'));
  // Config - Environment
  karate.set(read(`classpath:config-${karate.env}.yml`));
  // Config - Environment Secrets
  karate.set(read(`classpath:config-${karate.env}-secrets.yml`));

  // Reporting Flags
  var showLogOption = false;
  var showAllStepsOption = true;
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
  // Karate - Logging
  karate.configure('logging', {
    report:  'debug',                           // what gets captured into reports
    console: 'debug',                           // what hits stdout via SLF4J/Logback
    pretty:  true                               // pretty-print HTTP JSON bodies
  });
  // Log Masking
  var loggingMaskConfig = {
    mask: {
      headers: [
        'authorization', 'token', 'secret', 'key', 'username', 'password',
        'x-authorization', 'x-token', 'x-secret', 'x-key', 'x-username', 'x-password',
        'x-openam-username', 'x-openam-password'],
      patterns: [
        {
          regex: 'Bearer [A-Za-z0-9._\\-]+\\.[A-Za-z0-9._\\-]+\\.[A-Za-z0-9._\\-]*',
          replacement: 'Bearer *****'
        },
        {
          regex: 'Basic [A-Za-z0-9+/]+=*',
          replacement: 'Basic *****'
        }
      ],
      replacement: '*****'
    }
  };
  karate.configure('logging', loggingMaskConfig);
  config.loggingMaskConfig = loggingMaskConfig;

  var mocks;
  // Karate - Local Mock Server
  if (karate.env === 'local') {
    karate.logger.debug('>> karate.tools >> Mock Server Configuration');
    // Mocks Before Everything Hook for the entire test suite.
    // Mocks Not to be executed unless is 'local' env and 'mock.templates.standalone' property set to true
    // Default set to standAloneMocks
    var standAloneMocks = true;
    if (karate.properties['karate.options']) {
      standAloneMocks = !karate.properties['karate.options'].includes('~@mock.templates.standalone')
      karate.logger.debug(">> karate.tools >> karate.options['@mock.templates.standalone'] >> " + standAloneMocks);
    }
    if (standAloneMocks) {
      karate.logger.debug('>> karate.tools >> karate.callSingle("classpath:mocks/mock-templates-standalone.feature")');
      mocks = karate.callSingle("classpath:mocks/mock-templates-standalone.feature");
      karate.logger.debug('>> karate.tools >> karate.callSingle("classpath:mocks/mock-templates-standalone.feature") DONE');
    }
  }
  config.mocks = mocks;

  // Karate 1.4.1 includes a breaking change in the behavior of "match each" when the array is empty.
  // The new behavior is: match each defaults to fail if array is empty
  // This may break existing test scripts only if you are extensively using the fuzzy matching short-cut #[] in enbedded schema validations.
  // The good news is that if you see any of your existing tests break, you can do this to get back the old behavior.
  // * configure matchEachEmptyAllowed = true
  karate.configure('matchEachEmptyAllowed', true);

  // Utility function to get the parent folder of the current feature file.
  // When absolute=true (default):
  //    Strips scheme prefix only, returns raw CWD-relative path (e.g. target/test-classes/scenarios/auth). 
  //    Used for file system paths (cert/key parameters).
  // When absolute=false:
  //    Classloader-normalized, returns classpath-relative path (e.g. scenarios/auth).
  //    Used with classpath: prefix for resource loading (mock parameter).
  config.getParentFolder = function (path, absolute) {
    if (absolute === undefined) absolute = true;
    karate.logger.debug('>> karate.tools >> getParentFolder >> path >> ' + path + ' >> absolute >> ' + absolute);
    var rawPath = path.substring(path.indexOf(':') + 1);
    var Paths = Java.type('java.nio.file.Paths');
    if (!absolute) {
      var Thread = Java.type('java.lang.Thread');
      try {
        var cl = Thread.currentThread().getContextClassLoader();
        var rootUrl = cl.getResource('');
        if (rootUrl != null) {
          var rootPath = Paths.get(rootUrl.toURI()).toAbsolutePath().normalize();
          var absPath = Paths.get(rawPath).toAbsolutePath().normalize();
          if (absPath.startsWith(rootPath)) {
            rawPath = rootPath.relativize(absPath).toString();
          }
        }
      } catch (e) { }
    }
    var parent = Paths.get(rawPath).getParent().toString();
    var normalizedParent = parent.replace(/\\/g, '/');
    karate.logger.debug('>> karate.tools >> getParentFolder >> ' + normalizedParent);
    return normalizedParent;
  };

  return config;
}
