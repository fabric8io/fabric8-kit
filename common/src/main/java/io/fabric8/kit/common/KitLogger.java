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
     * Whether debugging is enabled.
     */
    boolean isInfoEnabled();

    /**
     * Whether verbose is enabled
     */
    boolean isVerboseEnabled();

        /**
     * Start a progress bar* @param total the total number to be expected
     */
    void progressStart();

    /**
     * Update the progress
     *
     * @param layerId the image id of the layer fetched
     * @param status a status message
     * @param progressMessage the progressBar
     */
    void progressUpdate(String layerId, String status, String progressMessage);

    /**
     * Finis progress meter. Must be always called if {@link #progressStart()} has been
     * used.
     */
    void progressFinished();

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

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public void progressStart() { }

        @Override
        public void progressUpdate(String id, String status, String progressMessage) { }

        @Override
        public void progressFinished() { }
    }
}
