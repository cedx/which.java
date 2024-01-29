package io.belin.which;

import static io.belin.which.Finder.which;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the features of the {@link Finder.ResultSet} class.
 */
@DisplayName("Finder.ResultSet")
final class ResultSetTest {

	@Test
	@DisplayName("all()")
	@SuppressWarnings("PMD.ConfusingTernary")
	void all() {
		var paths = List.of(Path.of("share"));

		// It should return the path of the `executable.cmd` file on Windows.
		var executables = which("executable", paths).all();
		if (!Finder.isWindows) assertTrue(executables.isEmpty());
		else {
			assertEquals(1, executables.get().size());
			assertTrue(executables.get().getFirst().toString().endsWith("\\share\\executable.cmd"));
		}

		// It should return the path of the `executable.sh` file on POSIX.
		executables = which("executable.sh", paths).all();
		if (Finder.isWindows) assertTrue(executables.isEmpty());
		else {
			assertEquals(1, executables.get().size());
			assertTrue(executables.get().getFirst().toString().endsWith("/share/executable.sh"));
		}

		// It should return an empty array if the searched command is not executable or not found.
		assertTrue(which("not_executable.sh", paths).all().isEmpty());
		assertTrue(which("foo", paths).all().isEmpty());
	}

	@Test
	@DisplayName("first()")
	void first() {
		var paths = List.of(Path.of("share"));

		// It should return the path of the `executable.cmd` file on Windows.
		var executable = which("executable", paths).first();
		if (Finder.isWindows) assertTrue(executable.get().toString().endsWith("\\share\\executable.cmd"));
		else assertTrue(executable.isEmpty());

		// It should return the path of the `executable.sh` file on POSIX.
		executable = which("executable.sh", paths).first();
		if (Finder.isWindows) assertTrue(executable.isEmpty());
		else assertTrue(executable.get().toString().endsWith("/share/executable.sh"));

		// It should return an empty string if the searched command is not executable or not found.
		assertTrue(which("not_executable.sh", paths).first().isEmpty());
		assertTrue(which("foo", paths).first().isEmpty());
	}
}
