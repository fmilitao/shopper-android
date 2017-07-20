package io.github.fmilitao.shopper.utils.model;

public class Item {
    private long id;
    private String name;
    private String unit;
    private double quantity;
    private String category;
    private boolean done;

    public Item() {
        // This is only valid because the 'id' should always be ignored when persisting to the DB.
        this(-1, null, null, 1, null, false);
    }

    public Item(long id, String name, String unit, double quantity, String category, boolean done) {
        this.setId(id);
        this.setName(name);
        this.setQuantity(quantity);
        this.setCategory(category);
        this.setUnit(unit);
        this.setDone(done);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
