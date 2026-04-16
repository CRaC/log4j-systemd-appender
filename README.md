<!-- SPDX-License-Identifier: BSD-3-Clause -->
<!--
    AI Tool Usage BOM
    - - - - - - - - -

    AI Tools Used:
    - Anthropic Claude Sonnet 4.6
-->

# log4j-systemd-appender

A Log4j 2 appender that sends structured log events directly to the systemd journal
via Unix datagram socket, using the native [journal protocol].
No `libsystemd` dependency.

[journal protocol]: https://systemd.io/JOURNAL_NATIVE_PROTOCOL/

## Features

- Structured fields: `PRIORITY`, `SYSLOG_IDENTIFIER`, `THREAD_NAME`, `LOG4J_LOGGER`, `LOG4J_APPENDER`, `CODE_FILE/LINE/FUNC`, `STACKTRACE`, `SYSLOG_FACILITY`
- Optional ThreadContext (MDC) forwarding with configurable key prefix
- Message truncation when the datagram would exceed `maxMessageSize`
- Uses junixsocket by default for `AF_UNIX` datagram transport on all supported JDKs

## Requirements

- Java 21 or later
- Linux with systemd (the journal socket at `/run/systemd/journal/socket`)

## Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="org.github.crac.systemd_appender">
    <Appenders>
        <SystemdJournal name="journal"
                 syslogIdentifier="my-app"
                 syslogFacility="3"
                 logSource="false"
                 logStacktrace="true"
                 logThreadName="true"
                 logLoggerName="true"
                 logLoggerAppName="MYAPP"
                 logAppenderName="true"
                 logThreadContext="true"
                 threadContextPrefix="THREAD_CONTEXT_"
                 maxMessageSize="65536"/>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="journal"/>
        </Root>
    </Loggers>
</Configuration>
```

### Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `syslogIdentifier` | String | process name | Maps to `SYSLOG_IDENTIFIER`. Defaults to the executable name derived from `ProcessHandle`. |
| `syslogFacility` | int | *(unset)* | Maps to `SYSLOG_FACILITY`. Expects a numeric syslog facility code. Omitted when not set. |
| `logSource` | boolean | `false` | Log source location fields `CODE_FILE`, `CODE_LINE`, `CODE_FUNC`, `JAVA_CLASSNAME`. Has a performance cost; requires `includeLocation` on the logger. |
| `logStacktrace` | boolean | `true` | Log the full exception stacktrace to the `STACKTRACE` field. |
| `logThreadName` | boolean | `true` | Log the thread name to `THREAD_NAME`. |
| `logLoggerName` | boolean | `true` | Log the logger name. Field is `LOG4J_LOGGER` by default, or `{logLoggerAppName}_LOGGER` when `logLoggerAppName` is set. |
| `logLoggerAppName` | String | *(unset)* | When set, changes the logger-name field from `LOG4J_LOGGER` to `{logLoggerAppName}_LOGGER`. |
| `logAppenderName` | boolean | `true` | Log the appender name to `LOG4J_APPENDER`. |
| `logThreadContext` | boolean | `true` | Forward ThreadContext (MDC) entries as `THREAD_CONTEXT_*` fields. Keys are uppercased and non-alphanumeric characters replaced with `_`. |
| `threadContextPrefix` | String | `THREAD_CONTEXT_` | Prefix applied to ThreadContext keys. |
| `maxMessageSize` | int | `65536` | Maximum datagram size in bytes. Messages that would exceed this limit are truncated and suffixed with `[TRUNCATED]`. |
