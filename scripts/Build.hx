/** Builds the project. **/
function main() {
	final debug = Sys.args().contains("--debug");
	final pattern = "io/belin/which/*.java";
	Tools.setClassPath();
	Sys.command('javac -d bin ${debug ? "-g -Xlint:all,-processing" : ""} src/$pattern');
}
