package Model;

import java.util.List;

public class Cinema {
    private String id;
    private String name;
    private List<Screen> screens;
    private String City;

    public Cinema(String id, String name, List<Screen> screens, String city) {
        this.id = id;
        this.name = name;
        this.screens = screens;
        City = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }
}
