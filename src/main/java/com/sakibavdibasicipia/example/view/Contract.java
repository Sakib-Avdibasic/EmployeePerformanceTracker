package com.sakibavdibasicipia.example.view;

import javax.swing.*;
import java.awt.*;

public class Contract {
    public Contract(String date, String name, Employee employee) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Ugovor o unapređenju");
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(null);

            dialog.setLayout(new BorderLayout());

            StringBuilder contractBuilder = new StringBuilder();
            contractBuilder.append("UGOVOR O UNAPREĐENJU\n\n");
            contractBuilder.append("Ovaj ugovor je sklopljen na dan " + date + " između:\n");
            contractBuilder.append("- Poslodavca: TehnoPuls\n");
            contractBuilder.append("- Zaposlenika: " +  name + "\n\n");
            contractBuilder.append("Predmet ugovora:\n");
            contractBuilder.append("Poslodavac ovim ugovorom unapređuje zaposlenika na poziciju:\n");
            contractBuilder.append("GLAVNI UREDNIK\n\n");
            contractBuilder.append("Odredbe i uslovi:\n");
            contractBuilder.append("1. Zaposlenik preuzima odgovornost za vođenje uredničkog tima.\n");
            contractBuilder.append("2. Plata na novoj poziciji iznosi 3000 BAM.\n");
            contractBuilder.append("3. Ugovor stupa na snagu od " + date + ".\n\n");
            contractBuilder.append("Potpisi:\n");
            contractBuilder.append("TehnoPuls                              ");
            StringBuilder crta = new StringBuilder();
            for(int i = 0; i < name.length(); i++) crta.append("_");
            contractBuilder.append(crta + "\n");
            contractBuilder.append("Poslodavac                             Zaposlenik\n");

            JTextArea textArea = new JTextArea(contractBuilder.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton acceptButton = new JButton("Prihvati");
            JButton declineButton = new JButton("Odbij");

            acceptButton.addActionListener(e -> {
                Timer timer = new Timer(100, null);
                final int[] index = {0};
                String contractText = textArea.getText();

                timer.addActionListener(event -> {
                    if (index[0] < name.length()) {
                        String updatedText = contractText.replaceFirst(crta.toString(),
                                name.substring(0, index[0] + 1) + crta.toString().substring(index[0] + 1));
                        textArea.setText(updatedText);
                        index[0]++;
                    } else {
                        timer.stop();
                        employee.setAccepted(true);
                        dialog.dispose();
                    }
                });
                timer.start();
            });

            declineButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(dialog, "Odbili ste unapređenje.");
                employee.setAccepted(false);
                dialog.dispose();
            });

            buttonPanel.add(acceptButton);
            buttonPanel.add(declineButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });
    }
}

