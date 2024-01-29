package io.belin.which;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.List;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Find the instances of an executable in the system path.
 */
@Command(
	name = "which",
	description = "Find the instances of an executable in the system path.",
	mixinStandardHelpOptions = true,
	versionProvider = Program.class
)
class Program implements Callable<Integer>, IVersionProvider {

	/**
	 * Value indicating whether to list all executables found.
	 */
	@Option(
		names = {"-a", "--all"},
		description = "List all executable instances found (instead of just the first one)."
	)
	private boolean all;

	/**
	 * The name of the executable to find.
	 */
	@Parameters(
		index = "0",
		description = "The name of the executable to find."
	)
	private String command;

	/**
	 * Value indicating whether to silence the output.
	 */
	@Option(
		names = {"-s", "--silent"},
		description = "Silence the output, just return the exit code (0 if any executable is found, otherwise 1)."
	)
	private boolean silent;

	/**
	 * Application entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) {
		System.exit(new CommandLine(new Program()).execute(args));
	}

	/**
	 * Runs this program.
	 * @return The exit code.
	 */
	@Override
	@SuppressWarnings("PMD.SystemPrintln")
	public Integer call() {
		var finder = new Finder();
		var resultSet = new Finder.ResultSet(command, finder);

		var executables = all ? resultSet.all() : resultSet.first().map(List::of);
		if (!silent) {
			if (executables.isPresent()) {
				var paths = executables.get().stream().map(Path::toString);
				System.out.println(paths.collect(Collectors.joining(System.lineSeparator())));
			}
			else {
				var paths = finder.paths.stream().map(Path::toString);
				System.err.printf("No '%s' in (%s)%n", command, paths.collect(Collectors.joining(Finder.isWindows ? ";" : File.pathSeparator)));
			}
		}

		return executables.isEmpty() ? 1 : 0;
	}

	/**
	 * Gets the package version of this program.
	 * @return The package version of this program.
	 */
	@Override public String[] getVersion() {
		return new String[] {getClass().getPackage().getImplementationVersion()};
	}
}
