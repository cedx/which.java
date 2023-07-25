import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Performs the static analysis of source code.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.ShortClassName", "PMD.UseUtilityClass"})
class Lint {

	/**
	 * The list of directories to analyze.
	 */
	static final List<String> directories = List.of("example", "scripts", "src/io/belin", "test");

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException {
		exec("java scripts/Build.java --debug");

		var environment = Map.of("CLASSPATH", getClassPath(Path.of("bin")));
		var mainClass = "net.sourceforge.pmd.cli.PmdCli";
		var sources = String.join(",", directories.toArray(String[]::new));
		exec("java %s cpd --dir=%s --exclude=scripts --minimum-tokens=100".formatted(mainClass, sources), environment);
		exec("java %s check --cache=var/pmd.cache --dir=%s --no-progress --rulesets=etc/pmd.xml".formatted(mainClass, sources), environment);
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
