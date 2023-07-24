import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Updates the version number in the sources.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Version {

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws IOException, ParserConfigurationException, SAXException {
		var version = getPackageVersion();
		replaceInFile(Path.of("README.md"), Pattern.compile("project/v\\d+(\\.\\d+){2}"), "project/v" + version);
		replaceInFile(Path.of("etc/manifest.properties"), Pattern.compile("Version: \\d+(\\.\\d+){2}"), "Version: " + version);
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

	/**
	 * Replaces in the specified file the substring which the pattern matches with the given replacement.
	 * @param file The path of the file to process.
	 * @param pattern The pattern to search for.
	 * @param replacement The string to replace.
	 */
	private static void replaceInFile(Path file, Pattern pattern, String replacement) throws IOException {
		Files.writeString(file, pattern.matcher(Files.readString(file)).replaceAll(replacement));
	}
}
