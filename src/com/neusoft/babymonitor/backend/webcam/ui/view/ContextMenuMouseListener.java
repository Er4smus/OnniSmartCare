package com.neusoft.babymonitor.backend.webcam.ui.view;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class ContextMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();

    private Action pasteAction;

    private JTextComponent textComponent;

    @SuppressWarnings("serial")
    public ContextMenuMouseListener() {
        // add paste action
        pasteAction = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                textComponent.paste();
            }
        };
        popup.add(pasteAction);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // if the right click button was pressed on a JTextComponent
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JTextComponent)) {
                return;
            }

            textComponent = (JTextComponent) e.getSource();
            textComponent.requestFocus();

            boolean enabled = textComponent.isEnabled();
            boolean editable = textComponent.isEditable();

            boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)
                    .isDataFlavorSupported(DataFlavor.stringFlavor);

            // enable paste action in the popup menu
            pasteAction.setEnabled(enabled && editable && pasteAvailable);
            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }
            // display a popup menu with paste action
            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}