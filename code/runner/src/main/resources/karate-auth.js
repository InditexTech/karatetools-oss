// karate-auth functions
function fn(requestAuth, failOnAuthError = true) {

  // validate auth
  var validateAuth = function(requestAuth) {
    // get auth from requestAuth
    var auth = karate.merge(requestAuth || {});

    // get authMode from requestAuth or default config
    auth.authMode = auth.authMode || karate.get('defaultAuthMode');
    // valid authModes: basic, jwt
    if (auth.authMode == null || ! ['basic', 'jwt'].includes(auth.authMode)) {
      return error('>> auth >> Trying to authenticate with no authMode or invalid authMode. auth.authMode:' + auth.authMode);
    }

    // get username requestAuth or default config
    if (['basic', 'jwt'].includes(auth.authMode)) {
      auth.username = auth.username || karate.get('defaultUsername');
      if (auth.username == null || auth.username === 'none' || auth.username === '') {
        return error('>> auth >> Trying to authenticate with no username. auth.authMode:' + auth.authMode + ' auth.username:' + auth.username);
      }
    }

    // get password from requestAuth or default config
    if (['basic'].includes(auth.authMode)) {
      auth.password = auth.password || karate.get('credentials', {})[auth.username];
      if (auth.password == null || auth.password === 'none' || auth.password === '') {
        return error('>> auth >> Trying to authenticate with no password. auth.authMode:' + auth.authMode + ' auth.username:' + auth.username + ' auth.password:' + auth.password);
      }
    }

    return auth;
  }

  // returns basic auth header
  var basicAuthorization = function(username, password) {
    var basicAuthHeadersCache = karate.properties['basicAuthHeadersCache'] || {};
    var authHeader = basicAuthHeadersCache[username];
    // if authHeader not present in cache, then generate and store in cache
    if (!authHeader) {
      var temp = username + ':' + password;
      var Base64 = Java.type('java.util.Base64');
      var JavaString = Java.type('java.lang.String');
      var encoded = Base64.getEncoder().encodeToString(new JavaString(temp).getBytes('utf-8'));
      basicAuthHeadersCache[username] = encoded;
    }
    else {
      karate.logger.debug('>> auth >> basic >> authHeader present in cache');
    }
    karate.properties['basicAuthHeadersCache'] = basicAuthHeadersCache;
    return { Authorization: 'Basic ' + basicAuthHeadersCache[username] };
  };

  // basic auth wrapper
  var basicAuth = function(auth) {
    return basicAuthorization(auth.username, auth.password)
  };

  // returns jwt auth header
  var jwtAuth = function(auth) {
    var jwtAuthHeadersCache = karate.properties['jwtAuthHeadersCache'] || {};
    var authHeader = jwtAuthHeadersCache[auth.username];
    if (!authHeader) {
      var readFile = function(filename) {
        try {
          return karate.read(filename);
        } catch (e) {
          return null;
        }
      }
      var jwt;
      try {
        var defaultJwt = readFile('classpath:jwt/default-jwt.yml');
        var userJwt = readFile('classpath:jwt/' + auth.username + '-jwt.yml');
        var jwtData;
        if (userJwt == null) {
          defaultJwt.payloads.sub = auth.username
          jwtData = {
            secret: defaultJwt.secret,
            headers: defaultJwt.headers,
            payloads: defaultJwt.payloads
          }
        } else {
          jwtData = {
            secret: defaultJwt.secret,
            headers: karate.merge(defaultJwt.headers, userJwt.headers || {}),
            payloads: karate.merge(defaultJwt.payloads, userJwt.payloads || {})
          }
        }
        karate.logger.debug('>> auth >> jwt >> jwtData >>', jwtData);
        jwt = Java.type('dev.inditex.karate.jwt.JWTGenerator').generateToken(jwtData);
      } catch (e) {
        return error('>> auth >> jwt >> failed to generate jwt >> exception >> ' + e.message);
      }
      if (jwt == null) {
        return error('>> auth >> jwt >> failed to generate jwt');
      }
      jwtAuthHeadersCache[auth.username] = jwt;
    }
    else {
      karate.logger.debug('>> auth >> jwt >> authHeader present in cache');
    }
    karate.properties['jwtAuthHeadersCache'] = jwtAuthHeadersCache;
    return { Authorization: 'Bearer ' + jwtAuthHeadersCache[auth.username] };
  };

  var error = function(message) {
    if (failOnAuthError) {
      karate.fail(message);
    } else {
      karate.logger.error(message);
      return null;
    }
  }

  // authModes  (basic, jwt)
  var authModes = {
    basic: basicAuth,
    jwt: jwtAuth
  };

  // validate auth
  var auth = validateAuth(requestAuth);

  // calls auth function and return auth header
  return auth ? authModes[auth.authMode](auth) : null;
}
