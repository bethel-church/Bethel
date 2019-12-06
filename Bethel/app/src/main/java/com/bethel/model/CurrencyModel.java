package com.bethel.model;

import java.util.List;

/**
 * Created by kuljeetsingh on 10/2/16.
 */

public class CurrencyModel {
    /**
     * status : success
     * trips : [{"Currency":{"id":"42","currency":"AED","trip_id":"74","created":"2016-09-21 01:05:10"}},{"Currency":{"id":"41","currency":"BAM","trip_id":"74","created":"2016-09-21 01:05:10"}},{"Currency":{"id":"40","currency":"ANG","trip_id":"74","created":"2016-09-21 01:05:10"}},{"Currency":{"id":"43","currency":"ALL","trip_id":"74","created":"2016-09-21 01:05:10"}}]
     */

    private String status;
    /**
     * Currency : {"id":"42","currency":"AED","trip_id":"74","created":"2016-09-21 01:05:10"}
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

    public static class TripsEntity {
        /**
         * id : 42
         * currency : AED
         * trip_id : 74
         * created : 2016-09-21 01:05:10
         */

        private CurrencyEntity Currency;

        public void setCurrency(CurrencyEntity Currency) {
            this.Currency = Currency;
        }

        public CurrencyEntity getCurrency() {
            return Currency;
        }

        public static class CurrencyEntity {
            private String id;
            private String currency;
            private String trip_id;
            private String created;

            public void setId(String id) {
                this.id = id;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public void setTrip_id(String trip_id) {
                this.trip_id = trip_id;
            }

            public void setCreated(String created) {
                this.created = created;
            }

            public String getId() {
                return id;
            }

            public String getCurrency() {
                return currency;
            }

            public String getTrip_id() {
                return trip_id;
            }

            public String getCreated() {
                return created;
            }
        }
    }
}
