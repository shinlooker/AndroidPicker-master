package cn.qqtheme.framework.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 省份
 * <br/>
 * Author:李玉江[QQ:1032694760]
 * DateTime:2016-10-15 19:06
 * Builder:Android Studio
 */
public class Province extends Area implements LinkageFirst<City> {
    private List<City> cities = new ArrayList<>();

    public Province() {
        super();
    }

    public Province(String areaName) {
        super(areaName);
    }

    public Province(String areaId, String areaName) {
        super(areaId, areaName);
    }

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    @Override
    public List<City> getSeconds() {
        return cities;
    }

}