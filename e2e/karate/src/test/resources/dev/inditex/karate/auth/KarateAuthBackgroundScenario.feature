@ignore
Feature: karate-auth-background-scenario

Background:

Scenario: karate-auth-background-scenario

# Functions
# Log Watcher
* def configureLogWatcher =
"""
function() {
  var ListAppender = Java.type('ch.qos.logback.core.read.ListAppender');
  var LoggerFactory = Java.type('org.slf4j.LoggerFactory');
  var Level = Java.type('ch.qos.logback.classic.Level');

  var logWatcher = new ListAppender();
  logWatcher.setName('logWatcher');
  logWatcher.start();

  var logger = LoggerFactory.getLogger("com.intuit.karate");
  logger.addAppender(logWatcher);
  logger.setLevel(Level.DEBUG);
  return {
    logger: logger,
    logWatcher: logWatcher
  }
}
"""

* def detachLogWatcher =
"""
function() {
  logger.detachAppender('logWatcher')
}
"""

# Get Last N Logs from Log Watcher
* def getLastLogs =
"""
function(count) {
  var logs = [];
  for(var i = count; i >= 1; i--) {
    logs.push(logWatcher.list.get(logWatcher.list.size() - i).getFormattedMessage().trim());
  }
  return logs;
}
"""

# Remove all auth default configurations and clean cache so we can test config error management
* def configureAuth =
"""
function() {
  karate.set('defaultAuthMode', null);
  karate.set('defaultUsername', null);
  karate.set('credentials', null);

  karate.properties['basicAuthHeadersCache'] = {}
  karate.properties['jwtAuthHeadersCache'] = {}
}
"""

# Restore default configurations after each scenario
* def afterScenarioAuth =
"""
function() {
  karate.log('afterScenarioAuth');
  karate.set('defaultAuthMode', configAuthMode);
  karate.set('defaultUsername', configUsername);
  karate.set('credentials', configCredentials);
}
"""

# Get the default configurations
* def configAuthMode = karate.get('defaultAuthMode');
* def configUsername = karate.get('defaultUsername');
* def configCredentials = karate.get('credentials');

# Remove all auth default configurations so we can test config error management
* configureAuth()

# Restore default configurations after each scenario
* configure afterScenario = afterScenarioAuth

* def logging = configureLogWatcher()
* def logger = logging.logger
* def logWatcher = logging.logWatcher
