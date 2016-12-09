package pl.lodz.p.dmcs.map;

/**
 * Created by Krystian on 2016-12-09.
 */

public class UnitData {
    private int id;
    private String name;
    private boolean checked;

    public UnitData(int id, String name) {
        this.id = id;
        this.name = name;
        this.checked = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
