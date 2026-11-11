const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { test } = require('node:test')
const { load } = require('js-yaml')
const registerRedirects = require('@inditextech/docouture-antora-extensions/lib/redirects')
const registerDuplicateLatestVersion = require('@inditextech/docouture-antora-extensions/lib/duplicate-latest-version')

function createContext() {
  const handlers = new Map()
  return {
    handlers,
    on(event, handler) {
      handlers.set(event, handler)
    },
    getLogger() {
      return {
        info() {},
        warn() {},
        error(message) {
          throw new Error(message)
        },
      }
    },
  }
}

function createSiteCatalog(initialFiles = []) {
  const files = new Map(initialFiles.map((file) => [file.out.path, file]))
  return {
    addFile(file) {
      files.set(file.out.path, file)
    },
    getFiles() {
      return [...files.values()]
    },
  }
}

function createPage(name) {
  return {
    src: { component: 'ROOT', version: '6.0.0', module: 'main' },
    pub: { url: `/6.0.0/main/${name}/` },
    out: { path: `6.0.0/main/${name}/index.html` },
    contents: Buffer.from(name),
  }
}

function produceRedirects(rules, pages = [createPage('clients'), createPage('about')]) {
  const context = createContext()
  const siteCatalog = createSiteCatalog()
  registerRedirects(context, rules)
  context.handlers.get('navigationBuilt')({
    contentCatalog: { getPages: () => pages },
    siteCatalog,
    playbook: {
      site: { url: 'https://example.com' },
      urls: { htmlExtensionStyle: 'indexify', redirectFacility: 'static' },
    },
  })
  return siteCatalog.getFiles()
}

test('literal HTML redirects retain their filenames and Antora source metadata', () => {
  const aliases = produceRedirects([
    { from: '/legacy/clients/index.html', to: '/6.0.0/main/clients/' },
    { from: '/legacy/about.html', to: '/6.0.0/main/about/' },
  ])

  assert.deepEqual(aliases.map((file) => file.out.path).sort(), [
    'legacy/about.html',
    'legacy/clients/index.html',
  ])
  for (const alias of aliases) {
    assert.deepEqual(alias.src, { component: 'ROOT', version: '6.0.0', module: 'main' })
    assert.match(alias.contents.toString(), /6\.0\.0\/main\//)
  }
})

test('extensionless redirects keep indexified directory URLs', () => {
  const aliases = produceRedirects([
    { from: '/legacy/*', to: '/6.0.0/main/*/' },
  ])

  assert.deepEqual(aliases.map((file) => file.out.path).sort(), [
    'legacy/about/index.html',
    'legacy/clients/index.html',
  ])
})

test('historical playbook redirects preserve legacy URLs and target ROOT pages', () => {
  const playbook = load(readFileSync(`${__dirname}/../antora-playbook.yml`, 'utf8'))
  const { redirects } = playbook.antora.extensions.find(
    (extension) => extension.require === '@inditextech/docouture-antora-extensions'
  )
  const versions = ['4.0.0', '5.0.0', '5.0.1', '5.1.0', '5.2.0', '5.3.0', '5.4.0', '6.0.0']
  const rules = versions.map((version) => {
    const rule = redirects.find((entry) => entry.from === `/karatetools-oss/karatetools-oss/${version}/**`)
    assert.ok(rule, `Missing legacy redirect for ${version}`)
    assert.equal(rule.to, `/${version}/**`)
    return rule
  })
  const pages = versions.map((version) => ({
    src: { component: 'ROOT', version, module: 'clients' },
    pub: { url: `/${version}/clients/jms/` },
    out: { path: `${version}/clients/jms/index.html` },
    contents: Buffer.from('JMS'),
  }))
  const aliases = produceRedirects(rules, pages)

  assert.equal(aliases.length, versions.length)
  for (const [index, version] of versions.entries()) {
    assert.equal(aliases[index].out.path, `karatetools-oss/karatetools-oss/${version}/clients/jms/index.html`)
    assert.equal(aliases[index].src.component, 'ROOT')
    assert.ok(aliases[index].contents.toString().includes(`/${version}/clients/jms/`))
  }
})

test('latest duplication preserves an existing redirect and copies unrelated pages', () => {
  const context = createContext()
  const redirect = {
    out: { path: 'latest/main/clients/index.html' },
    contents: Buffer.from('redirect to the migrated clients page'),
  }
  const siteCatalog = createSiteCatalog([redirect])
  registerDuplicateLatestVersion(context, { duplicateLatestVersion: true })
  context.handlers.get('pagesComposed')({
    contentCatalog: {
      getComponents: () => [{ name: 'ROOT', latest: { version: '6.0.0' } }],
      getFiles: () => [createPage('clients'), createPage('about')],
    },
    siteCatalog,
  })

  assert.equal(siteCatalog.getFiles().find((file) => file.out.path === redirect.out.path), redirect)
  assert.equal(siteCatalog.getFiles().find((file) => file.out.path === 'latest/main/about/index.html').contents.toString(), 'about')
})