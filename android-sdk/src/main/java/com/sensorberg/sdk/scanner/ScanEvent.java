package com.sensorberg.sdk.scanner;

import android.os.Parcel;
import android.os.Parcelable;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.utils.Objects;

/**
 * Class {@link ScanEvent} represents an event.
 */
public class ScanEvent implements Parcelable {
    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    public static final Creator<ScanEvent> CREATOR = new Creator<ScanEvent>() {
        public ScanEvent createFromParcel(Parcel in) {
            return (new ScanEvent(in));
        }

        public ScanEvent[] newArray(int size) {
            return (new ScanEvent[size]);
        }
    };
    private String hardwareAdress;

    private int initialRssi;

    private int calRssi;
    private final BeaconId beaconId;
    private final long eventTime;
    private final int eventMask;

    protected ScanEvent(BeaconId beaconId, long eventTime, int eventMask) {
        this.beaconId = beaconId;
        this.eventTime = eventTime;
        this.eventMask = eventMask;
    }

    private ScanEvent(Parcel source) {
        this.beaconId = source.readParcelable(BeaconId.class.getClassLoader());
        this.eventTime = source.readLong();
        this.eventMask = source.readInt();
        this.hardwareAdress = source.readString();
        this.initialRssi = source.readInt();
        this.calRssi = source.readInt();
    }

    public ScanEvent(BeaconId beaconId, long now, int mask, String address, int rssi, int calRssi) {
        this(beaconId, now, mask);
        this.hardwareAdress = address;
        this.initialRssi = rssi;
        this.calRssi = calRssi;
    }

    public int describeContents() {
        return (0);
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(beaconId, flags);
        destination.writeLong(eventTime);
        destination.writeInt(eventMask);
        destination.writeString(hardwareAdress);
        destination.writeInt(initialRssi);
        destination.writeInt(calRssi);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return (true);
        }
        if (object == null) {
            return (false);
        }
        if (((Object) this).getClass() != object.getClass()) {
            return (false);
        }
        ScanEvent other = (ScanEvent) object;
        if (!Objects.equals(beaconId, other.beaconId)){
            return false;
        }
        return eventMask == other.eventMask;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beaconId == null) ? 0 : beaconId.hashCode());
        result = prime * result + eventMask;
        return (result);
    }

    /**
     * Returns the {@link BeaconId} of the {@link ScanEvent}.
     *
     * @return the {@link BeaconId} of the {@link ScanEvent}
     */
    public BeaconId getBeaconId() {
        return (beaconId);
    }

    /**
     * Returns the event mask of the {@link ScanEvent}; see {@link ScanEventType}.
     *
     * @return the event mask of the {@link ScanEvent}
     */
    public int getEventMask() {
        return (eventMask);
    }

    /**
     * Returns the event time in milliseconds of the {@link ScanEvent}.
     *
     * @return the event time in milliseconds of the {@link ScanEvent}
     */
    public long getEventTime() {
        return (eventTime);
    }

    /**
     * The provided rssi provided by the beacon. Corresponds to the rssi of an iPhone 5S in 1 meter distance.
     * This value can be used for disatance calculations
     * @return rssi in db
     */
    public int getCalRssi() {
        return calRssi;
    }

    /**
     * Returns the hardware address of this BluetoothDevice.
     * <p> For example, "00:11:22:AA:BB:CC".
     * @return Bluetooth hardware address as string
     */
    public String getHardwareAdress() {
        return hardwareAdress;
    }

    /**
     * Get the initial RSSI of this event.
     * @return the received signal strength in db
     */

    public int getInitialRssi() {
        return initialRssi;
    }


    public static class Builder {
        private BeaconId beaconId;
        private long eventTime;
        private int eventMask;

        public Builder() {
        }

        public Builder withBeaconId(BeaconId beaconId) {
            this.beaconId = beaconId;
            return this;
        }

        public Builder withEventTime(long eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder withEventMask(int eventMask) {
            this.eventMask = eventMask;
            return this;
        }

        public ScanEvent build() {
            return new ScanEvent(beaconId, eventTime, eventMask);
        }
    }

    @Override
    public String toString() {
        return "ScanEvent{" +
                "hardwareAdress='" + hardwareAdress + '\'' +
                ", initialRssi=" + initialRssi +
                ", calRssi=" + calRssi +
                ", beaconId=" + beaconId +
                ", eventTime=" + eventTime +
                ", eventMask=" + eventMask +
                '}';
    }
}
