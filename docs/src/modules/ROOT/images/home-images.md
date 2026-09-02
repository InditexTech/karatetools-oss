# Home images

The home-page artwork is an original, AI-generated set owned by INDITEX S.A.
The final JPEGs live in `home/`; their lossless PNG masters live in
`home/masters/`. License metadata is recorded in the root `REUSE.toml` file.

## Visual style

- Clean editorial technical illustration on a predominantly white canvas.
- Thin black structural linework, very light gray surfaces, a faint orthogonal
  construction grid and generous safe margins.
- Cyan (`#0ABAD9`, `#1288A9`) marks active test flows, orange (`#F68A11`)
  marks generated or build checkpoints, and magenta-red (`#D22443`) is
  reserved for test and review gates.
- Testing systems are shown as transformations, pipelines and modular Java
  package geometry. Meaning must remain clear without relying on color.
- No people, reproduced logos, photorealism, glossy 3D, gradients, decorative
  multicolor, dense text or watermarks.

The generated set uses the Karate Tools logo as a palette reference only and
the Docouture `home-hero.jpg` and `card-architecture.jpg` assets as linework and
composition references. The built-in image generator created one distinct
image per asset.

## Alternative hero direction

`home/hero-v2.jpg` explores a deliberately different visual language and is
not currently active on the page. It treats Karate Tools as a tactile testing
workbench built from matte paper and shallow bas-relief modules. Relationships
come from proximity, nesting and fitted tracks rather than grids, arrows and
thin-line flowcharts. The logo palette becomes the material palette: cyan and
charcoal form the workbench, orange identifies the Java/build layer, and one
magenta insert marks the test result. The final is 1600×900; its PNG master is
`home/masters/hero-v2.png`.

## Assets

- `home/hero.jpg` — OpenAPI and configuration inputs move through a Java build,
  test assets and service clients into a final report (1600×900).
- `home/get-started-quickstart.jpg` — one command creates and validates a
  ready-to-run test module (1024×1024).
- `home/get-started-guides.jpg` — four connected steps from setup to reporting
  (1024×1024).
- `home/get-started-reference.jpg` — structured configuration and package
  lookup (1024×1024).
- `home/get-started-contributing.jpg` — contribution, review, automated tests
  and merge (1024×1024).
- `home/capability-scaffold.jpg` — project parts assemble into a validated
  Karate module (1600×900).
- `home/capability-openapi.jpg` — an OpenAPI definition becomes scenarios,
  data, mocks and validation flows (1600×900).
- `home/capability-execution.jpg` — environments and services converge into a
  result report (1600×900).

## Retired emoji assets

The previous card images remain in this directory for history. They came from
https://github.com/googlefonts/noto-emoji and retain their existing individual
entries in `REUSE.toml`: `dev.png`, `lab.png`, `tools.png` and `training.png`.
