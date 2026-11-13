const assert = require('node:assert/strict')
const { spawnSync } = require('node:child_process')
const { existsSync, readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { test } = require('node:test')
const { load } = require('js-yaml')

const docsDir = resolve(__dirname, '..')
const workflowsDir = resolve(docsDir, '../.github/workflows')

function resolveRelease(...args) {
  return spawnSync(process.execPath, [
    resolve(docsDir, 'node_modules/.bin/docouture'),
    'release', 'resolve', '--mode', 'versioned', ...args,
  ], { cwd: docsDir, encoding: 'utf8' })
}

function assertRelease(result, source) {
  assert.ifError(result.error)
  assert.equal(result.status, 0, result.stderr)
  const outputs = Object.fromEntries(
    result.stdout.trim().split('\n').map((line) => {
      const separator = line.indexOf('=')
      assert.ok(separator > 0, `Unexpected resolver output: ${line}`)
      return [line.slice(0, separator), line.slice(separator + 1)]
    })
  )
  assert.deepEqual(outputs, {
    version: '6.1.0',
    tag: 'docs/v6.1.0',
    source,
    version_source: 'github-release',
    uses_descriptor: 'false',
  })
}

test('Docouture packages and version source stay aligned with the Git Flow release model', () => {
  const pkg = JSON.parse(readFileSync(resolve(docsDir, 'package.json'), 'utf8'))
  const packages = Object.entries(pkg.devDependencies).filter(
    ([name]) => name.startsWith('@inditextech/docouture-')
  )

  assert.equal(packages.length, 5)
  for (const [name, version] of packages) assert.equal(version, '1.4.1', name)
  assert.equal(pkg.docouture.versionSource, 'github-release')
  assert.equal(pkg.docouture.branching, 'git-flow')
  assert.equal(pkg.docouture.prereleaseBranch, 'develop')
  assert.equal(pkg.docouture.versionSourcePrefix, undefined)
  assert.equal(existsSync(resolve(docsDir, '.release-version')), false)
})

test('a docs-only merge selects the highest published stable release, not list order', () => {
  assertRelease(resolveRelease(
    '--event', 'pull_request', '--labels', 'docs/release',
    '--releases', '6.0.0,not-semver,6.1.0,6.2.0-rc.1,5.4.0'
  ), 'latest GitHub Release')
})

test('a published Java release supplies the documentation version without a descriptor bump', () => {
  assertRelease(resolveRelease('--event', 'release', '--release-tag', '6.1.0'), 'release tag')
})

test('manual republishing accepts the explicit version without a descriptor bump', () => {
  assertRelease(resolveRelease(
    '--event', 'workflow_dispatch', '--version', '6.1.0'
  ), 'version input')
})

test('a docs-only merge without a published release fails explicitly', () => {
  const result = resolveRelease('--event', 'pull_request', '--labels', 'docs/release')

  assert.ifError(result.error)
  assert.notEqual(result.status, 0)
  assert.match(result.stderr, /::error::.*none was found/)
})

test('GitHub Release versioning rejects docs-only minor and major bump labels', () => {
  for (const label of ['docs/release-minor', 'docs/release-major']) {
    const result = resolveRelease(
      '--event', 'pull_request', '--labels', label, '--releases', '6.1.0'
    )
    assert.ifError(result.error)
    assert.notEqual(result.status, 0, label)
    assert.match(result.stderr, /::error::.*none was given/)
  }
})

test('release and preview workflows use the resolver and gate descriptor-only operations', () => {
  const release = load(readFileSync(resolve(workflowsDir, 'docouture-release.yml'), 'utf8'))
  const preview = load(readFileSync(resolve(workflowsDir, 'docouture-release-preview.yml'), 'utf8'))
  const steps = release.jobs.release.steps
  const bump = steps.find((step) => step.id === 'bump')
  const resolveVersion = steps.find((step) => step.id === 'resolve')
  const publishedReleases = steps.find((step) => step.id === 'releases')

  assert.deepEqual(release.on.pull_request.branches, ['main*'])
  assert.match(resolveVersion.run, /release resolve/)
  assert.match(resolveVersion.run, /--releases "\$RELEASES"/)
  assert.match(publishedReleases.run, /select\(\.draft \| not\)/)
  assert.match(publishedReleases.run, /select\(\.prerelease \| not\)/)
  assert.match(bump.if, /steps\.resolve\.outputs\.uses_descriptor == 'true'/)
  assert.match(release.jobs['sync-prerelease'].if, /needs\.release\.outputs\.uses_descriptor == 'true'/)
  assert.match(
    preview.jobs['release-preview'].steps.find((step) => step.id === 'resolve').run,
    /--releases "\$releases" --report/
  )
})
