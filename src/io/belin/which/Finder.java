package io.belin.which;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds the instances of an executable in the system path.
 */
public final class Finder {

	/**
	 * Value indicating whether the current platform is Windows.
	 */
	public static final boolean isWindows =
		System.getProperty("os.name").startsWith("Windows") || List.of("cygwin", "msys").contains(Objects.toString(System.getenv("OSTYPE"), ""));

	/**
	 * The list of executable file extensions.
	 */
	public final List<String> extensions;

	/**
	 * The list of system paths.
	 */
	public final List<Path> paths;

	/**
	 * Creates a new finder.
	 */
	public Finder() {
		this(null, null);
	}

	/**
	 * Creates a new finder.
	 * @param paths The system path. Defaults to the `PATH` environment variable.
	 */
	public Finder(List<Path> paths) {
		this(paths, null);
	}

	/**
	 * Creates a new finder.
	 * @param paths The system path. Defaults to the `PATH` environment variable.
	 * @param extensions The executable file extensions. Defaults to the `PATHEXT` environment variable.
	 */
	public Finder(List<Path> paths, List<String> extensions) {
		var extensionList = Objects.requireNonNullElse(extensions, Collections.<String>emptyList());
		if (extensionList.isEmpty()) {
			var pathExt = System.getenv("PATHEXT");
			extensionList = pathExt == null || pathExt.isEmpty() ? List.of(".exe", ".cmd", ".bat", ".com") : Arrays.asList(pathExt.split(";"));
		}

		var pathList = Objects.requireNonNullElse(paths, Collections.<Path>emptyList());
		if (pathList.isEmpty()) {
			var pathEnv = System.getenv("PATH");
			pathList = pathEnv == null || pathEnv.isEmpty()
				? Collections.emptyList()
				: Arrays.stream(pathEnv.split(isWindows ? ";" : File.pathSeparator)).map(Path::of).toList();
		}

		this.extensions = extensionList.stream()
			.map(item -> item.toLowerCase(Locale.getDefault()))
			.collect(Collectors.toList());

		this.paths = pathList.stream()
			.map(item -> item.toString().replaceAll("^\"|\"$", ""))
			.filter(item -> !item.isEmpty())
			.map(Path::of)
			.collect(Collectors.toList());
	}

	/**
	 * Finds the instances of the specified command in the system path.
	 * @param command The command to be resolved.
	 * @return The search results.
	 */
	public static ResultSet which(String command) {
		return which(command, null, null);
	}

	/**
	 * Finds the instances of the specified command in the system path.
	 * @param command The command to be resolved.
	 * @param paths The system path. Defaults to the `PATH` environment variable.
	 * @return The search results.
	 */
	public static ResultSet which(String command, List<Path> paths) {
		return which(command, paths, null);
	}

	/**
	 * Finds the instances of the specified command in the system path.
	 * @param command The command to be resolved.
	 * @param paths The system path. Defaults to the `PATH` environment variable.
	 * @param extensions The executable file extensions. Defaults to the `PATHEXT` environment variable.
	 * @return The search results.
	 */
	public static ResultSet which(String command, List<Path> paths, List<String> extensions) {
		return new ResultSet(command, new Finder(paths, extensions));
	}

	/**
	 * Finds the instances of an executable in the system path.
	 * @param command The command to be resolved.
	 * @return The paths of the executables found.
	 */
	public Stream<Path> find(String command) {
		Objects.requireNonNull(command);
		return paths.stream().flatMap(directory -> findExecutables(directory, command));
	}

	/**
	 * Gets a value indicating whether the specified file is executable.
	 * @param file The path of the file to be checked.
	 * @return `true` if the specified file is executable, otherwise `false`.
	 */
	public boolean isExecutable(Path file) {
		if (!Files.isRegularFile(Objects.requireNonNull(file))) return false;
		return isWindows ? checkFileExtension(file) : checkFilePermissions(file);
	}

	/**
	 * Checks that the specified file is executable according to the executable file extensions.
	 * @param path The file to be checked.
	 * @return Value indicating whether the specified file is executable.
	 */
	private boolean checkFileExtension(Path path) {
		var extension = Optional.of(path.toString())
			.filter(value -> value.contains("."))
			.map(value -> value.substring(value.lastIndexOf('.')));

		return extension.isPresent() && extensions.contains(extension.get().toLowerCase(Locale.getDefault()));
	}

	/**
	 * Checks that the specified file is executable according to its permissions.
	 * @param path The file to be checked.
	 * @return Value indicating whether the specified file is executable.
	 */
	@SuppressWarnings("PMD.AvoidUsingOctalValues")
	private boolean checkFilePermissions(Path path) {
		try {
			var attributes = Files.readAttributes(path, "unix:gid,mode,uid");
			var process = new com.sun.security.auth.module.UnixSystem();

			// Others.
			var perms = (int) attributes.get("mode");
			if ((perms & 0001) != 0) return true;

			// Group.
			var gid = (int) attributes.get("gid");
			if ((perms & 0010) != 0) return process.getGid() == gid;

			// Owner.
			var uid = (int) attributes.get("uid");
			if ((perms & 0100) != 0) return process.getUid() == uid;

			// Root.
			return (perms & (0100 | 0010)) != 0 && uid == 0;
		}
		catch (IOException e) {
			return false;
		}
	}

	/**
	 * Finds the instances of an executable in the specified directory.
	 * @param directory The directory path.
	 * @param command The command to be resolved.
	 * @return The paths of the executables found.
	 */
	private Stream<Path> findExecutables(Path directory, String command) {
		return Stream.concat(Stream.of(""), isWindows ? extensions.stream() : Stream.empty())
			.map(extension -> directory.resolve(command + extension).toAbsolutePath())
			.filter(this::isExecutable);
	}

	/**
	 * Provides convenient access to the stream of search results.
	 */
	public static class ResultSet {

		/**
		 * The searched command.
		 */
		private final String command;

		/**
		 * The finder used to perform the search.
		 */
		private final Finder finder;

		/**
		 * Creates a new result set.
		 * @param command The searched command.
		 * @param finder The finder used to perform the search.
		 */
		ResultSet(String command, Finder finder) {
			this.command = Objects.requireNonNull(command);
			this.finder = Objects.requireNonNull(finder);
		}

		/**
		 * Returns all instances of the searched command.
		 * @return All search results.
		 */
		public Optional<List<Path>> all() {
			var executables = stream().distinct().toList();
			return executables.isEmpty() ? Optional.empty() : Optional.of(executables);
		}

		/**
		 * Returns the first instance of the searched command.
		 * @return The first search result.
		 */
		public Optional<Path> first() {
			return stream().findFirst();
		}

		/**
		 * Returns a stream of instances of the searched command.
		 * @return A stream of the search results.
		 */
		public Stream<Path> stream() {
			return finder.find(command);
		}
	}
}
