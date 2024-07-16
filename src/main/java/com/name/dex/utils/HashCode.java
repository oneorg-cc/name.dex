package com.name.dex.utils;

public abstract class HashCode {

    public static class Builder {

        private final int prime;
        private int result = 1;

        //

        public Builder() {
            this(31);
        }

        public Builder(int prime) {
            this.prime = prime;
        }

        //

        public Builder append(Object obj) {
            this.result = this.prime * this.result + ((obj == null) ? 0 : obj.hashCode());
            return this;
        }

        //

        public int build() {
            int hashcode = this.result;
            this.result = 1;
            return hashcode;
        }

    }

    //


    public static boolean equals(Object o1, Object o2) {
        if(o1 == null && o2 == null) return true;
        if(o1 == null || o2 == null) return false;
        return o1.getClass() == o2.getClass() && o1.hashCode() == o2.hashCode();
    }
}
