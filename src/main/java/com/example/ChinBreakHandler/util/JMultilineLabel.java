package com.example.ChinBreakHandler.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class JMultilineLabel extends JTextArea
{
    private static final long serialVersionUID = 1L;

    public JMultilineLabel()
    {
        super();

        setEditable(false);
        setCursor(null);
        setOpaque(false);
        setFocusable(false);
        setWrapStyleWord(true);
        setLineWrap(true);
        setBorder(new EmptyBorder(0, 8, 0, 8));
        setAlignmentY(JLabel.CENTER_ALIGNMENT);

        DefaultCaret caret = (DefaultCaret) getCaret();
        if (caret != null)
        {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }
}
