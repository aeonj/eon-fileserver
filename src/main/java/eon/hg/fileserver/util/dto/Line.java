package eon.hg.fileserver.util.dto;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private String name;

    private List<Object[]> data = new ArrayList<Object[]>();

    public Line(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object[]> getData() {
        return data;
    }

    public void setData(List<Object[]> data) {
        this.data = data;
    }

}
