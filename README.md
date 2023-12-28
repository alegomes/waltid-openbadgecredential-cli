# waltid-openbadgecredential-cli
A console application in Kotlin that uses the walt.id community stack to create and verify a W3C Verifiable Credential of type "OpenBadgeCredential" 

# Build it
```
$ gradle clean build
```
# Usage instructions
```
$ gradle run
> Task :walt-cli:run
Usage: walt [-hV] [COMMAND]
Command line tool to issue and verify Open Badge credentials.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  issue  Issues a verifiable credential
```
# Issue a credential
```
$ gradle run --args="issue"
```
# Present an issued credential
```
$ gradle run --args="present"
```