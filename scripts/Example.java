import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Runs a main class from the "example" folder.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Example {

	/**
	 * The base package of this library.
	 */
	static final String pack = "io.belin.which";

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String... args) throws InterruptedException, IOException {
		if (args.length == 0) {
			System.err.println("You must specify an example to run.");
			System.exit(1);
		}

		var script = Path.of("example", args[0] + ".java");
		if (!Files.exists(script)) {
			System.err.println("Class not found: " + script);
			System.exit(2);
		}

		var jar = Path.of("bin/%s.jar".formatted(pack));
		if (!Files.exists(jar)) exec("java scripts/Dist.java");
		System.exit(exec("java " + script, Map.of("CLASSPATH", getClassPath(jar))));
	}

	/**
	 * Executes the specified command.
	 * @param command The command to execute.
	 * @return The exit code of the executed command.
	 */
	private static int exec(String command) throws InterruptedException, IOException {
		return exec(command, null);
	}

	/**
	 * Executes the specified command.
	 * @param command The command to execute.
	 * @param environment The optional environment variables to add to the spawned process.
	 * @return The exit code of the executed command.
	 */
	private static int exec(String command, Map<String, String> environment) throws InterruptedException, IOException {
		var map = new HashMap<>(System.getenv());
		if (environment != null) map.putAll(environment);

		var variables = map.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue());
		var process = Runtime.getRuntime().exec(Objects.requireNonNull(command), variables.toArray(String[]::new));
		Stream.concat(process.errorReader().lines(), process.inputReader().lines()).parallel().forEach(System.out::println);
		return process.waitFor();
	}

	/**
	 * Returns the class path.
	 * @param path An optional path to prepend to the returned class path.
	 * @return The class path.
	 */
	private static String getClassPath(Path path) throws IOException {
		var prefix = path != null && Files.exists(path) ? path.toString() + File.pathSeparator : "";
		return prefix + Files.readString(Path.of(".classpath")).stripTrailing();
	}
}
