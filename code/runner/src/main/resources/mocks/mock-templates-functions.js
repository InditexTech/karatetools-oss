function fn() {
  /**
   * List the templates in the given folder
   *
   * @param  {} folder
   */
  function listTemplates(folder) {
    karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >>', folder);
    function traverse(file, base) {
      karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >> traverse >>', file);
      let list = new java.util.ArrayList();
      const files = file.listFiles();
      if (!files) {
        karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >> traverse >> files >> EMPTY');
        return list;
      }
      karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >> traverse >> files', files.lenght);
      for (const file of files) {
        const f = file;
        if (f.directory) {
          list.addAll(traverse(f, base));
        } else if(file.getName().endsWith(".yml")) {
          const relative = base.toURI().relativize(f.toURI()).getPath();
          karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >> file >>', relative);
          list.add("classpath:" + relative);
        } else {
          karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >> file >>', file, 'SKIPPING');
        }
      }
      if(list.size() > 0) {
        java.util.Collections.sort(list);
      }
      karate.log('>> karate.tools >> mock-templates >> listTemplates >> # files >>', list.size());
      return list;
    }
    try {
      const url = java.lang.Thread.currentThread().getContextClassLoader().getResource(folder);
      const root = new java.io.File(url.getPath());
      const urlBase = java.lang.Thread.currentThread().getContextClassLoader().getResource(".");
      const base = new java.io.File(urlBase.getPath());
      return traverse(root, base);
    } catch (e) {
      karate.log('>> karate.tools >> mock-templates >> listTemplates >> ERROR >>', e.message);
      return new java.util.ArrayList();
    }
  }
  /**
   * Read the templates in the given list
   *
   * @param  {} templatesList
   */
  function readTemplates(templatesList) {
    karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates >> templatesList >>', templatesList);
    let templates = new java.util.ArrayList();
    for (const templateFile of templatesList) {
      karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates >> templateFile >>', templateFile);
      let template = karate.read(templateFile);
      if (template.params) {
        karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates >> template.params IF >> ', template.params);
        template.params = parseParams(template.params);
      }
      template.file = templateFile;
      karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates >>', template);
      templates.add(template);
    }
    karate.log('>> karate.tools >> mock-templates >> readTemplates >> # templates >>', templates.size());
    return templates;
  }
  /**
   * Parse template parameters to align with karate mocks format (array)
   * Accept karate marker matchers (#?...) as provided
   * Accept karate marker #string. Since karate params are arrays accept anything (##object)
   * Parse the rest converting p1=...&p2=...&pN=... to { "p1": [...], "p2": [...], ..., "pN": [...] }
   *
   * @param  {} paramsString
   */
  function parseParams(paramsString) {
    karate.logger.debug(">> karate.tools >> mock-templates >> parseParams  >>", paramsString);
    let templateParams = {};
    if (paramsString) {
      if (paramsString.startsWith('#?')) {
        // Accept karate marker matchers (#?...) as provided
        templateParams = paramsString;
      }
      else if (paramsString == '#string') {
        // Accept karate marker #string. Since karate params are arrays accept anything (##object)
        templateParams = '##object';
      }
      else {
        const params = paramsString.split('&');
        karate.logger.debug(">> karate.tools >> mock-templates >> parseParams  >>", params);
        for (const param of params) {
          const indexOfEqual = param.indexOf('=');
          const key = param.substring(0, indexOfEqual);
          const value = param.substring(indexOfEqual + 1);
          if (!templateParams[key]) templateParams[key] = [];
          const templateParamsIndex = templateParams[key].length;
          templateParams[key][templateParamsIndex] = value;
        }
      }
    }
    return templateParams;
  }
  /**
   * Find a Match in the templates list for the provided parameters (requestMethod, requestUri, requestParams, request)
   *
   * @param  {} templates
   * @param  {} requestMethod
   * @param  {} requestUri
   * @param  {} requestParams
   * @param  {} request
   */
  function findTemplate(templates, requestMethod, requestUri, requestParams, request) {
    karate.logger.debug(">> karate.tools >> mock-templates >> findTemplate >> \n    >> method      >>", requestMethod, "\n    >> path        >>", requestUri, "\n    >> params      >>", requestParams, "\n    >> request     >>", request);

    let match = { "responseStatus": 404, "responseHeaders": { "Content-Type": "application/json" }, "response": { "mock": "404" } };
    let matchFound = false;
    for (let i = 0; i < templates.length; i++) {
      let template = templates[i];
      karate.logger.debug(">> karate.tools >> mock-templates >> findTemplate >> CHECKING [", i, "][", template.file, "]");

      if (!pathMatch(requestUri, template.path)) continue;
      if (!methodMatch(requestMethod, template.method)) continue;
      if (requestParams && template.params) {
        const paramsMatch = karate.match(requestParams, template.params);
        if (!paramsMatch.pass) continue;
      } else {
        karate.logger.debug(">> karate.tools >> mock-templates >> findTemplate >> params      EMPTY[", i, "][", template.file, "] >>", requestParams, ">> template.params  >>", template.params);
      }

      if (request && template.request) {
        const requestMatch = karate.match(request, template.request);
        if (!requestMatch.pass) continue;
      } else {
        karate.logger.debug(">> karate.tools >> mock-templates >> findTemplate >> request     EMPTY[", i, "][", template.file, "] >>", request, ">> template.request  >>", template.request);
      }
      karate.log(">> karate.tools >> mock-templates >> findTemplate >> FOUND !!![", i, "][", template.file, "]");

      match.responseStatus = template.responseStatus;
      match.responseHeaders = template.responseHeaders;
      match.response = template.response;
      matchFound = true;
      break;
    }
    if (!matchFound) karate.logger.error(">> karate.tools >> mock-templates >> findTemplate >> NOT FOUND !!! >> \n    >> method      >>", requestMethod, "\n    >> path        >>", requestUri, "\n    >> params      >>", requestParams, "\n    >> request     >>", request);

    return match;
  }
  
  /**
   * Check if the path matches the template path
   *
   * @param  {} path
   * @param  {} templatePath
   */
  function pathMatch(path, templatePath) {
    if (path === templatePath) return true;
    var pathParts = com.intuit.karate.http.HttpUtils.parseUriPattern(templatePath, path);
    karate.logger.debug(">> karate.tools >> mock-templates >> pathMatch >> \n    >> path        >>", path, "\n    >> templatePath>>", templatePath, "\n    >> pathParts   >>", pathParts);
    return pathParts !== null;
  }
  
  /**
   * Check if the method matches the template method
   *
   * @param  {} method
   * @param  {} templateMethod
   */
  function methodMatch(method, templateMethod) {
    return method.toUpperCase() === templateMethod.toUpperCase();
  }
    
  return {
    listTemplates, readTemplates, parseParams, findTemplate, pathMatch, methodMatch
  }
}