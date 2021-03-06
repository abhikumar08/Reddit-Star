
package com.rahul_lohra.redditstar.modal;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListingDataStructure extends ListingDS{

    @SerializedName("children")
    @Expose
    private List<ListingChild> children = null;

    public List<ListingChild> getChildren() {
        return children;
    }

    public void setChildren(List<ListingChild> children) {
        this.children = children;
    }
}
