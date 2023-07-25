import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
 * Builds the project.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Build {

	/**
	 * The base package of this library.
	 */
	static final String pack = "io.belin.which";

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException, ParserConfigurationException, SAXException {
		var options = Arrays.asList(args).contains("--debug") ? "-g -Xlint:all,-path,-processing" : "-g:none";
		extractPicocliSource(Path.of("picocli/CommandLine.java"), Path.of("src"));
		shellExec("javac -d bin %s src/%s/*.java".formatted(options, pack.replace('.', '/')), Map.of("CLASSPATH", getClassPath()));
	}

	/**
	 * Executes the specified command.
	 * @param command The command to execute.
	 * @return The exit code of the executed command.
	 */
	private static int exec(String command) throws InterruptedException, IOException {
		var process = Runtime.getRuntime().exec(Objects.requireNonNull(command));
		Stream.concat(process.errorReader().lines(), process.inputReader().lines()).parallel().forEach(System.out::println);
		return process.waitFor();
	}

	/**
	 * Extracts the Picocli source into the specified output directory.
	 * @param sourceFile The path of the Picocli source file.
	 * @param outputDirectory The path of the directory where to extract the source.
	 */
	private static void extractPicocliSource(Path sourceFile, Path outputDirectory)
	throws InterruptedException, IOException, ParserConfigurationException, SAXException {
		var outputFile = Objects.requireNonNull(outputDirectory).resolve(Objects.requireNonNull(sourceFile));
		if (Files.exists(outputFile)) return;

		var xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("ivy.xml"));
		xml.getDocumentElement().normalize();

		var dependencies = xml.getElementsByTagName("dependency");
		var version = Stream.iterate(0, index -> index < dependencies.getLength(), index -> index + 1)
			.map(index -> (Element) dependencies.item(index))
			.filter(dependency -> dependency.getAttribute("name").equals("picocli"))
			.findFirst()
			.orElseThrow()
			.getAttribute("rev");

		exec("jar --extract --file=lib/info.picocli/picocli/sources/picocli-%s-sources.jar %s".formatted(version, sourceFile));
		Files.move(sourceFile.getParent(), outputFile.getParent());
	}

	/**
	 * Returns the class path.
	 * @return The class path.
	 */
	private static String getClassPath() throws IOException {
		return Files.readString(Path.of(".classpath")).stripTrailing();
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
