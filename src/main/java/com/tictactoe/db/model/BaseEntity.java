package com.tictactoe.db.model;

import java.io.Serializable;
/**
 * Base entity class that all model classes will extend
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}