package com.sakibavdibasicipia.example.view;

public class ComboItem {
    private String text;
    private String value;

    public ComboItem(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return text;
    }
}
