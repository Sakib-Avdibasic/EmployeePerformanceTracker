package com.sakibavdibasicipia.example;

import com.formdev.flatlaf.FlatLightLaf;
import com.sakibavdibasicipia.example.view.Login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        UIManager.put("Panel.background", Color.decode("#F4F6FC"));
        UIManager.put("Panel.border", new EmptyBorder(5, 5, 5, 5));

        UIManager.put("Button.arc", 30);
        UIManager.put("TextComponent.arc", 20);
        UIManager.put("ComboBox.arc", 20);
        UIManager.put("CheckBox.arc", 15);

        Color primaryBackground = Color.decode("#F4F6FC");
        Color primaryAccent = Color.decode("#004AAD");
        Color secondaryAccent = Color.decode("#0073E6");
        Color mutedColor = Color.decode("#B0C4DE");
        Color focusRing = Color.decode("#FF5722");
        Color textPrimary = Color.decode("#000000");
        Color textSecondary = Color.decode("#333333");

        UIManager.put("Button.pressedBackground", primaryAccent.darker());

        UIManager.put("TextComponent.background", primaryBackground);
        UIManager.put("TextComponent.foreground", textPrimary);
        UIManager.put("TextComponent.caretForeground", primaryAccent);
        UIManager.put("TextComponent.margin", new Insets(10, 12, 10, 12));
        UIManager.put("TextComponent.border", BorderFactory.createLineBorder(mutedColor, 1));

        UIManager.put("ComboBox.background", primaryBackground);
        UIManager.put("ComboBox.foreground", textPrimary);
        UIManager.put("ComboBox.selectionBackground", primaryAccent);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("CheckBox.icon.checkmarkColor", primaryAccent);
        UIManager.put("CheckBox.foreground", textPrimary);
        UIManager.put("CheckBox.hoverCheckmarkColor", secondaryAccent);

        UIManager.put("Label.foreground", textSecondary);

        UIManager.put("Component.focusColor", focusRing);
        UIManager.put("Component.focusWidth", 1);
        new Login();
    }

}