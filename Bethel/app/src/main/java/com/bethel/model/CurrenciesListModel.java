package com.bethel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by kuljeetsingh on 10/2/16.
 */

public class CurrenciesListModel implements Parcelable {


    private ListEntity list;

    public void setList(ListEntity list) {
        this.list = list;
    }

    public ListEntity getList() {
        return list;
    }

    public static class ListEntity implements Parcelable {
        /**
         * type : resource-list
         * start : 0
         * count : 174
         */

        private MetaEntity meta;
        /**
         * resource : {"classname":"Quote","fields":{"name":"USD/KRW","price":"1098.300049","symbol":"KRW=X","ts":"1475269956","type":"currency","utctime":"2016-09-30T21:12:36+0000","volume":"0"}}
         */

        private List<ResourcesEntity> resources;

        public void setMeta(MetaEntity meta) {
            this.meta = meta;
        }

        public void setResources(List<ResourcesEntity> resources) {
            this.resources = resources;
        }

        public MetaEntity getMeta() {
            return meta;
        }

        public List<ResourcesEntity> getResources() {
            return resources;
        }

        public static class MetaEntity implements Parcelable {
            private String type;
            private int start;
            private int count;

            public void setType(String type) {
                this.type = type;
            }

            public void setStart(int start) {
                this.start = start;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public String getType() {
                return type;
            }

            public int getStart() {
                return start;
            }

            public int getCount() {
                return count;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.type);
                dest.writeInt(this.start);
                dest.writeInt(this.count);
            }

            public MetaEntity() {
            }

            protected MetaEntity(Parcel in) {
                this.type = in.readString();
                this.start = in.readInt();
                this.count = in.readInt();
            }

            public static final Creator<MetaEntity> CREATOR = new Creator<MetaEntity>() {
                public MetaEntity createFromParcel(Parcel source) {
                    return new MetaEntity(source);
                }

                public MetaEntity[] newArray(int size) {
                    return new MetaEntity[size];
                }
            };
        }

        public static class ResourcesEntity implements Parcelable {
            /**
             * classname : Quote
             * fields : {"name":"USD/KRW","price":"1098.300049","symbol":"KRW=X","ts":"1475269956","type":"currency","utctime":"2016-09-30T21:12:36+0000","volume":"0"}
             */

            private ResourceEntity resource;

            public void setResource(ResourceEntity resource) {
                this.resource = resource;
            }

            public ResourceEntity getResource() {
                return resource;
            }

            public static class ResourceEntity implements Parcelable {
                private String classname;
                /**
                 * name : USD/KRW
                 * price : 1098.300049
                 * symbol : KRW=X
                 * ts : 1475269956
                 * type : currency
                 * utctime : 2016-09-30T21:12:36+0000
                 * volume : 0
                 */

                private FieldsEntity fields;

                public void setClassname(String classname) {
                    this.classname = classname;
                }

                public void setFields(FieldsEntity fields) {
                    this.fields = fields;
                }

                public String getClassname() {
                    return classname;
                }

                public FieldsEntity getFields() {
                    return fields;
                }

                public static class FieldsEntity implements Parcelable {
                    private String name;
                    private String price;
                    private String symbol;
                    private String ts;
                    private String type;
                    private String utctime;
                    private String volume;
                    private boolean isChecked = false;

                    public boolean isChecked() {
                        return isChecked;
                    }

                    public void setChecked(boolean checked) {
                        isChecked = checked;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public void setPrice(String price) {
                        this.price = price;
                    }

                    public void setSymbol(String symbol) {
                        this.symbol = symbol;
                    }

                    public void setTs(String ts) {
                        this.ts = ts;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public void setUtctime(String utctime) {
                        this.utctime = utctime;
                    }

                    public void setVolume(String volume) {
                        this.volume = volume;
                    }

                    public String getName() {
                        return name;
                    }

                    public String getPrice() {
                        return price;
                    }

                    public String getSymbol() {
                        return symbol;
                    }

                    public String getTs() {
                        return ts;
                    }

                    public String getType() {
                        return type;
                    }

                    public String getUtctime() {
                        return utctime;
                    }

                    public String getVolume() {
                        return volume;
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {
                        dest.writeString(this.name);
                        dest.writeString(this.price);
                        dest.writeString(this.symbol);
                        dest.writeString(this.ts);
                        dest.writeString(this.type);
                        dest.writeString(this.utctime);
                        dest.writeString(this.volume);
                        dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
                    }

                    public FieldsEntity() {
                    }

                    protected FieldsEntity(Parcel in) {
                        this.name = in.readString();
                        this.price = in.readString();
                        this.symbol = in.readString();
                        this.ts = in.readString();
                        this.type = in.readString();
                        this.utctime = in.readString();
                        this.volume = in.readString();
                        this.isChecked = in.readByte() != 0;
                    }

                    public static final Creator<FieldsEntity> CREATOR = new Creator<FieldsEntity>() {
                        public FieldsEntity createFromParcel(Parcel source) {
                            return new FieldsEntity(source);
                        }

                        public FieldsEntity[] newArray(int size) {
                            return new FieldsEntity[size];
                        }
                    };
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(this.classname);
                    dest.writeParcelable(this.fields, 0);
                }

                public ResourceEntity() {
                }

                protected ResourceEntity(Parcel in) {
                    this.classname = in.readString();
                    this.fields = in.readParcelable(FieldsEntity.class.getClassLoader());
                }

                public static final Creator<ResourceEntity> CREATOR = new Creator<ResourceEntity>() {
                    public ResourceEntity createFromParcel(Parcel source) {
                        return new ResourceEntity(source);
                    }

                    public ResourceEntity[] newArray(int size) {
                        return new ResourceEntity[size];
                    }
                };
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeParcelable(this.resource, 0);
            }

            public ResourcesEntity() {
            }

            protected ResourcesEntity(Parcel in) {
                this.resource = in.readParcelable(ResourceEntity.class.getClassLoader());
            }

            public static final Creator<ResourcesEntity> CREATOR = new Creator<ResourcesEntity>() {
                public ResourcesEntity createFromParcel(Parcel source) {
                    return new ResourcesEntity(source);
                }

                public ResourcesEntity[] newArray(int size) {
                    return new ResourcesEntity[size];
                }
            };
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.meta, 0);
            dest.writeTypedList(resources);
        }

        public ListEntity() {
        }

        protected ListEntity(Parcel in) {
            this.meta = in.readParcelable(MetaEntity.class.getClassLoader());
            this.resources = in.createTypedArrayList(ResourcesEntity.CREATOR);
        }

        public static final Creator<ListEntity> CREATOR = new Creator<ListEntity>() {
            public ListEntity createFromParcel(Parcel source) {
                return new ListEntity(source);
            }

            public ListEntity[] newArray(int size) {
                return new ListEntity[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.list, 0);
    }

    public CurrenciesListModel() {
    }

    protected CurrenciesListModel(Parcel in) {
        this.list = in.readParcelable(ListEntity.class.getClassLoader());
    }

    public static final Creator<CurrenciesListModel> CREATOR = new Creator<CurrenciesListModel>() {
        public CurrenciesListModel createFromParcel(Parcel source) {
            return new CurrenciesListModel(source);
        }

        public CurrenciesListModel[] newArray(int size) {
            return new CurrenciesListModel[size];
        }
    };
}
