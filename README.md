# PeakabooPlugins
This repository contains all of the first-party plugins developed for [Peakaboo](https://github.com/nsherry4/Peakaboo).

[Download Plugins](https://github.com/PeakabooLabs/PeakabooPlugins/releases)

Because others may also develop plugins for Peakaboo, this may not be an exhaustive list.

## Building

Install Peakaboo into your local Maven repository first (`mvn install` in the Peakaboo tree), then run
`mvn verify` here. Plugin jars land in `Plugins/DataSource/`. The plugins build against the Peakaboo
version matching this repository's own version.

## Releasing

Run the **Build Plugins Release** workflow with the release version. It produces a zip of the plugin
jars and `contents.yaml`; attach its contents to this repository's `v<X.Y>` release.

The workflow uses the manifest tool from a
[RepositoryTooling](https://github.com/PeakabooLabs/RepositoryTooling) release, read with the
`REPOSITORY_TOOLING_READ_PAT` secret.
