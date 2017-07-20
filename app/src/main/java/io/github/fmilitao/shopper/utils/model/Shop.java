package io.github.fmilitao.shopper.utils.model;


public class Shop {
    private final long id;
    private final String name;

    public Shop(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
