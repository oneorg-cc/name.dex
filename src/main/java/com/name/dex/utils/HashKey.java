package com.name.dex.utils;

import java.util.List;
import java.util.function.Supplier;

public abstract class HashKey {

    private Supplier<List<Object>> relevantObjectsSupplier = null;

    protected HashKey() {}

    protected HashKey(Supplier<List<Object>> relevantObjectsSupplier) {
        this.relevantObjectsSupplier = relevantObjectsSupplier;
    }

    //

    protected Supplier<List<Object>> getRelevantObjectsSupplier() { return this.relevantObjectsSupplier; }

    protected void setRelevantObjectsSupplier(Supplier<List<Object>> supplier) { this.relevantObjectsSupplier = supplier; }

    //

    @Override
    public int hashCode() {
        HashCode.Builder builder = new HashCode.Builder();

        if(this.relevantObjectsSupplier != null) {
            List<Object> relevantObjects = this.relevantObjectsSupplier.get();
            for (Object relevantObject : relevantObjects)
                builder.append(relevantObject);
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        return HashCode.equals(this, obj);
    }
}
