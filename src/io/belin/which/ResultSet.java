package io.belin.which;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides convenient access to the stream of search results.
 */
public final class ResultSet {

	/**
	 * The searched command.
	 */
	private final String command;

	/**
	 * The finder used to perform the search.
	 */
	private final Finder finder;

	/**
	 * Creates a new result set.
	 * @param command The searched command.
	 * @param Finder finder The finder used to perform the search.
	 */
	ResultSet(String command, Finder finder) {
		this.command = command;
		this.finder = finder;
	}

	/**
	 * Returns all instances of the searched command.
	 * @return All search results.
	 * @throws \RuntimeException The command has not been found.
	 */
	public Optional<List<Path>> all() {
		var executables = stream().distinct().toList();
		return executables.isEmpty() ? Optional.empty() : Optional.of(executables);
	}

	/**
	 * Returns the first instance of the searched command.
	 * @return The first search result.
	 * @throws \RuntimeException The command has not been found.
	 */
	public Optional<Path> first() {
		return stream().findFirst();
	}

	/**
	 * Returns a stream of instances of the searched command.
	 * @return A stream of the search results.
	 */
	public Stream<Path> stream() {
		return finder.find(command);
	}
}
