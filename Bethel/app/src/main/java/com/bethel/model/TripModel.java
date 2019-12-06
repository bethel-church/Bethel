package com.bethel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by kuljeetsingh on 9/30/16.
 */

public class TripModel implements Parcelable {
    /**
     * status : success
     * trips : [{"Trip":{"id":"74","name":"A-D Trip Test 2","budget":"5000","created":"2016-09-20 15:58:36"}},{"Trip":{"id":"60","name":"Denver, Colorado Trip 2016 (Angelo)","budget":"1376.68","created":"2016-06-14 14:23:55"}},{"Trip":{"id":"63","name":"Japan Summer Trip","budget":"6699.84","created":"2016-07-08 20:47:45"}},{"Trip":{"id":"5","name":"Test Trip","budget":"24000","created":"2016-03-17 22:28:53"}}]
     */

    private String status;
    /**
     * Trip : {"id":"74","name":"A-D Trip Test 2","budget":"5000","created":"2016-09-20 15:58:36"}
     */

    private List<TripsEntity> trips;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTrips(List<TripsEntity> trips) {
        this.trips = trips;
    }

    public String getStatus() {
        return status;
    }

    public List<TripsEntity> getTrips() {
        return trips;
    }

    public static class TripsEntity implements Parcelable {
        /**
         * id : 74
         * name : A-D Trip Test 2
         * budget : 5000
         * created : 2016-09-20 15:58:36
         */

        private TripEntity Trip;

        public void setTrip(TripEntity Trip) {
            this.Trip = Trip;
        }

        public TripEntity getTrip() {
            return Trip;
        }

        public static class TripEntity implements Parcelable {
            private String id;
            private String name;
            private String budget;
            private String created;
            private boolean isChecked=false;

            public boolean isChecked() {
                return isChecked;
            }

            public void setChecked(boolean checked) {
                isChecked = checked;
            }

            public void setId(String id) {
                this.id = id;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setBudget(String budget) {
                this.budget = budget;
            }

            public void setCreated(String created) {
                this.created = created;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getBudget() {
                return budget;
            }

            public String getCreated() {
                return created;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.id);
                dest.writeString(this.name);
                dest.writeString(this.budget);
                dest.writeString(this.created);
                dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
            }

            public TripEntity() {
            }

            protected TripEntity(Parcel in) {
                this.id = in.readString();
                this.name = in.readString();
                this.budget = in.readString();
                this.created = in.readString();
                this.isChecked = in.readByte() != 0;
            }

            public static final Creator<TripEntity> CREATOR = new Creator<TripEntity>() {
                public TripEntity createFromParcel(Parcel source) {
                    return new TripEntity(source);
                }

                public TripEntity[] newArray(int size) {
                    return new TripEntity[size];
                }
            };
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.Trip, 0);
        }

        public TripsEntity() {
        }

        protected TripsEntity(Parcel in) {
            this.Trip = in.readParcelable(TripEntity.class.getClassLoader());
        }

        public static final Creator<TripsEntity> CREATOR = new Creator<TripsEntity>() {
            public TripsEntity createFromParcel(Parcel source) {
                return new TripsEntity(source);
            }

            public TripsEntity[] newArray(int size) {
                return new TripsEntity[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.status);
        dest.writeTypedList(trips);
    }

    public TripModel() {
    }

    protected TripModel(Parcel in) {
        this.status = in.readString();
        this.trips = in.createTypedArrayList(TripsEntity.CREATOR);
    }

    public static final Creator<TripModel> CREATOR = new Creator<TripModel>() {
        public TripModel createFromParcel(Parcel source) {
            return new TripModel(source);
        }

        public TripModel[] newArray(int size) {
            return new TripModel[size];
        }
    };
}
