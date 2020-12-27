# commit-analyser
This program analyses a code repository and generates a report that lists all commits that removed method parameters.

To run, please clone the project and run the following from the command-line:

```./gradlew run --args='[https-remote-git-url] [output-path]'```

Sample output:

```Mandar@Mandar-PC MINGW64 ~/Desktop/commit-analyser (main)
$ ./gradlew run --args='https://github.com/apache/dubbo-samples.git out'

> Task :run
2020-12-27 12:11:18:965 +0530 [main] INFO GitClient - Cloning remote repository present at https://github.com/apache/dubbo-samples.git at local path 'out\remote'
2020-12-27 12:11:18:965 +0530 [main] WARN GitClient - Directory out\remote exists. Contents of this directory would be recursively deleted.
2020-12-27 12:11:33:826 +0530 [main] INFO GitClient - Repository cloned
2020-12-27 12:11:33:826 +0530 [main] INFO GitClient - Obtaining commits modifying lines resembling Java method declarations
2020-12-27 12:11:35:394 +0530 [main] INFO GitClient - Found 259 such commits
2020-12-27 12:11:35:394 +0530 [main] INFO Application - Iterating over commits, parsing files if required and generating report..
2020-12-27 12:11:37:340 +0530 [main] INFO Application - Found 5 commits where method parameters were removed
2020-12-27 12:11:37:341 +0530 [main] INFO ReportWriter - Writing report
2020-12-27 12:11:37:511 +0530 [main] INFO Application - Wrote report to report_dubbo-samples_20201227121137.csv

BUILD SUCCESSFUL in 22s
4 actionable tasks: 3 executed, 1 up-to-date

```
## Flow

A flowchart describing high-level flow of the program:

<img src="blob/flow.png" alt="IMAGE ALT TEXT HERE" width="1024" height="700" border="10" />

## Core logic

The core logic to identify whether a parameter was removed is as follows:

1. Obtain old and new function signature with their name and parameter type list.
   
   e.g. ```Old function signature: "myFunc" : \["int", "MyClass", "long"\]```
   
        New function signature: "myFunc" : \["int", "MyClass", "double"\]
        
   _Note: local parameter variables are not considered as they do not change function signatures
2. Element-wise compare each parameter from old list to corresponding parameter from new list, 
   if there is a mismatch, report that parameter was removed from the list.

## Sample Reports:

Please find sample reports in `/reports` directory generated for some popular Java repositories:
* [redisson](https://github.com/redisson/redisson.git) : ~6000 commits on default branch, 2m40s to generate report on my local machine
* [retrofit](https://github.com/square/retrofit.git) : ~2000 commits on default branch, 46s to generate report on my local machine
* [spring-boot](https://github.com/spring-projects/spring-boot.git) ~30000 commits on default branch, 7m40s to generate report on my local machine

_Note: times mentioned include time taken to clone the repository_

## Current assumptions:

* The program expects an HTTPS Git remote URL.
* This program analyses commits starting from where the HEAD points at (generally main or master branch) and walks these commits backwards. It would not be difficult to extend the same code to generate report for all branches.
