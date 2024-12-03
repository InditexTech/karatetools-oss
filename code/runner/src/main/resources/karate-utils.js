function fn() {
  const utils = {};
  /**
   * Reads a testFile and meges json contents with base files.
   * @param {string} file
   */
  utils.readTestData = (file) => {
    var folder = file.substring(0, file.lastIndexOf('/') + 1);
    var baseName = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'))
    var extension = file.substring(file.lastIndexOf('.'), file.length);
    var tokens = baseName.split('_');
    // karate.logger.debug('folder', folder, 'baseName', baseName, 'extension', extension, 'tokens', tokens);
    var files = [file];
    while (tokens.pop() && tokens.length > 0) {
      files.push(folder + tokens.join('_') + extension);
    }
    files.push(folder + 'baseRequest' + extension);
    files = files.reverse();
    // karate.logger.debug('files', files);

    var testData = utils.merge(
      ...files.map(function(file) {
        try {
          return karate.read(file);
        } catch (e) {
          return null;
        }
      })
    );

    return testData;
  };

  utils.isArray = (array) => {
    // karate.logger.debug('isArray', ({}).toString.call(array));
    return Array.isArray(array) || ({}).toString.call(array).indexOf('Array') >= 0;
  }

  /**
   *
   * @param {*} objects
   */
  utils.merge = (...objects) => {
    // karate.logger.debug('merge', objects);
    if (objects == null) {
      return null;
    }
    if (karate.sizeOf(objects) <= 1) {
      return objects[0] || objects || {};
    }
    if (!utils.isArray(objects)) {
      objects = new Array(objects);
    }

    objects = objects.filter(function(obj) {
      return obj != null;
    });
    // var target = objects.shift();
    var target = {};
    (objects || []).forEach(function(source) {
      utils._deepMerge(target, source);
    });

    return target;
  };

  utils._deepMerge = (target, source) => {
    if (typeof source === 'object') {
      karate.keysOf(source).forEach(function(key) {
        if (utils.isArray(source[key]) || utils.isArray(target[key])) {
          target[key] = target[key] || [];
          if (target[key].concat) {
            target[key] = target[key].concat(source[key]);
          } else {
            target[key].addAll(source[key]);
          }
        }
        else if (target[key] && source[key] !== null && typeof source[key] === 'object') {
          utils._deepMerge(target[key], source[key]);
        } else {
          target[key] = source[key];
        }
      });
    }
  }

  utils.replaceExpressions = (source) => {
    // ex: replaces values in source of the form #(storeId) with values.storeId
    var callback = function(leafValue) {
      if (typeof leafValue === 'string') {
        var replaced = leafValue;
        const EXPRESSION_REGEX = /#\(([\w\.]+)\)/gm;
        while ((m = EXPRESSION_REGEX.exec(leafValue)) !== null) {
          replaced = replaced.replace(m[0], karate.get(m[1], '#(' + m[1] + ')'));
        }
        return replaced;
      }
      return leafValue;
    }
    return utils.manipulateLeafValues(source, callback);
  }

  utils.manipulateLeafValues = (source, callback) => {
    if (utils.isArray(source)) {
      source.forEach(function(item, index) {
        source[index] = utils.manipulateLeafValues(item, callback);
      });
    }
    else if (typeof source === 'object') {
      (karate.keysOf(source) || []).forEach(function(key) {
        if (source[key] !== null && typeof source[key] === 'object') {
          utils.manipulateLeafValues(source[key], callback);
        } else {
          source[key] = callback(source[key]);
        }
      });
    } else {
      source = callback(source);
    }
    return source;
  }

  return utils;
}
