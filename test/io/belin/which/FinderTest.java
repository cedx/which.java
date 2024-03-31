package io.belin.which;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the features of the {@link Finder} class.
 */
@DisplayName("Finder")
final class FinderTest {

	@Test
	@DisplayName("new()")
	void constructor() {
		// It should set the `paths` property to the value of the `PATH` environment variable by default.
		var pathEnv = System.getenv("PATH");
		var paths = pathEnv == null || pathEnv.isEmpty()
			? Collections.<Path>emptyList()
			: Arrays.stream(pathEnv.split(File.pathSeparator)).filter(item -> !item.isEmpty()).map(Path::of).toList();

		assertEquals(paths, new Finder().paths);

		// It should set the `extensions` property to the value of the `PATHEXT` environment variable by default.
		var pathExt = System.getenv("PATHEXT");
		var extensions = pathExt == null || pathExt.isEmpty()
			? List.of(".exe", ".cmd", ".bat", ".com")
			: Arrays.stream(pathExt.split(";")).map(item -> item.toLowerCase(Locale.getDefault())).toList();

		assertEquals(extensions, new Finder().extensions);

		// It should put in lower case the list of file extensions.
		assertEquals(List.of(".exe", ".js", ".ps1"), new Finder(null, List.of(".EXE", ".JS", ".PS1")).extensions);
	}

	@Test
	@DisplayName("find()")
	void find() {
		var finder = new Finder(List.of(Path.of("share")));

		// It should return the path of the `executable.cmd` file on Windows.
		var executables = finder.find("executable").toList();
		assertEquals(Finder.isWindows ? 1 : 0, executables.size());
		if (Finder.isWindows) assertTrue(executables.getFirst().endsWith("share\\executable.cmd"));

		// It should return the path of the `executable.sh` file on POSIX.
		executables = finder.find("executable.sh").toList();
		assertEquals(Finder.isWindows ? 0 : 1, executables.size());
		if (!Finder.isWindows) assertTrue(executables.getFirst().endsWith("res/executable.sh"));

		// It should return an empty array if the searched command is not executable or not found.
		assertEquals(0, finder.find("not_executable.sh").count());
		assertEquals(0, finder.find("foo").count());
	}

	@Test
	@DisplayName("isExecutable()")
	void isExecutable() {
		var finder = new Finder();

		// It should return `false` if the searched command is not executable or not found.
		assertFalse(finder.isExecutable(Path.of("foo/bar/baz.qux")));
		assertFalse(finder.isExecutable(Path.of("res/not_executable.sh")));

		// It should return `false` for a POSIX executable, when test is run on Windows.
		assertEquals(!Finder.isWindows, finder.isExecutable(Path.of("res/executable.sh")));

		// It should return `false` for a Windows executable, when test is run on POSIX.
		assertEquals(Finder.isWindows, finder.isExecutable(Path.of("res/executable.cmd")));
	}
}
