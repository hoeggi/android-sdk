package com.sensorberg.sdk.scanner;

/**
 * Enumeration {@link ScanEventType} enumerates the various event types.
 */
public enum ScanEventType {
    /**
     * Constant indicating the entry event; this is triggered when a device enters the proximity of a beacon
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    ENTRY(1 << 0),
    /**
     * Constant indicating the exit event; this is triggered when a device exits the proximity of a beacon
     */
    EXIT(1 << 1);

    private final int mask;

    ScanEventType(int mask) {
        this.mask = mask;
    }

    /**
     * Adds a new {@link ScanEventType} to an event mask.
     *
     * @param eventMask the event mask to add to
     * @param eventType the {@link ScanEventType} to be added
     * @return the original event mask with the {@link ScanEventType} added
     */
    public static int addToMask(int eventMask, ScanEventType eventType) {
        return (eventMask | eventType.mask);
    }

    /**
     * Return the mask value of the {@link ScanEventType}.
     *
     * @return the mask value of the {@link ScanEventType}
     */
    public int getMask() {
        return (mask);
    }
}
