package io.belin.which;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
			: Arrays.asList(pathEnv.split(File.pathSeparator)).stream().filter(item -> !item.isEmpty()).map(Path::of).toList();

		assertEquals(new Finder().paths, paths);

		// It should set the `extensions` property to the value of the `PATHEXT` environment variable by default.
		var pathExt = System.getenv("PATHEXT");
		var extensions = pathExt == null || pathExt.isEmpty()
			? List.of(".exe", ".cmd", ".bat", ".com")
			: Arrays.asList(pathExt.split(";")).stream().map(item -> item.toLowerCase()).toList();

		assertEquals(new Finder().extensions, extensions);

		// It should put in lower case the list of file extensions.
		assertEquals(new Finder(null, List.of(".EXE", ".JS", ".PS1")).extensions, List.of(".exe", ".js", ".ps1"));
	}

	/*
	@Test
	@DisplayName("find()")
	void find() {
		var finder = new Finder(List.of("share"));

		// It should return the path of the `executable.cmd` file on Windows.
		$executables = [...finder.find("executable");
		assertThat($executables, countOf(Finder.isWindows ? 1 : 0));
		if (Finder.isWindows) assertThat($executables[0]->getPathname(), stringEndsWith("\\share\\executable.cmd"));

		// It should return the path of the `executable.sh` file on POSIX.
		$executables = [...finder.find("executable.sh");
		assertThat($executables, countOf(Finder.isWindows ? 0 : 1));
		if (!Finder.isWindows) assertThat($executables[0]->getPathname(), stringEndsWith("/share/executable.sh"));

		// It should return an empty array if the searched command is not executable or not found.
		assertThat([...finder.find("not_executable.sh"), isEmpty());
		assertThat([...finder.find("foo"), isEmpty());
	}*/

	@Test
	@DisplayName("isExecutable()")
	void isExecutable() {
		var finder = new Finder();

		// It should return `false` if the searched command is not executable or not found.
		assertFalse(finder.isExecutable(Path.of("foo/bar/baz.qux")));
		assertFalse(finder.isExecutable(Path.of("share/not_executable.sh")));

		// It should return `false` for a POSIX executable, when test is run on Windows.
		assertEquals(finder.isExecutable(Path.of("share/executable.sh")), !Finder.isWindows);

		// It should return `false` for a Windows executable, when test is run on POSIX.
		assertEquals(finder.isExecutable(Path.of("share/executable.cmd")), Finder.isWindows);
	}
}
