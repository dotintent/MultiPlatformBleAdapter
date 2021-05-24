package com.polidea.multiplatformbleadapter.utils;


import com.polidea.rxandroidble2.LogConstants;

public class LogLevel {

    @LogConstants.LogLevel
    public static int toLogLevel(String logLevel) {
        switch (logLevel) {
            case Constants.BluetoothLogLevel.VERBOSE:
                return LogConstants.VERBOSE;
            case Constants.BluetoothLogLevel.DEBUG:
                return LogConstants.DEBUG;
            case Constants.BluetoothLogLevel.INFO:
                return LogConstants.INFO;
            case Constants.BluetoothLogLevel.WARNING:
                return LogConstants.WARN;
            case Constants.BluetoothLogLevel.ERROR:
                return LogConstants.ERROR;
            case Constants.BluetoothLogLevel.NONE:
                // fallthrough
            default:
                return LogConstants.NONE;
        }
    }

    @Constants.BluetoothLogLevel
    public static String fromLogLevel(int logLevel) {
        switch (logLevel) {
            case LogConstants.VERBOSE:
                return Constants.BluetoothLogLevel.VERBOSE;
            case LogConstants.DEBUG:
                return Constants.BluetoothLogLevel.DEBUG;
            case LogConstants.INFO:
                return Constants.BluetoothLogLevel.INFO;
            case LogConstants.WARN:
                return Constants.BluetoothLogLevel.WARNING;
            case LogConstants.ERROR:
                return Constants.BluetoothLogLevel.ERROR;
            case LogConstants.NONE:
                // fallthrough
            default:
                return Constants.BluetoothLogLevel.NONE;
        }
    }
}
