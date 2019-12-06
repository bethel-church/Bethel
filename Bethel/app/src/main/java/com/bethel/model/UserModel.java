package com.bethel.model;

import android.os.Parcel;;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kuljeetsingh on 10/1/16.
 */

public class UserModel implements Serializable {
    /**
     * status : success
     * trips : {"Trip":{"id":"74","name":"A-D Trip Test 2","budget":"5000","created":"2016-09-20 15:58:36"},"User":[{"id":"5018","first_name":"Vinod","middle_name":"Kumar","last_name":"Sharma","type":"1","created":"2016-09-20 15:58:36","trip_id":"74"},{"id":"5019","first_name":"Dina","middle_name":"Nath","last_name":"Chauhan","type":"1","created":"2016-09-20 15:58:36","trip_id":"74"}]}
     */

    private String status;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Trip : {"id":"74","name":"A-D Trip Test 2","budget":"5000","created":"2016-09-20 15:58:36"}
     * User : [{"id":"5018","first_name":"Vinod","middle_name":"Kumar","last_name":"Sharma","type":"1","created":"2016-09-20 15:58:36","trip_id":"74"},{"id":"5019","first_name":"Dina","middle_name":"Nath","last_name":"Chauhan","type":"1","created":"2016-09-20 15:58:36","trip_id":"74"}]
     */

    private TripsEntity trips;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTrips(TripsEntity trips) {
        this.trips = trips;
    }

    public String getStatus() {
        return status;
    }

    public TripsEntity getTrips() {
        return trips;
    }

    public static class TripsEntity implements Serializable {
        /**
         * id : 74
         * name : A-D Trip Test 2
         * budget : 5000
         * created : 2016-09-20 15:58:36
         */

        private TripEntity Trip;
        /**
         * id : 5018
         * first_name : Vinod
         * middle_name : Kumar
         * last_name : Sharma
         * type : 1
         * created : 2016-09-20 15:58:36
         * trip_id : 74
         */

        private List<UserEntity> User;

        public void setTrip(TripEntity Trip) {
            this.Trip = Trip;
        }

        public void setUser(List<UserEntity> User) {
            this.User = User;
        }

        public TripEntity getTrip() {
            return Trip;
        }

        public List<UserEntity> getUser() {
            return User;
        }

        public static class TripEntity implements Serializable {
            private String id;
            private String name;
            private String budget;
            private String created;

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

//            @Override
//            public int describeContents() {
//                return 0;
//            }
//
//            @Override
//            public void writeToParcel(Parcel dest, int flags) {
//                dest.writeString(this.id);
//                dest.writeString(this.name);
//                dest.writeString(this.budget);
//                dest.writeString(this.created);
//            }

            public TripEntity() {
            }

            protected TripEntity(Parcel in) {
                this.id = in.readString();
                this.name = in.readString();
                this.budget = in.readString();
                this.created = in.readString();
            }

//            public static final Serializable.Creator<TripEntity> CREATOR = new Serializable.Creator<TripEntity>() {
//                public TripEntity createFromParcel(Parcel source) {
//                    return new TripEntity(source);
//                }
//
//                public TripEntity[] newArray(int size) {
//                    return new TripEntity[size];
//                }
//            };
        }

        public static class UserEntity implements Serializable {
            private String id;
            private String first_name;
            private String middle_name;
            private String last_name;
            private String type;
            private String created;
            private String trip_id;
            private boolean isChecked = false;

            public boolean isChecked() {
                return isChecked;
            }

            public void setChecked(boolean checked) {
                isChecked = checked;
            }

            public void setId(String id) {
                this.id = id;
            }

            public void setFirst_name(String first_name) {
                this.first_name = first_name;
            }

            public void setMiddle_name(String middle_name) {
                this.middle_name = middle_name;
            }

            public void setLast_name(String last_name) {
                this.last_name = last_name;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setCreated(String created) {
                this.created = created;
            }

            public void setTrip_id(String trip_id) {
                this.trip_id = trip_id;
            }

            public String getId() {
                return id;
            }

            public String getFirst_name() {
                return first_name;
            }

            public String getMiddle_name() {
                return middle_name;
            }

            public String getLast_name() {
                return last_name;
            }

            public String getType() {
                return type;
            }

            public String getCreated() {
                return created;
            }

            public String getTrip_id() {
                return trip_id;
            }
//
//            @Override
//            public int describeContents() {
//                return 0;
//            }
//
//            @Override
//            public void writeToParcel(Parcel dest, int flags) {
//                dest.writeString(this.id);
//                dest.writeString(this.first_name);
//                dest.writeString(this.middle_name);
//                dest.writeString(this.last_name);
//                dest.writeString(this.type);
//                dest.writeString(this.created);
//                dest.writeString(this.trip_id);
//                dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
//            }

            public UserEntity() {
            }

            protected UserEntity(Parcel in) {
                this.id = in.readString();
                this.first_name = in.readString();
                this.middle_name = in.readString();
                this.last_name = in.readString();
                this.type = in.readString();
                this.created = in.readString();
                this.trip_id = in.readString();
                this.isChecked = in.readByte() != 0;
            }

//            public static final Serializable.Creator<UserEntity> CREATOR = new Serializable.Creator<UserEntity>() {
//                public UserEntity createFromParcel(Parcel source) {
//                    return new UserEntity(source);
//                }
//
//                public UserEntity[] newArray(int size) {
//                    return new UserEntity[size];
//                }
//            };
        }

//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeSerializable(this.Trip, 0);
//            dest.writeTypedList(User);
//        }

        public TripsEntity() {
        }

//        protected TripsEntity(Parcel in) {
//            this.Trip = in.readSerializable(TripEntity.class.getClassLoader());
//            this.User = in.createTypedArrayList(UserEntity.CREATOR);
//        }
//
//        public static final Serializable.Creator<TripsEntity> CREATOR = new Serializable.Creator<TripsEntity>() {
//            public TripsEntity createFromParcel(Parcel source) {
//                return new TripsEntity(source);
//            }
//
//            public TripsEntity[] newArray(int size) {
//                return new TripsEntity[size];
//            }
//        };
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.status);
//        dest.writeString(this.message);
//        dest.writeSerializable(this.trips, 0);
//    }

    public UserModel() {
    }

    protected UserModel(Parcel in) {
        this.status = in.readString();
        this.message = in.readString();
//        this.trips = in.readSerializable(TripsEntity.class.getClassLoader());
    }

//    public static final Serializable.Creator<UserModel> CREATOR = new Serializable.Creator<UserModel>() {
//        public UserModel createFromParcel(Parcel source) {
//            return new UserModel(source);
//        }
//
//        public UserModel[] newArray(int size) {
//            return new UserModel[size];
//        }
//    };
}
