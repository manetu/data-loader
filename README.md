# data-loader

[![CircleCI](https://circleci.com/gh/manetu/data-loader/tree/master.svg?style=svg)](https://circleci.com/gh/manetu/data-loader/tree/master)

The Manetu data-loader is a command-line tool to load and verify RDF-based attribute data, at scale, into the Manetu platform.  This tool emulates the essential functions of a Manetu data connector intended to facilitate testing.  The basic premise is that a user expresses a set of PII data in JSON or CSV format, and the tool provides various operations on this data, such as onboarding, verifying, and deleting vaults.

## Basic features

Based on an input .json file with one or more PII definitions, users of this tool may operate in one of four modes:

1. **create-vaults**: Create a vault for each user, based on a hash of the email address
2. **load-attributes**: Load attributes into the vault, based on the Person schema
3. **delete-attributes**: Delete all attributes from the vault
4. **onboard**: Simultaneously create vaults and load attributes.

## Installing

### Prerequisites

- JRE

``` shell
sudo wget https://github.com/manetu/data-loader/releases/download/v1.0.0/manetu-data-loader -O /usr/local/bin/manetu-data-loader
sudo chmod +x /usr/local/bin/manetu-data-loader
```

## Building

### Prerequisites

In addition to the requirements for installation, you will also need:

- Leiningen

```
$ make
```

## Usage

### Overview

```
$ ./target/manetu-data-loader -h
manetu-data-loader version: vX.Y.Z

Usage: manetu-data-loader [options] <file.json>

Options:
  -h, --help
  -v, --version                                                  Print the version and exit
  -u, --url URL                                                  The connection URL
  -i, --insecure           false                                 Disable TLS checks (dev only)
      --[no-]progress      true                                  Enable/disable progress output (default: enabled)
      --provider SID       manetu.com                            The service-provider
      --userid USERID                                            The id of the user to login with
  -p, --password PASSWORD                                        The password of the user
  -t, --token TOKEN                                              A personal access token
  -l, --log-level LEVEL    :info                                 Select the logging verbosity level from: [trace, debug, info, error]
      --fatal-errors       false                                 Any sub-operation failure is considered to be an application level failure
      --verbose-errors     false                                 Any sub-operation failure is logged as ERROR instead of TRACE
      --type TYPE          data-loader                           The type of data source this CLI represents
      --id ID              535CC6FC-EAF7-4CF3-BA97-24B2406674A7  The id of the data-source this CLI represents
      --class CLASS        global                                The schemaClass of the data-source this CLI represents
  -c, --concurrency NUM    16                                    The number of parallel requests to issue
  -m, --mode MODE          :load-attributes                      Select the mode from: [query-attributes, load-attributes, onboard, delete-vaults, delete-attributes, create-vaults]
  -d, --driver DRIVER      :grpc                                 Select the driver from: [graphql, grpc]```

The parameters '--url --provider --userid --password' are all related to the caller's context and the environment they are targetting.

'--mode' and the <file.json> input relate to the operation that the tool will perform.

Each row in the <file.json> will create parallel request to the server.  The system will measure time and report the status of each response.

### Example

##### Create vaults for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 --userid admin -p test --provider mockprovider.com -m create-vaults sample-data/mock-data-100.json
```

##### Load attributes for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 --userid admin -p test --provider mockprovider.com -m load-attributes sample-data/mock-data-100.json
