# waltid-openbadgecredential-cli
A console application in Kotlin that uses the Walt.Id Community Stack to create and verify a W3C Verifiable Credential of type [OpenBadgeCredential](https://www.imsglobal.org/spec/ob/v3p0) 

It doesn't mean to be a comprehensive CLI tool but just a sample code on how to use the Identity SDK for a basic credential flow :-) 
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
  issue    Issues a verifiable credential
  present  Presents a verifiable credential for verification
  verify   Presents a verifiable credential for verification
```
# Issue a credential
```
$ gradle run --args="issue"
```
The issued VC will be saved in the ```vc.json``` and ```vc.jws``` files. 
# Create a presentation
```
$ gradle run --args="present"
```
With no extra option provided, the presentation will be issued based on the VC found in the ```vc.jws``` file.
# Verify the presentation
```
$ gradle run --args="verify"
```
With no extra option provided, the verification will proceed based on the VP found in the ```vp.jws``` file.
