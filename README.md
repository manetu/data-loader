# data-loader

A command-line tool to load attribute data, at scale, into the Manetu platform.  This tool emulates the basic functions of a connector, but in a manner designed to facilitate testing.  The basic premise is that a JSON file can be utilized to define a set of customers such as one may find in an enterprise store (e.g. Salesforce, Outlook, etc), and this tool can drive various operations into the Manetu control plane based on this data.

## Basic features

Based on an input .json file with 1 or more PII definitions, users of this tool may operate in one of three modes:

1. **create-vaults**: Create a vault for each user, based on a hash of the email address
2. **load-attributes**: Load attributes into the vault, based on the Person schema
3. **delete-attributes**: Delete all attributes from the vault

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

The parameters '--url --tls --provider --email --password' are all related to the context of the caller and the environment they are targetting.

'--mode' and the <file.json> input relate to the operation that the tool will perform.

Each row in the <file.json> will create 1 parallel GRPC to the server, simultaneously.  The system will measure time and report the status of each GRPC as they are returned.

### Example

##### Create vaults for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 -e realm-admin@mockprovider.com -p test --provider mockprovider.com -m create-vaults sample-data/mock-data-100.json
```

##### Load attributes for Users

```
$ ./target/manetu-data-loader -u https://ghaskins.sbx.aws.manetu.com:443 -e realm-admin@mockprovider.com -p test --provider mockprovider.com -m load-attributes sample-data/mock-data-100.json
```
