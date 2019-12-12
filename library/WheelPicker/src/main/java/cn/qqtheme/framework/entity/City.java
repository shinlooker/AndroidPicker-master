package cn.qqtheme.framework.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 地市
 * <br/>
 * Author:李玉江[QQ:1032694760]
 * DateTime:2016-10-15 19:07
 * Builder:Android Studio
 */
public class City extends Area implements LinkageSecond<County> {
    private String provinceId;
    private List<County> counties = new ArrayList<>();

    public City() {
        super();
    }

    public City(String areaName) {
        super(areaName);
    }

    public City(String areaId, String areaName) {
        super(areaId, areaName);
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public List<County> getCounties() {
        return counties;
    }

    public void setCounties(List<County> counties) {
        this.counties = counties;
    }

    @Override
    public List<County> getThirds() {
        return counties;
    }

}