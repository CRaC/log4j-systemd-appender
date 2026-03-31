// SPDX-License-Identifier: BSD-3-Clause

// AI Tool Usage BOM
// ------------------
//
// AI Tools Used:
// - Anthropic Claude Sonnet 4.6
//

package org.github.crac.systemd_appender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

final class NativeLoader {

    private NativeLoader() {}

    /**
     * Extracts the named shared library from the JAR resources and loads it via
     * {@link System#load}. The resource path is
     * {@code /native/linux-<arch>[-musl]/lib<name>.so}, where the {@code -musl}
     * suffix is added on musl-based systems (e.g. Alpine Linux).
     *
     * @throws UnsatisfiedLinkError if the library cannot be found or loaded
     */
    static void loadLibrary(String name) {
        String arch = normalizeArch(System.getProperty("os.arch", ""));
        String variant = isMusl(arch) ? "linux-" + arch + "-musl" : "linux-" + arch;
        String resource = "/native/" + variant + "/lib" + name + ".so";

        InputStream in = NativeLoader.class.getResourceAsStream(resource);
        if (in == null) {
            throw new UnsatisfiedLinkError(
                    "Native library not bundled in JAR for this platform: " + resource);
        }

        try (in) {
            Path tmp = Files.createTempFile("lib" + name + "-", ".so");
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().setExecutable(true);
            System.load(tmp.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(
                    "Failed to extract and load native library " + resource + ": " + e.getMessage());
        }
    }

    /**
     * Detects whether the current system uses musl libc by checking for the
     * musl dynamic linker at its standard path {@code /lib/ld-musl-<arch>.so.1}.
     */
    private static boolean isMusl(String arch) {
        return Files.exists(Path.of("/lib/ld-musl-" + arch + ".so.1"));
    }

    private static String normalizeArch(String osArch) {
        return switch (osArch.toLowerCase(Locale.ROOT)) {
            case "amd64", "x86_64"  -> "x86_64";
            case "aarch64", "arm64" -> "aarch64";
            default -> throw new UnsatisfiedLinkError("Unsupported architecture: " + osArch);
        };
    }
}
