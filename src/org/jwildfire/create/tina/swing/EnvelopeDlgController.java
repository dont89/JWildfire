/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2013 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.swing;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.RootPaneContainer;

import org.jwildfire.base.Tools;
import org.jwildfire.base.mathlib.MathLib;
import org.jwildfire.envelope.Envelope;
import org.jwildfire.envelope.EnvelopePanel;
import org.jwildfire.envelope.EnvelopeView;

public class EnvelopeDlgController {
  private enum MouseClickWaitMode {
    ADD_POINT, REMOVE_POINT, NONE
  }

  private final JButton addPointButton;
  private final JButton removePointButton;
  private final JButton clearButton;
  private final JButton viewAllButton;
  private final JButton viewLeftButton;
  private final JButton viewRightButton;
  private final JButton viewUpButton;
  private final JButton viewDownButton;
  private final JWFNumberField xMinREd;
  private final JWFNumberField xMaxREd;
  private final JWFNumberField yMinREd;
  private final JWFNumberField yMaxREd;
  private final JWFNumberField xREd;
  private final JWFNumberField yREd;
  private final JComboBox interpolationCmb;
  private final EnvelopePanel envelopePanel;

  private boolean noRefresh;
  private final Envelope envelope;

  private MouseClickWaitMode mouseClickWaitMode = MouseClickWaitMode.NONE;

  public EnvelopeDlgController(Envelope pEnvelope, JButton pAddPointButton, JButton pRemovePointButton, JButton pClearButton,
      JWFNumberField pXMinREd, JWFNumberField pXMaxREd, JWFNumberField pYMinREd, JWFNumberField pYMaxREd,
      JWFNumberField pXREd, JWFNumberField pYREd, JComboBox pInterpolationCmb, JButton pViewAllButton,
      JButton pViewLeftButton, JButton pViewRightButton, JButton pViewUpButton,
      JButton pViewDownButton, EnvelopePanel pEnvelopePanel) {
    envelope = pEnvelope;
    addPointButton = pAddPointButton;
    removePointButton = pRemovePointButton;
    clearButton = pClearButton;
    xMinREd = pXMinREd;
    xMaxREd = pXMaxREd;
    yMinREd = pYMinREd;
    yMaxREd = pYMaxREd;
    xREd = pXREd;
    yREd = pYREd;
    interpolationCmb = pInterpolationCmb;
    viewAllButton = pViewAllButton;
    viewLeftButton = pViewLeftButton;
    viewRightButton = pViewRightButton;
    viewUpButton = pViewUpButton;
    viewDownButton = pViewDownButton;
    envelopePanel = pEnvelopePanel;
    envelopePanel.addMouseListener(new java.awt.event.MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {
        switch (mouseClickWaitMode) {
          case NONE:
            selectPoint(e);
            break;
        }
      }
    });
    envelopePanel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent e) {
        try {
          switch (mouseClickWaitMode) {
            case ADD_POINT:
              finishAddPoint(e);
              break;
            case REMOVE_POINT:
              finishRemovePoint(e);
              break;
            default:
              // selectPoint(e);
              break;
          }
        }
        finally {
          mouseClickWaitMode = MouseClickWaitMode.NONE;
          clearCrosshairCursor();
        }
      }
    });
    envelopePanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseDragged(java.awt.event.MouseEvent e) {
        if (mouseClickWaitMode == MouseClickWaitMode.NONE) {
          movePoint(e);
        }
      }
    });

  }

  public Envelope getCurrEnvelope() {
    return envelope;
  }

  public void enableControls() {
    boolean hasEnvelope = (envelope != null);
    boolean editable = hasEnvelope && !envelope.isLocked();
    interpolationCmb.setEnabled(editable);
    xMinREd.setEnabled(hasEnvelope);
    xMaxREd.setEnabled(hasEnvelope);
    yMinREd.setEnabled(hasEnvelope);
    yMaxREd.setEnabled(hasEnvelope);
    xREd.setEnabled(editable);
    yREd.setEnabled(editable);
    addPointButton.setEnabled(editable);
    removePointButton.setEnabled(editable && (envelope.size() > 1));
    clearButton.setEnabled(editable);
    viewAllButton.setEnabled(hasEnvelope);
    viewLeftButton.setEnabled(hasEnvelope);
    viewRightButton.setEnabled(hasEnvelope);
    viewUpButton.setEnabled(hasEnvelope);
    viewDownButton.setEnabled(hasEnvelope);
  }

  private void refreshXMinField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      xMinREd.setValue(envelope.getViewXMin());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  private void refreshXMaxField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      xMaxREd.setValue(envelope.getViewXMax());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  private void refreshYMinField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      yMinREd.setValue(envelope.getViewYMin());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  private void refreshYMaxField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      yMaxREd.setValue(envelope.getViewYMax());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  private void refreshXField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      xREd.setValue(envelope.getSelectedX());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  private void refreshYField() {
    boolean oldNoRefresh = noRefresh;
    noRefresh = true;
    try {
      yREd.setValue(envelope.getSelectedY());
    }
    finally {
      noRefresh = oldNoRefresh;
    }
  }

  public void refreshEnvelope() {
    refreshXField();
    refreshXMinField();
    refreshXMaxField();
    refreshYField();
    refreshYMinField();
    refreshYMaxField();
    envelopePanel.repaint();
  }

  public void removePoint() {
    setCrosshairCursor();
    mouseClickWaitMode = MouseClickWaitMode.REMOVE_POINT;
  }

  public void addPoint() {
    setCrosshairCursor();
    mouseClickWaitMode = MouseClickWaitMode.ADD_POINT;
  }

  public void interpolationCmbChanged() {
    if (envelope != null && !noRefresh) {
      Envelope.Interpolation interpolation = (Envelope.Interpolation) interpolationCmb
          .getSelectedItem();
      envelope.setInterpolation(interpolation);
      refreshEnvelope();
    }
  }

  public void clearEnvelope() {
    envelope.clear();
    refreshEnvelope();
  }

  public void editFieldChanged() {
    if (!noRefresh) {
      envelope.setViewXMin(getIntValue(xMinREd));
      envelope.setViewXMax(getIntValue(xMaxREd));
      envelope.setSelectedX(getIntValue(xREd));
      envelope.setViewYMin((Double) yMinREd.getValue());
      envelope.setViewYMax((Double) yMaxREd.getValue());
      envelope.setSelectedY((Double) yREd.getValue());
      if ((envelope.getViewXMax() - envelope.getViewXMin()) < 1)
        envelope.setViewXMax(envelope.getViewXMin() + 1);
      if ((envelope.getViewYMax() - envelope.getViewYMin()) < 0.001)
        envelope.setViewYMax(envelope.getViewYMin() + 0.001);
      refreshEnvelope();
    }
  }

  private int getIntValue(JWFNumberField pEdit) {
    Object val = pEdit.getValue();
    if (val != null && val instanceof Double) {
      return Tools.FTOI((Double) val);
    }
    else if (val != null && val instanceof Integer) {
      return (Integer) val;
    }
    else {
      return 0;
    }
  }

  private final Cursor CROSSHAIR_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  private final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  private void setCrosshairCursor() {
    RootPaneContainer root = (RootPaneContainer) envelopePanel.getTopLevelAncestor();
    root.getGlassPane().setCursor(CROSSHAIR_CURSOR);
    root.getGlassPane().setVisible(true);
  }

  private void clearCrosshairCursor() {
    RootPaneContainer root = (RootPaneContainer) envelopePanel.getTopLevelAncestor();
    root.getGlassPane().setCursor(DEFAULT_CURSOR);
    root.getGlassPane().setVisible(false);
  }

  private void finishRemovePoint(MouseEvent e) {
    if ((envelope != null) && (envelope.size() > 1)) {
      int lx = e.getX();
      EnvelopeView envelopeView = new EnvelopeView(envelopePanel);
      double x = ((double) lx + envelopeView.getEnvelopeXTrans())
          / envelopeView.getEnvelopeXScale();
      int sel = 0;
      double dist = x - envelope.getX()[0];
      if (dist < 0)
        dist = 0 - dist;
      for (int i = 1; i < envelope.size(); i++) {
        double dist2 = x - envelope.getX()[i];
        if (dist2 < 0)
          dist2 = 0 - dist2;
        if (dist2 < dist) {
          dist = dist2;
          sel = i;
        }
      }

      int cnt = envelope.size() - 1;
      int[] xVals = new int[cnt];
      double[] yVals = new double[cnt];

      int curr = 0;
      for (int i = 0; i < envelope.size(); i++) {
        if (i != sel) {
          xVals[curr] = envelope.getX()[i];
          yVals[curr++] = envelope.getY()[i];
        }
      }
      envelope.setValues(xVals, yVals);
      refreshEnvelope();
      enableControls();
    }
  }

  private void selectPoint(MouseEvent e) {
    if (envelope != null) {
      int lx = e.getX();
      EnvelopeView envelopeView = new EnvelopeView(envelopePanel);
      double x = ((double) lx + envelopeView.getEnvelopeXTrans())
          / envelopeView.getEnvelopeXScale();
      int sel = 0;
      double dist = x - envelope.getX()[0];
      if (dist < 0)
        dist = 0 - dist;
      for (int i = 1; i < envelope.size(); i++) {
        double dist2 = x - envelope.getX()[i];
        if (dist2 < 0)
          dist2 = 0 - dist2;
        if (dist2 < dist) {
          dist = dist2;
          sel = i;
        }
      }
      envelope.select(sel);
      refreshXField();
      refreshYField();
      envelopePanel.repaint();
    }
  }

  private void finishAddPoint(MouseEvent e) {
    if (envelope != null) {
      int lx = e.getX();
      int ly = e.getY();
      EnvelopeView envelopeView = new EnvelopeView(envelopePanel);

      double x = (double) Tools.FTOI(((double) lx + envelopeView.getEnvelopeXTrans())
          / envelopeView.getEnvelopeXScale());
      double y = ((double) ly + envelopeView.getEnvelopeYTrans())
          / envelopeView.getEnvelopeYScale();
      {
        for (int i = 0; i < envelope.size(); i++) {
          if (MathLib.fabs(x - envelope.getX()[i]) < 0.01)
            return;
        }
      }

      int xl = (Tools.FTOI(x));

      int pred = -1;
      for (int i = 0; i < envelope.size(); i++) {
        if (envelope.getX()[i] < xl)
          pred = i;
        else if (envelope.getX()[i] == xl) {
          xl += 1.0;
          pred = i;
        }
      }
      int cnt = envelope.size() + 1;
      int[] xVals = new int[cnt];
      double[] yVals = new double[cnt];

      if (pred >= 0) {
        for (int i = 0; i <= pred; i++) {
          xVals[i] = envelope.getX()[i];
          yVals[i] = envelope.getY()[i];
        }
        int curr = pred + 1;
        xVals[curr] = xl;
        yVals[curr++] = y;
        for (int i = pred + 1; i < envelope.size(); i++) {
          xVals[curr] = envelope.getX()[i];
          yVals[curr++] = envelope.getY()[i];
        }
      }
      else {
        xVals[0] = xl;
        yVals[0] = y;
        int curr = 1;
        for (int i = 0; i < envelope.size(); i++) {
          xVals[curr] = envelope.getX()[i];
          yVals[curr++] = envelope.getY()[i];
        }
      }
      envelope.setValues(xVals, yVals);
      envelope.select(pred + 1);
      refreshEnvelope();
      enableControls();
    }
  }

  private void movePoint(java.awt.event.MouseEvent e) {
    if ((envelope != null) && (!envelope.isLocked())) {
      int lx = e.getX();
      int ly = e.getY();
      EnvelopeView envelopeView = new EnvelopeView(envelopePanel);

      double x = (double) Tools.FTOI(((double) lx + envelopeView.getEnvelopeXTrans())
          / envelopeView.getEnvelopeXScale());
      double y = ((double) ly + envelopeView.getEnvelopeYTrans())
          / envelopeView.getEnvelopeYScale();
      if (envelope.getSelectedIdx() > 0) {
        double xc;
        xc = envelope.getX()[envelope.getSelectedIdx() - 1];
        if (x <= xc)
          x = xc + 1.0;
      }
      if (envelope.getSelectedIdx() < (envelope.size() - 1)) {
        double xc;
        xc = envelope.getX()[envelope.getSelectedIdx() + 1];
        if (x >= xc)
          x = xc - 1.0;
      }
      if (lx <= envelopeView.getEnvelopeLeft()) {
        int xi = Tools.FTOI(x);
        if (xi < -9999)
          xi = -9999;
        envelope.setViewXMin(xi);
        envelope.getX()[envelope.getSelectedIdx()] = xi;
        envelope.getY()[envelope.getSelectedIdx()] = y;
        refreshXMinField();
        refreshXField();
        refreshYField();
        envelopePanel.repaint();
      }
      else if (lx >= envelopeView.getEnvelopeRight()) {
        int xi = Tools.FTOI(x);
        if (xi > 9999)
          xi = 9999;
        envelope.setViewXMax(xi);
        envelope.getX()[envelope.getSelectedIdx()] = xi;
        envelope.getY()[envelope.getSelectedIdx()] = y;
        refreshXMaxField();
        refreshXField();
        refreshYField();
        envelopePanel.repaint();
      }
      else if (ly <= envelopeView.getEnvelopeTop()) {
        int xi = Tools.FTOI(x);
        if (y > 32000.0)
          y = 32000.0;
        envelope.setViewYMax(y);
        envelope.getX()[envelope.getSelectedIdx()] = xi;
        envelope.getY()[envelope.getSelectedIdx()] = y;
        refreshYMaxField();
        refreshXField();
        refreshYField();
        envelopePanel.repaint();
      }
      else if (ly >= envelopeView.getEnvelopeBottom()) {
        int xi = Tools.FTOI(x);
        if (y < -32000.0)
          y = -32000.0;
        envelope.setViewYMin(y);
        envelope.getX()[envelope.getSelectedIdx()] = xi;
        envelope.getY()[envelope.getSelectedIdx()] = y;
        refreshYMinField();
        refreshXField();
        refreshYField();
        envelopePanel.repaint();
      }
      else {
        int xi = Tools.FTOI(x);
        envelope.getX()[envelope.getSelectedIdx()] = xi;
        envelope.getY()[envelope.getSelectedIdx()] = y;
        refreshXField();
        refreshYField();
        envelopePanel.repaint();
      }
    }
  }

  public void viewDown() {
    if (envelope != null) {
      double dy = ((envelope.getViewYMax() - envelope.getViewYMin()) / 10.0);
      envelope.setViewYMin(envelope.getViewYMin() + dy);
      envelope.setViewYMax(envelope.getViewYMax() + dy);
      refreshYMinField();
      refreshYMaxField();
      envelopePanel.repaint();
    }
  }

  public void viewUp() {
    if (envelope != null) {
      double dy = ((envelope.getViewYMax() - envelope.getViewYMin()) / 10.0);
      envelope.setViewYMin(envelope.getViewYMin() - dy);
      envelope.setViewYMax(envelope.getViewYMax() - dy);
      refreshYMinField();
      refreshYMaxField();
      envelopePanel.repaint();
    }
  }

  public void viewAll() {
    if (envelope != null) {
      double xmin, xmax, ymin, ymax;
      if (envelope.size() == 1) {
        xmin = envelope.getX()[0] - 1.0;
        xmax = envelope.getX()[0] + 10.0;
        ymin = envelope.getY()[0] - 1.0;
        ymax = envelope.getY()[0] + 1.0;
      }
      else {
        xmin = xmax = envelope.getX()[0];
        ymin = ymax = envelope.getY()[0];
        for (int i = 1; i < envelope.size(); i++) {
          if (envelope.getX()[i] < xmin)
            xmin = envelope.getX()[i];
          else if (envelope.getX()[i] > xmax)
            xmax = envelope.getX()[i];
          if (envelope.getY()[i] < ymin)
            ymin = envelope.getY()[i];
          else if (envelope.getY()[i] > ymax)
            ymax = envelope.getY()[i];
        }
        double dx = (xmax - xmin) / 10.0;
        double dy = (ymax - ymin) / 8.0;
        xmin = xmin - dx;
        xmax = xmax + dx;
        ymin = ymin - dy;
        ymax = ymax + dy;
      }
      if ((xmax - xmin) < 10.0)
        xmax = xmin + 10.0;
      if ((ymax - ymin) < 0.5)
        ymax = ymin + 0.5;

      envelope.setViewXMin(Tools.FTOI(xmin));
      envelope.setViewXMax(Tools.FTOI(xmax));
      envelope.setViewYMin(ymin);
      envelope.setViewYMax(ymax);

      refreshXMinField();
      refreshXMaxField();
      refreshYMinField();
      refreshYMaxField();
      envelopePanel.repaint();
    }
  }

  public void viewLeft() {
    if (envelope != null) {
      int dx = (int) ((envelope.getViewXMax() - envelope.getViewXMin()) / 20.0 + 0.5);
      envelope.setViewXMin(envelope.getViewXMin() + dx);
      envelope.setViewXMax(envelope.getViewXMax() + dx);
      refreshXMinField();
      refreshXMaxField();
      envelopePanel.repaint();
    }
  }

  public void viewRight() {
    if (envelope != null) {
      int dx = (int) ((envelope.getViewXMax() - envelope.getViewXMin()) / 20.0 + 0.5);
      envelope.setViewXMin(envelope.getViewXMin() - dx);
      envelope.setViewXMax(envelope.getViewXMax() - dx);
      refreshXMinField();
      refreshXMaxField();
      envelopePanel.repaint();
    }
  }

  public void setNoRefresh(boolean pNoRefresh) {
    noRefresh = pNoRefresh;
  }

}
