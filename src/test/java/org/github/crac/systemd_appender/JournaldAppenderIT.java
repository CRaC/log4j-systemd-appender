// SPDX-License-Identifier: BSD-3-Clause

// AI Tool Usage BOM
// ------------------
//
// AI Tools Used:
// - Anthropic Claude Sonnet 4.6
//

package org.github.crac.systemd_appender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test — requires a running systemd journal.
 * Run with: mvn verify -Pintegration-test
 */
class JournaldAppenderIT {

    private static final Logger log = LogManager.getLogger(JournaldAppenderIT.class);

    @Test
    void message_appearsInJournal() throws Exception {
        String marker = "log4j-systemd-it-" + UUID.randomUUID();
        int expectedLine = new Throwable().getStackTrace()[0].getLineNumber() + 1;
        log.info(marker);

        // Give journald a moment to flush
        Thread.sleep(500);

        var result = new ProcessBuilder(
                "journalctl", "--user", "-n", "50", "--no-pager", "-o", "export",
                "SYSLOG_IDENTIFIER=log4j-systemd-it")
                .redirectErrorStream(true)
                .start();
        String output = new String(result.getInputStream().readAllBytes());
        result.waitFor();

        assertTrue(output.contains(marker),
                "Expected marker '" + marker + "' not found in journalctl output:\n" + output);
        assertTrue(output.contains("CODE_FILE=JournaldAppenderIT.java"),
                "Expected CODE_FILE not found in journalctl output:\n" + output);
        assertTrue(output.contains("CODE_LINE=" + expectedLine),
                "Expected CODE_LINE=" + expectedLine + " not found in journalctl output:\n" + output);
    }
}
