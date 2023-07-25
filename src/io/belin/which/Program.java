package io.belin.which;

import java.util.concurrent.Callable;
import java.io.File;
import java.util.List;
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
@SuppressWarnings("PMD.SystemPrintln")
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
	 */
	@Override public Integer call() {
		var finder = new Finder();
		var resultSet = new ResultSet(command, finder);

		var executables = all ? resultSet.all() : resultSet.first().map(path -> List.of(path));
		if (!silent) {
			if (executables.isPresent()) System.out.println(String.join(System.lineSeparator(), executables.get().toArray(String[]::new)));
			else { /* TODO */}
		}

		return executables.isEmpty() ? 1 : 0;
	}

	/**
	 * Gets the package version of this program.
	 * @return The package version of this program.
	 */
	public String[] getVersion() {
		return new String[] {getClass().getPackage().getImplementationVersion()};
	}
}
