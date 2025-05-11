# data-loader

[![CircleCI](https://circleci.com/gh/manetu/data-loader/tree/master.svg?style=svg)](https://circleci.com/gh/manetu/data-loader/tree/master)

The Manetu data-loader is a command-line tool to validate and load attribute data onto the Manetu Platform. The tool is designed to run "at scale" such that it can put significant load on a Manetu Plaftorm for performance testing. Data-loader takes a set of Personally Identifiable Information (PII) in JSON or CSV format and executes a selected operation using this data, such as onboarding, verifying, and deleting vaults. The input data is assumed to be a RDF representation of the PII. By default, the tool loads attributes based on the [Person](https://schema.org/Person) schema with [email](https://schema.org/email)as the root of the attribute graph.

## Basic features

Based on an input file with one or more PII definitions, users of this tool may operate in one of four modes:

1. **create-vaults**: Create a vault for each user, based on a hash of the email address
2. **load-attributes**: Load attributes into the vault, based on the Person schema
3. **delete-attributes**: Delete all attributes from the vault
4. **onboard**: Simultaneously create vaults and load attributes.

## Installing

### Prerequisites

- A Java Runtime Environment (JRE)

A binary release of the latest version can be downloaded here:
``` shell
sudo wget https://github.com/manetu/data-loader/releases/download/v1.0.0/manetu-data-loader -O /usr/local/bin/manetu-data-loader
sudo chmod +x /usr/local/bin/manetu-data-loader
```
On MacOS you may need to remove the quarantine attribute
```
sudo xattr -r -d com.apple.quarantine /usr/local/bin/manetu-data-loader
```
## Building

### Prerequisites

In addition to the requirements for installation, you will also need:

- The clojure build tool `leiningen`

The to build:
```
$ make
```

## Usage

### Overview

``` shell
$ manetu-data-loader -h
manetu-data-loader version: v<M.m.s>

Usage: manetu-data-loader [options] <input-file>

Options:
  -h, --help
  -v, --version                                                Print version info and exit
  -u, --url URL                                                The connection URL
  -i, --insecure         false                                 Disable TLS checks
      --[no-]progress    true                                  Enable/disable progress output
  -t, --token TOKEN                                            A manetu personal access token
  -l, --log-level LEVEL  :info                                 Select the logging verbosity level from: [trace, debug, info, error]
      --fatal-errors     false                                 Any sub-operation failure is considered to be an application level failure
      --verbose-errors   false                                 Any sub-operation failure is logged as ERROR instead of TRACE
  -c, --concurrency NUM  16                                    The number of parallel requests to issue
  -m, --mode MODE        :load-attributes                      Select the mode from: [query-attributes, load-attributes, onboard, delete-vaults, delete-attributes, create-vaults]
  -d, --driver DRIVER    :graphql                              Select the driver from: [graphql]
      --id ID            535CC6FC-EAF7-4CF3-BA97-24B2406674A7  The RDF id to be applied the data-source
      --type TYPE        data-loader                           the RDF type of the data source
      --class CLASS      global                                The RDF schemaClass applied to the data source

```
The `<input-file>` input parameter is a json or csv file containing the data to load. Each row in  `<input-file>` will create a parallel request to the server up to the maximum as specified by the
`--concurrency` options (default: 16).  The system will measure the time taken for each request and report the status of each response.

The option: `--url` specifies the target manetu platform,

The option: `--token` is a Manetu Personal Access Token which authenticates to the target realm on your Manetu Platform instance

The option: `--mode` selects the operation that the tool will perform.

The options: `--id`, `--type`, and `--class` allow you to modify parameters of the RDF created from the input data. They are typically not used. For further information reach out to Manetu.
### Examples

Set value of the environment variable MANETU_TOKEN to a string containing a Personal Access Token for a user on your Manetu Platform deployment. 
##### Create vaults for Users

```
$ ./target/manetu-data-loader --url https://manetu.instance --token $MANETU_TOKEN --mode create-vaults sample-data/mock-data-100.json
```
##### Load attributes for Users

```
$ ./target/manetu-data-loader --url https://your.manetu.instance --token $MANETU_TOKEN --mode load-attributes sample-data/mock-data-100.json
```

