package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads and writes files strictly beneath a fixed root directory.
 *
 * <p>Containment is bound to the operation rather than to a pathname. Validating a path and then
 * opening it is only a snapshot: the entry can be replaced with a symbolic link in between, and
 * both {@link Files#write} and {@link Files#readAllBytes} follow links. Instead, every directory in
 * the relative path is opened through its parent's descriptor with {@link
 * LinkOption#NOFOLLOW_LINKS} (a {@link SecureDirectoryStream}), and the final entry is opened the
 * same way, so the kernel — not an earlier check — rejects a link at the moment of use.
 *
 * <p>The rule is uniform: nothing inside local storage is reached through a symbolic link, whether
 * it points outside the root or back into it. Stored files and their sidecars are written only by
 * this application, so a link anywhere in the tree was planted by something else.
 *
 * <h2>Platforms without descriptor-relative traversal</h2>
 *
 * <p>{@link SecureDirectoryStream} is optional; the Linux and macOS default providers supply it,
 * Windows does not. Where it is unavailable this falls back to pathname operations that still apply
 * the no-follow policy to every component, which closes the same holes for a link that is already
 * in place but leaves a narrow window in which an ancestor swapped mid-operation could be followed.
 * The fallback is logged once at startup.
 *
 * <h2>Hard links: outside the guarantee</h2>
 *
 * <p>A hard link is an ordinary directory entry, indistinguishable from the file it names, so
 * neither no-follow opens nor real-path resolution can tell that the inode also has a name outside
 * the root. Someone able to create a hard link inside the storage directory — which requires local
 * write access there and a target on the same filesystem — can therefore expose that file's
 * contents through this adapter. Rejecting entries with a link count above one was considered and
 * dropped: snapshot and dedup tooling (for example {@code rsync --link-dest}) raises the count on
 * legitimate files and would break every download. Local storage is a development provider; the
 * containment guarantee here covers traversal and symbolic links and assumes nothing hostile can
 * write into the storage tree.
 */
@Slf4j
final class ContainedFileTree {

    /** Signals that an entry is not storage-owned: it is, or became, a symbolic link. */
    static final class ContainmentException extends IOException {
        ContainmentException(String message) {
            super(message);
        }
    }

    private static final Set<OpenOption> READ = Set.of(StandardOpenOption.READ);

    private static final Set<OpenOption> REPLACE =
            Set.of(
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

    /** The root with symbolic links resolved; every traversal starts from this directory. */
    private final Path root;

    private final boolean descriptorRelative;

    ContainedFileTree(Path root) throws IOException {
        this.root = root.toRealPath();
        this.descriptorRelative = supportsDescriptorRelativeTraversal(this.root);

        if (!descriptorRelative) {
            log.warn(
                    "Descriptor-relative directory traversal is unavailable at {}; local storage"
                            + " falls back to pathname operations with a no-follow policy",
                    this.root);
        }
    }

    byte[] readAllBytes(Path relative) throws IOException {
        try (Cursor cursor = openParent(relative, false)) {
            String name = fileName(relative);
            rejectSymbolicLink(cursor, name, true);

            try (SeekableByteChannel channel = open(cursor, name, READ)) {
                return Channels.newInputStream(channel).readAllBytes();
            }
        }
    }

    void write(Path relative, byte[] content) throws IOException {
        try (Cursor cursor = openParent(relative, true)) {
            String name = fileName(relative);
            rejectSymbolicLink(cursor, name, false);

            try (SeekableByteChannel channel = open(cursor, name, REPLACE)) {
                channel.write(ByteBuffer.wrap(content));
            }
        }
    }

    /**
     * Removes the entry if it exists. A symbolic link is refused rather than unlinked: this adapter
     * never creates one, so removing it would quietly clean up someone else's plant and hide that
     * the tree was tampered with.
     */
    boolean deleteIfExists(Path relative) throws IOException {
        try (Cursor cursor = openParent(relative, false)) {
            String name = fileName(relative);
            if (!rejectSymbolicLink(cursor, name, false)) {
                return false;
            }

            cursor.delete(name);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    /** Attributes of the entry itself, never of a symbolic link's target. */
    BasicFileAttributes readAttributes(Path relative) throws IOException {
        try (Cursor cursor = openParent(relative, false)) {
            String name = fileName(relative);
            rejectSymbolicLink(cursor, name, true);
            return cursor.attributes(name);
        }
    }

    /** Whether a storage-owned entry exists; a symbolic link counts as absent, not present. */
    boolean exists(Path relative) {
        try {
            readAttributes(relative);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Rejects an entry that is currently a symbolic link, reporting whether it exists at all. This
     * is a classification step, not the guard: the no-follow open that follows is what actually
     * prevents a link from being traversed, including one planted after this check.
     */
    private boolean rejectSymbolicLink(Cursor cursor, String name, boolean requireExisting)
            throws IOException {
        BasicFileAttributes attributes;
        try {
            attributes = cursor.attributes(name);
        } catch (NoSuchFileException e) {
            if (requireExisting) {
                throw e;
            }
            return false;
        }

        if (attributes.isSymbolicLink()) {
            throw new ContainmentException(
                    "Refusing to follow a symbolic link inside local storage: " + name);
        }
        return true;
    }

    private SeekableByteChannel open(Cursor cursor, String name, Set<OpenOption> options)
            throws IOException {
        try {
            return cursor.open(name, noFollow(options));
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            // A no-follow open of a symbolic link lands here — the case this whole class exists
            // for, an entry swapped after it was classified. The JDK reports it as a plain
            // IOException (ELOOP) with no distinguishable type, so every failure that is not
            // "missing" is treated as a containment violation: this adapter opens only files it
            // wrote itself, and failing closed is the safe direction for the ones it did not.
            throw new ContainmentException(
                    "Refusing to open an entry that changed during the operation: " + name);
        }
    }

    /** Opens the directory that immediately contains {@code relative}, creating it when asked. */
    private Cursor openParent(Path relative, boolean createMissing) throws IOException {
        Path parent = relative.getParent();

        if (!descriptorRelative) {
            return new PathCursor(walkByPathname(parent, createMissing));
        }

        SecureDirectoryStream<Path> stream = openRoot();
        try {
            Path absolute = root;
            for (Path component : componentsOf(parent)) {
                absolute = absolute.resolve(component.toString());
                SecureDirectoryStream<Path> child =
                        openDirectory(stream, component.toString(), absolute, createMissing);
                stream.close();
                stream = child;
            }
            return new SecureCursor(stream);
        } catch (IOException e) {
            stream.close();
            throw e;
        }
    }

    /**
     * Descends one level through the parent's descriptor. Missing directories are created by
     * pathname because Java exposes no descriptor-relative {@code mkdir}; the result is immediately
     * re-opened through the parent descriptor with no-follow, so a raced ancestor can at worst
     * leave a stray empty directory and never divert a file read or write out of the root.
     */
    private SecureDirectoryStream<Path> openDirectory(
            SecureDirectoryStream<Path> parent, String name, Path absolute, boolean createMissing)
            throws IOException {
        try {
            return openChild(parent, name);
        } catch (NoSuchFileException e) {
            if (!createMissing) {
                throw e;
            }
        }

        try {
            Files.createDirectory(absolute);
        } catch (FileAlreadyExistsException e) {
            // Lost a benign race with a concurrent upload into the same directory.
        }
        return openChild(parent, name);
    }

    private SecureDirectoryStream<Path> openChild(SecureDirectoryStream<Path> parent, String name)
            throws IOException {
        BasicFileAttributes attributes =
                parent.getFileAttributeView(
                                Path.of(name),
                                BasicFileAttributeView.class,
                                LinkOption.NOFOLLOW_LINKS)
                        .readAttributes();

        if (attributes.isSymbolicLink()) {
            throw new ContainmentException(
                    "Refusing to traverse a symbolic link inside local storage: " + name);
        }
        if (!attributes.isDirectory()) {
            throw new NotDirectoryException(name);
        }

        try {
            return parent.newDirectoryStream(Path.of(name), LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            // Same reasoning as open(): a link swapped in after the check fails here untyped.
            throw new ContainmentException(
                    "Refusing to traverse an entry that changed during the operation: " + name);
        }
    }

    /** Fallback traversal: same no-follow policy, applied to pathnames instead of descriptors. */
    private Path walkByPathname(Path parent, boolean createMissing) throws IOException {
        Path directory = root;
        for (Path component : componentsOf(parent)) {
            directory = directory.resolve(component.toString());

            if (createMissing) {
                try {
                    Files.createDirectory(directory);
                } catch (FileAlreadyExistsException e) {
                    // Already there, or lost a benign race; validated just below either way.
                }
            }

            BasicFileAttributes attributes =
                    Files.readAttributes(
                            directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isSymbolicLink()) {
                throw new ContainmentException(
                        "Refusing to traverse a symbolic link inside local storage: " + directory);
            }
            if (!attributes.isDirectory()) {
                throw new NotDirectoryException(directory.toString());
            }
        }
        return directory;
    }

    private SecureDirectoryStream<Path> openRoot() throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(root);
        if (stream instanceof SecureDirectoryStream<Path> secure) {
            return secure;
        }
        stream.close();
        throw new IOException("Storage root no longer supports descriptor-relative traversal");
    }

    private static boolean supportsDescriptorRelativeTraversal(Path root) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            return stream instanceof SecureDirectoryStream;
        } catch (IOException e) {
            return false;
        }
    }

    private static Iterable<Path> componentsOf(Path parent) {
        return parent == null ? Set.of() : parent;
    }

    private static String fileName(Path relative) {
        Path name = relative.getFileName();
        if (name == null) {
            throw new IllegalArgumentException("Path has no file name: " + relative);
        }
        return name.toString();
    }

    private static Set<OpenOption> noFollow(Set<OpenOption> options) {
        Set<OpenOption> withNoFollow = new HashSet<>(options);
        withNoFollow.add(LinkOption.NOFOLLOW_LINKS);
        return withNoFollow;
    }

    /** A handle on the directory that immediately contains the entry being operated on. */
    private interface Cursor extends Closeable {

        SeekableByteChannel open(String name, Set<OpenOption> options) throws IOException;

        BasicFileAttributes attributes(String name) throws IOException;

        void delete(String name) throws IOException;
    }

    /** Descriptor-relative: operations cannot be redirected by anything above the directory. */
    private record SecureCursor(SecureDirectoryStream<Path> stream) implements Cursor {

        @Override
        public SeekableByteChannel open(String name, Set<OpenOption> options) throws IOException {
            return stream.newByteChannel(Path.of(name), options);
        }

        @Override
        public BasicFileAttributes attributes(String name) throws IOException {
            return stream.getFileAttributeView(
                            Path.of(name), BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)
                    .readAttributes();
        }

        @Override
        public void delete(String name) throws IOException {
            stream.deleteFile(Path.of(name));
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    /** Pathname fallback for providers without {@link SecureDirectoryStream}. */
    private record PathCursor(Path directory) implements Cursor {

        @Override
        public SeekableByteChannel open(String name, Set<OpenOption> options) throws IOException {
            return Files.newByteChannel(directory.resolve(name), options);
        }

        @Override
        public BasicFileAttributes attributes(String name) throws IOException {
            return Files.readAttributes(
                    directory.resolve(name), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }

        @Override
        public void delete(String name) throws IOException {
            Files.delete(directory.resolve(name));
        }

        @Override
        public void close() {
            // Nothing is held open.
        }
    }
}
