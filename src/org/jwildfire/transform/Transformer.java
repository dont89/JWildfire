/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke

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
package org.jwildfire.transform;

import java.util.Calendar;

import org.jwildfire.base.ManagedObject;
import org.jwildfire.base.Preset;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.swing.Buffer.BufferType;

public abstract class Transformer extends ManagedObject {
  protected boolean storeMesh3D;

  public abstract boolean supports3DOutput();

  public Mesh3D getOutputMesh3D(boolean pRemoveOwnReference) {
    return null;
  }

  public void setInputMesh3D(Mesh3D pInputMesh3D) {

  }

  protected abstract void performImageTransformation(SimpleImage pImg);

  public abstract void initDefaultParams(SimpleImage pImg);

  public abstract boolean acceptsInputBufferType(BufferType pBufferType);

  protected int roundColor(double pValue) {
    int res = (int) (pValue + 0.5);
    if (res < 0)
      res = 0;
    else if (res > 255)
      res = 255;
    return res;
  }

  private long initTime() {
    return Calendar.getInstance().getTimeInMillis();
  }

  private void showElapsedTime(long t0) {
    if (allowShowStats()) {
      long t1 = Calendar.getInstance().getTimeInMillis();
      String name = this.getClass().getSimpleName();
      System.out.println(name + ": " + ((double) (t1 - t0) / 1000.0) + "s");
    }
  }

  public void transformImage(SimpleImage pImg) {
    //    long t0 = initTime();
    initTransformation(pImg);
    try {
      performImageTransformation(pImg);
      //      showElapsedTime(t0);
    }
    finally {
      cleanupTransformation(pImg);
    }
  }

  protected void initTransformation(SimpleImage pImg) {

  }

  protected void cleanupTransformation(SimpleImage pImg) {

  }

  public void applyPreset(String pPresetName, SimpleImage pImg) {
    Preset preset = getPresetByName(pPresetName);
    if (preset == null) {
      if (pImg != null) {
        initDefaultParams(pImg);
      }
    }
    else {
      applyPreset(preset);
    }
  }

  public void setStoreMesh3D(boolean storeMesh3D) {
    this.storeMesh3D = storeMesh3D;
  }

  protected boolean allowShowStats() {
    return true;
  }
}
