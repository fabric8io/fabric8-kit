package io.fabric8.kit.common;

/**
 * @author roland
 * @since 30.05.17
 */
public interface KitLogger {

    /**
     * Debug message if debugging is enabled.
     *
     * @param format debug message format
     * @param params parameter for formatting message
     */
    void debug(String format, Object... params);

    /**
     * Informational message
     *
     * @param format info message format
     * @param params parameter for formatting message
     */
    void info(String format, Object... params);

    /**
     * Verbose message
     *
     * @param format verbose message format
     * @param params parameter for formatting message
     */
    void verbose(String format, Object... params);

    /**
     * A warning.
     *
     * @param format warning message format
     * @param params parameter for formatting message
     */
    void warn(String format, Object... params);

    /**
     * Severe errors
     *
     * @param format error message format
     * @param params parameter for formatting message
     */
    void error(String format, Object... params);

    /**
     * Whether debugging is enabled.
     */
    boolean isDebugEnabled();

    /**
     * Whether verbose is enabled
     */
    boolean isVerboseEnabled();

    class StdoutLogger implements KitLogger {
        @Override
        public void debug(String format, Object... params) {
            System.out.println(String.format(format,params));
        }

        @Override
        public void info(String format, Object... params) {
            System.out.println(String.format(format,params));
        }

        @Override
        public void verbose(String format, Object... params) {
            System.out.println(String.format(format,params));
        }

        @Override
        public void warn(String format, Object... params) {
            System.out.println(String.format(format,params));
        }

        @Override
        public void error(String format, Object... params) {
            System.out.println(String.format(format,params));
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isVerboseEnabled() {
            return true;
        }
    }
}
