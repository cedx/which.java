import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Publishes the package.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Publish {

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException, ParserConfigurationException, SAXException {
		var version = getPackageVersion();
		exec("java scripts/Dist.java");
		for (var action: List.of("tag", "push origin")) exec("git %s v%s".formatted(action, version));
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
	 * Returns the package version of this library.
	 * @return The package version of this library.
	 */
	private static String getPackageVersion() throws IOException, ParserConfigurationException, SAXException {
		var xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("ivy.xml"));
		xml.getDocumentElement().normalize();
		var info = (Element) xml.getElementsByTagName("info").item(0);
		return info.getAttribute("revision");
	}
}
