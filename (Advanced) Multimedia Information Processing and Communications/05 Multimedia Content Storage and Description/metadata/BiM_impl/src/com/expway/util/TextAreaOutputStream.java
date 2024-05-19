/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
development of the MPEG-7 Systems (ISO/IEC 15938-1) standard. 

This software module is an implementation of a part of one or more 
MPEG-7 Systems (ISO/IEC 15938-1) tools as specified by the 
MPEG-7 Systems (ISO/IEC 15938-1) standard. 

ISO/IEC gives users of the MPEG-7 Systems (ISO/IEC 15938-1) free license
to this software module or modifications thereof for use in hardware or
software products claiming conformance to the MPEG-7 Systems 
(ISO/IEC 15938-1). 

Those intending to use this software module in hardware or software 
products are advised that its use may infringe existing patents.

The original developer of this software module and his/her company, the
subsequent editors and their companies, and ISO/IEC have no liability
for use of this software module or modifications thereof in an
implementation. 

Copyright is not released for non MPEG-7 Systems (ISO/IEC 15938-1) 
conforming products. 

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

package com.expway.util;         

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.io.IOException;

public class TextAreaOutputStream extends OutputStream {
    JTextArea jTextArea;
    JScrollPane jScrollPane;

    public TextAreaOutputStream(JTextArea j,JScrollPane js){
        jTextArea = j;
        jScrollPane = js;
    }

    public void write(int b) throws IOException {
        char a[] = { (char)b };
        String s = new String(a);
        jTextArea.append(s);
        moveToEnd();
    }

    public void write(byte b[], int off, int len) throws IOException {
        String s = new String(b, off, len);
        jTextArea.append(s);
        moveToEnd();
    }

    public void close() throws IOException {
        jTextArea = null;
    }

    public void clear() {
        jTextArea.setText("");
    }

    // Marche pas bien
    synchronized public void moveToEnd() {
        JScrollBar jbar=jScrollPane.getVerticalScrollBar();
        int value=jbar.getModel().getValue();
        int max=jbar.getModel().getMaximum();
        int extent=jbar.getModel().getExtent();

        jbar.getModel().setValueIsAdjusting(true);
        jbar.getModel().setValue(max-extent);
        jbar.getModel().setValueIsAdjusting(false);

        //System.err.println("value="+value+" max="+max+" ext="+extent+" max-ext="+(max-extent));
    }
}
