import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Runs the test suite.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.ShortClassName", "PMD.UseUtilityClass", "PMD.TestClassWithoutTestCases"})
class Test {

	/**
	 * The base package of this library.
	 */
	static final String pack = "io.belin.which";

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException, ParserConfigurationException, SAXException {
		exec("java scripts/Build.java --debug");

		var environment = Map.of("CLASSPATH", getClassPath(Path.of("bin")));
		var pkgPath = Path.of(pack.replace('.', '/'));
		shellExec("javac -d bin -g -Xlint:all,-path,-processing test/%s/*.java".formatted(pkgPath), environment);

		var jacocoJar = "jacocoagent.jar";
		var outputDirectory = Path.of("var");
		extractJacocoJar(jacocoJar, outputDirectory);

		var jacocoExec = outputDirectory.resolve("jacoco.exec");
		var jacocoAgent = "-javaagent:%s=append=false,destfile=%s,includes=%s.*".formatted(outputDirectory.resolve(jacocoJar), jacocoExec, pack);
		var exitCode = exec("java %s org.junit.platform.console.ConsoleLauncher --select-package=%s".formatted(jacocoAgent, pack), environment);
		if (exitCode != 0) System.exit(exitCode);

		generateCoverageReport(jacocoExec, Path.of("bin").resolve(pkgPath));
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
	 * Extracts the JaCoCo Agent archive into the specified output directory.
	 * @param jarName The file name of the JaCoCo Agent archive.
	 * @param outputDirectory The path of the directory where to extract the agent.
	 */
	private static void extractJacocoJar(String jarName, Path outputDirectory)
	throws InterruptedException, IOException, ParserConfigurationException, SAXException {
		var outputFile = Objects.requireNonNull(outputDirectory).resolve(Objects.requireNonNull(jarName));
		if (Files.exists(outputFile)) return;

		var xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("ivy.xml"));
		xml.getDocumentElement().normalize();

		var dependencies = xml.getElementsByTagName("dependency");
		var version = Stream.iterate(0, index -> index < dependencies.getLength(), index -> index + 1)
			.map(index -> (Element) dependencies.item(index))
			.filter(dependency -> dependency.getAttribute("name").equals("org.jacoco.agent"))
			.findFirst()
			.orElseThrow()
			.getAttribute("rev");

		exec("jar --extract --file=lib/org.jacoco/org.jacoco.agent/jars/org.jacoco.agent-%s.jar %s".formatted(version, jarName));
		Files.move(Path.of(jarName), outputFile);
	}

	/**
	 * Generates the coverage report from the specified JaCoCo execution file and class directory.
	 * @param execFile The path of the JaCoCo execution file.
	 * @param classDirectory The path of the directory containing the compiled classes.
	 */
	private static void generateCoverageReport(Path execFile, Path classDirectory) throws InterruptedException, IOException {
		Files.walk(Objects.requireNonNull(classDirectory))
			.filter(file -> file.toString().endsWith("Test.class"))
			.map(Path::toFile)
			.forEach(File::delete);

		exec("java org.jacoco.cli.internal.Main report %s --classfiles=%s --sourcefiles=src --xml=var/coverage.xml".formatted(
			Objects.requireNonNull(execFile),
			classDirectory
		), Map.of("CLASSPATH", getClassPath(Path.of("bin"))));
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

	/**
	 * Executes the specified command in a shell.
	 * @param command The command to execute.
	 * @param environment The optional environment variables to add to the spawned process.
	 * @return The exit code of the executed command.
	 */
	private static int shellExec(String command, Map<String, String> environment) throws InterruptedException, IOException {
		var map = new HashMap<>(System.getenv());
		if (environment != null) map.putAll(environment);

		var shell = System.getProperty("os.name").startsWith("Windows") ? List.of("cmd.exe", "/c") : List.of("/bin/sh", "-c");
		var cmdList = Stream.concat(shell.stream(), Stream.of(Objects.requireNonNull(command)));
		var variables = map.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue());

		var process = Runtime.getRuntime().exec(cmdList.toArray(String[]::new), variables.toArray(String[]::new));
		Stream.concat(process.errorReader().lines(), process.inputReader().lines()).parallel().forEach(System.out::println);
		return process.waitFor();
	}
}
