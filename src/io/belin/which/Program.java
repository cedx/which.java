package io.belin.which;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.util.concurrent.Callable;

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
	boolean all;

	/**
	 * Value indicating whether to silence the output.
	 */
	@Option(
		names = {"-s", "--silent"},
		description = "Silence the output, just return the exit code (0 if any executable is found, otherwise 1)."
	)
	boolean silent;

	/**
	 * Application entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) {
		System.exit(new CommandLine(new Program()).execute(args));

		/*
		var arguments = List.of(args);

		var options = arguments.stream().filter(item -> item.startsWith("-")).toList();
		if (options.contains("-h") || options.contains("--help")) {
			printUsage();
			System.exit(0);
		}

		if (options.contains("-v") || options.contains("--version")) {
			System.out.println(Program.class.getPackage().getImplementationVersion());
			System.exit(0);
		}

		var positionals = arguments.stream().filter(item -> !item.startsWith("-")).toList();
		if (positionals.isEmpty()) {
			System.err.println("You must provide the name of a command to find.");
			System.exit(1);
		}*/
	}

	/**
	 * Runs this program.
	 */
	@Override public Integer call() {
		return 0;
	}

	/**
	 * Gets the package version of this program.
	 * @return The package version of this program.
	 */
	public String[] getVersion() {
		return new String[] {getClass().getPackage().getImplementationVersion()};
	}

	/**
	 * Prints the usage information.
	 */
	private static void printUsage() {
		System.out.print("""
			Find the instances of an executable in the system path.

			Usage:
			  which [options] <command>

			Arguments:
			  command        The name of the executable to find.

			Options:
			  -a, --all      List all executable instances found (instead of just the first one).
			  -s, --silent   Silence the output, just return the exit code (0 if any executable is found, otherwise 1).
			  -h, --help     Display this help.
			  -v, --version  Output the version number.
			""");
	}
}
