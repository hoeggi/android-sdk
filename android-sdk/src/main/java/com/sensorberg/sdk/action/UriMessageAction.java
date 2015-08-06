package com.sensorberg.sdk.action;

import android.os.Parcel;

import java.util.UUID;

/**
 * Class {@link UriMessageAction} extends {@link Action} for holding title, content, and a URI.
 */
public class UriMessageAction extends Action {
    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    @SuppressWarnings("hiding")
    public static final Creator<UriMessageAction> CREATOR = new Creator<UriMessageAction>() {
        public UriMessageAction createFromParcel(Parcel in) {
            return (new UriMessageAction(in));
        }

        public UriMessageAction[] newArray(int size) {
            return (new UriMessageAction[size]);
        }
    };
    private final String title;
    private final String content;
    private final String uri;

    /**
     * Creates and initializes a new {@link UriMessageAction}.
     * @param actionUUID
     * @param title   the title of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param content the message of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param uri     the URI of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param payload
     * @param delayTime
     */
    public UriMessageAction(UUID actionUUID, String title, String content, String uri, String payload, long delayTime) {
        super(ActionType.MESSAGE_URI, delayTime, actionUUID, payload);
        this.title = title;
        this.content = content;
        this.uri = uri;
    }

    protected UriMessageAction(Parcel source) {
        super(source);
        this.title = source.readString();
        this.content = source.readString();
        this.uri = source.readString();
    }

    /**
     * Returns the URI of the {@link UriMessageAction}.
     *
     * @return the URI of the {@link UriMessageAction}
     */
    public String getUri() {
        return (uri);
    }

    /**
     * Returns the content
     *
     * @return the content
     */
    public String getContent() {
        return (content);
    }

    /**
     * Returns the title
     *
     * @return the title
     */
    public String getTitle() {
        return (title);
    }

    /**
     * Returns a hash code bases on the actual contents.
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return (title.hashCode() + content.hashCode() + uri.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        UriMessageAction that = (UriMessageAction) o;

        if (!content.equals(that.content)) return false;
        if (!title.equals(that.title)) return false;
        if (!uri.equals(that.uri)) return false;

        return true;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        super.writeToParcel(destination, flags);
        destination.writeString(title);
        destination.writeString(content);
        destination.writeString(uri);
    }
}
