# Command line interface
Download the latest `cli` JAR file of **Which for Java** from the GitHub releases:  
[https://github.com/cedx/which.java/releases/latest](https://github.com/cedx/which.java/releases/latest)

Then use it to find the instances of an executable command:

```shell
$ java -jar which-cli.jar --help

Usage: which [-ahsV] <command>
Find the instances of an executable in the system path.
      <command>   The name of the executable to find.
  -a, --all       List all executable instances found (instead of just the
                    first one).
  -h, --help      Show this help message and exit.
  -s, --silent    Silence the output, just return the exit code (0 if any
                    executable is found, otherwise 1).
  -V, --version   Print version information and exit.
```

For example:

```shell
java -jar which-cli.jar --all java
# /usr/bin/java
# /usr/local/bin/java
```
