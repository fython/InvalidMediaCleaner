package moe.feng.media.cleaninvalid.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.ImageColumns;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaItem implements Parcelable {

    public static MediaItem fromCursor(@NonNull Cursor cur) {
        final MediaItem item = new MediaItem();
        int index;
        if ((index = cur.getColumnIndex(ImageColumns._ID)) != -1) {
            item.id = cur.getLong(index);
        }
        if ((index = cur.getColumnIndex(ImageColumns.DATA)) != -1) {
            item.path = cur.getString(index);
        }
        if ((index = cur.getColumnIndex(ImageColumns.DISPLAY_NAME)) != -1) {
            item.displayName = cur.getString(index);
        }
        if ((index = cur.getColumnIndex(ImageColumns.DISPLAY_NAME)) != -1) {
            item.addTime = new Date(cur.getLong(index));
        }
        return item;
    }

    public long id;
    public String path;
    public String displayName;
    public Date addTime;

    // View states
    public boolean isChecked;

    public MediaItem() {

    }

    private MediaItem(Parcel in) {
        id = in.readLong();
        path = in.readString();
        displayName = in.readString();
        long addTimeLong = in.readLong();
        if (addTimeLong > 0) {
            addTime = new Date(addTimeLong);
        }
        isChecked = in.readByte() != 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof MediaItem)) return false;
        final MediaItem other = (MediaItem) obj;
        return other.id == this.id;
    }

    @NonNull
    @Override
    public String toString() {
        return "MediaItem[id=" + id + ", " +
                "path=" + path + ", " +
                "displayName=" + displayName + ", " +
                "addTime=" + addTime + ", " +
                "isChecked=" + isChecked +
                "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(path);
        dest.writeString(displayName);
        dest.writeLong(addTime != null ? addTime.getTime() : 0);
        dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

}
