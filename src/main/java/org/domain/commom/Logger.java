package org.domain.commom;

public interface Logger {
	static final int LOG_LEVEL_ERROR   = 0x00000001;
	static final int LOG_LEVEL_WARNING = 0x00000002 | 0x00000001;
	static final int LOG_LEVEL_INFO    = 0x00000004 | 0x00000002 | 0x00000001;
	static final int LOG_LEVEL_TRACE   = 0x00000008 | 0x00000004 | 0x00000002 | 0x00000001;
	static final int LOG_LEVEL_DEBUG   = 0x0000000A | 0x00000008 | 0x00000004 | 0x00000002 | 0x00000001;

	public static String getLogLevelName(int logLevel) {
		String strLogLevel = "";

		if (logLevel == Logger.LOG_LEVEL_DEBUG) {
			strLogLevel = "DEBUG";
		} else if (logLevel == Logger.LOG_LEVEL_INFO) {
			strLogLevel = "INFO";
		} else if (logLevel == Logger.LOG_LEVEL_TRACE) {
			strLogLevel = "TRACE";
		} else if (logLevel == Logger.LOG_LEVEL_WARNING) {
			strLogLevel = "WARNING";
		} else if (logLevel == Logger.LOG_LEVEL_ERROR) {
			strLogLevel = "ERROR";
		}

		return strLogLevel;
	}

	public void log(int logLevel, String header, String text, Object obj);

}
