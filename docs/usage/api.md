# Application programming interface
This package provides the `Finder.which(String command)` method, allowing to locate a command in the system path.

This method takes the name of the command to locate, and returns a `Finder.ResultSet` with the three following methods:

- `all()` : get all instances of the searched command.
- `first()` : get the first instance of the searched command.
- `stream()` : get a stream of instances of the searched command.

### Optional&lt;List&lt;Path&gt;&gt; **all()**
The `ResultSet.all()` method returns a [`List`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html)
of the absolute [`Path`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html)s of all instances of an executable found in the system path, 
wrapped in a [`Optional`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Optional.html).
If the executable could not be located, it returns an empty `Optional`.

```java
import static io.belin.which.Finder.which;

class Program {
  public static void main(String... args) {
    var paths = which("foobar").all();
    if (paths.isEmpty()) System.err.println("The 'foobar' command cannot be found.");
    else {
      System.out.println("The 'foobar' command is available at these locations:");
      for (var path: paths.get()) System.out.println("- " + path);
    }
  }
}
```

### Optional&lt;Path&gt; **first()**
The `ResultSet.first()` method returns the absolute [`Path`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html)
of the first instance of an executable found in the system path, 
wrapped in an [`Optional`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Optional.html).
If the executable could not be located, it returns an empty `Optional`.

```java
import static io.belin.which.Finder.which;

class Program {
  public static void main(String... args) {
    var path = which("foobar").first();
    if (path.isEmpty()) System.err.println("The 'foobar' command cannot be found.");
    else System.out.println("The 'foobar' command is located at: " + path.get());
  }
}
```

### Stream&lt;Path&gt; **stream()**
The `ResultSet.stream()` method returns a [`Stream`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Stream.html) that yields
a [`Path`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html) instance for each executable found in the system path.

```java
import static io.belin.which.Finder.which;

class Program {
  public static void main(String... args) {
    System.out.println("The 'foobar' command is available at these locations:");
    which("foo").stream().forEach(path -> System.out.println("- " + path));
  }
}
```

## Options
The behavior of the `Finder.which(String command, List<Path> paths, List<String> extensions)` method can be customized using the following parameters.

### List&lt;Path&gt; **paths**
A [`List`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html)
of [`Path`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Path.html)s specifying the system paths
from which the given command will be searched.

Defaults to the list of directories provided by the `PATH` environment variable.

```java
import static io.belin.which.Finder.which;
import java.nio.file.Path;
import java.util.List;

which("foobar", List.of(Path.of("/usr/local/bin"), Path.of("/usr/bin")));
```

### List&lt;String&gt; **extensions**
A [`List`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html) of strings specifying the executable file extensions.

On Windows, defaults to the list of extensions provided by the `PATHEXT` environment variable.

```java
import static io.belin.which.Finder.which;
import java.util.List;

which("foobar", null, List.of(".foo", ".exe", ".cmd"));
```

> The `extensions` option is only meaningful on the Windows platform, where the executability of a file is determined from its extension.
