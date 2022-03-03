# data-loader

[![CircleCI](https://circleci.com/gh/manetu/data-loader/tree/master.svg?style=svg)](https://circleci.com/gh/manetu/data-loader/tree/master)

The Manetu data-loader is a command-line tool to load and verify RDF-based attribute data, at scale, into the Manetu platform.  This tool emulates the essential functions of a Manetu data connector intended to facilitate testing.  The basic premise is that a user expresses a set of PII data in JSON or CSV format, and the tool provides various operations on this data, such as onboarding, verifying, and deleting vaults.

## Basic features

Based on an input .json file with one or more PII definitions, users of this tool may operate in one of four modes:

1. **create-vaults**: Create a vault for each user, based on a hash of the email address
2. **load-attributes**: Load attributes into the vault, based on the Person schema
3. **delete-attributes**: Delete all attributes from the vault
4. **onboard**: Simultaneously create vaults and load attributes.

## Building

### Prerequisites

- Leiningen
- JRE

```
$ make
```

## Usage

### Overview

```
$ ./target/manetu-data-loader -h
manetu-data-loader version: v0.1.0

Usage: manetu-data-loader [options] <file.json>

Options:
  -h, --help
  -v, --version                                          Print the version and exit
  -u, --url URL            https://portal.manetu.io:443  The connection URL
  -t, --[no-]tls                                         Enable TLS
      --provider SID       manetu.com                    The service-provider
  -e, --email EMAIL                                      The email address of the user to login with
  -p, --password PASSWORD                                The password of the user
  -l, --log-level LEVEL    :info                         Select the logging verbosity level from: [trace, debug, info, error]
  -m, --mode MODE          :load-attributes              Select the mode from: [load-attributes, delete-attributes, create-vaults]
```

The parameters '--url --tls --provider --email --password' are all related to the caller's context and the environment they are targetting.

'--mode' and the <file.json> input relate to the operation that the tool will perform.

Each row in the <file.json> will create parallel GRPCs to the server.  The system will measure time and report the status of each response.

### Example

##### Create vaults for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 -e realm-admin@mockprovider.com -p test --provider mockprovider.com -m create-vaults sample-data/mock-data-100.json
```

##### Load attributes for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 -e realm-admin@mockprovider.com -p test --provider mockprovider.com -m load-attributes sample-data/mock-data-100.json
