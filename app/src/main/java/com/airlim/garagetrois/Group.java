package com.airlim.garagetrois;

/**
 * Created by jlang on 2/27/14.
 */
import java.util.ArrayList;
import java.util.List;

public class Group {

    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

}
