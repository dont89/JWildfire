/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2012 Andreas Maschke

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
package org.jwildfire.create.tina.render;

import static org.jwildfire.base.mathlib.MathLib.EPSILON;
import static org.jwildfire.base.mathlib.MathLib.M_PI;
import static org.jwildfire.base.mathlib.MathLib.cos;
import static org.jwildfire.base.mathlib.MathLib.exp;
import static org.jwildfire.base.mathlib.MathLib.log;
import static org.jwildfire.base.mathlib.MathLib.sin;
import static org.jwildfire.base.mathlib.MathLib.sqrt;

import java.util.List;

import org.jwildfire.base.Prefs;
import org.jwildfire.create.tina.base.Constants;
import org.jwildfire.create.tina.base.DrawMode;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.base.raster.AbstractRasterPoint;
import org.jwildfire.create.tina.palette.RenderColor;

public final class FlameRenderBlurThread extends FlameRenderThread {
  private XYZPoint affineT;
  private XYZPoint varT;
  private XYZPoint p;
  private XYZPoint q;
  private XForm xf;
  private long iter;
  private long startIter;

  public FlameRenderBlurThread(Prefs pPrefs, int pThreadId, FlameRenderer pRenderer, Flame pFlame, long pSamples) {
    super(pPrefs, pThreadId, pRenderer, pFlame, pSamples);
  }

  @Override
  protected void initState() {
    startIter = 0;
    preFuseIter();
  }

  @Override
  protected void iterate() {
    List<IterationObserver> observers = renderer.getIterationObservers();

    double blurKernel[][] = flame.getShadingInfo().createBlurKernel();
    //int blurRadius = (int) (flame.getShadingInfo().getBlurRadius() * (flame.getPixelsPerUnit() / 200.0));
    int blurRadius = flame.getShadingInfo().getBlurRadius();

    double fade = flame.getShadingInfo().getBlurFade();
    if (fade < 0.0) {
      fade = 0.0;
    }
    else if (fade > 1.0) {
      fade = 1.0;
    }
    long blurMax = (long) ((1 - fade) * samples);
    int rasterWidth = renderer.rasterWidth;
    int rasterHeight = renderer.rasterHeight;

    for (iter = startIter; !forceAbort && (samples < 0 || iter < samples); iter++) {
      if (iter % 10000 == 0) {
        preFuseIter();
      }
      if (iter % 100 == 0) {
        currSample = iter;
        if (Double.isInfinite(p.x) || Double.isInfinite(p.y) || Double.isInfinite(p.z) || Double.isNaN(p.x) || Double.isNaN(p.y) || Double.isNaN(p.z)) {
          //          System.out.println(Tools.TimeToString(new Date()) + ": recovering...");
          preFuseIter();
        }
      }
      xf = xf.getNextAppliedXFormTable()[randGen.random(Constants.NEXT_APPLIED_XFORM_TABLE_SIZE)];
      if (xf == null) {
        return;
      }
      xf.transformPoint(ctx, affineT, varT, p, p);

      if (xf.getDrawMode() == DrawMode.HIDDEN)
        continue;
      else if ((xf.getDrawMode() == DrawMode.OPAQUE) && (randGen.random() > xf.getOpacity()))
        continue;

      List<XForm> finalXForms = flame.getFinalXForms();
      int xIdx, yIdx;

      if (finalXForms.size() > 0) {
        finalXForms.get(0).transformPoint(ctx, affineT, varT, p, q);
        for (int i = 1; i < finalXForms.size(); i++) {
          finalXForms.get(i).transformPoint(ctx, affineT, varT, q, q);
        }
        if (!renderer.project(q, prj))
          continue;
        XForm finalXForm = finalXForms.get(finalXForms.size() - 1);
        if ((finalXForm.getAntialiasAmount() > EPSILON) && (finalXForm.getAntialiasRadius() > EPSILON) && (randGen.random() > 1.0 - finalXForm.getAntialiasAmount())) {
          double dr = exp(finalXForm.getAntialiasRadius() * sqrt(-log(randGen.random()))) - 1.0;
          double da = randGen.random() * 2.0 * M_PI;
          xIdx = (int) (renderer.bws * prj.x + dr * cos(da) + 0.5);
          if (xIdx < 0 || xIdx >= renderer.rasterWidth)
            continue;
          yIdx = (int) (renderer.bhs * prj.y + dr * sin(da) + 0.5);
          if (yIdx < 0 || yIdx >= renderer.rasterHeight)
            continue;
        }
        else {
          xIdx = (int) (renderer.bws * prj.x + 0.5);
          yIdx = (int) (renderer.bhs * prj.y + 0.5);
        }
      }
      else {
        q.assign(p);
        if (!renderer.project(q, prj))
          continue;

        if ((xf.getAntialiasAmount() > EPSILON) && (xf.getAntialiasRadius() > EPSILON) && (randGen.random() > 1.0 - xf.getAntialiasAmount())) {
          double dr = exp(xf.getAntialiasRadius() * sqrt(-log(randGen.random()))) - 1.0;
          double da = randGen.random() * 2.0 * M_PI;
          xIdx = (int) (renderer.bws * prj.x + dr * cos(da) + 0.5);
          yIdx = (int) (renderer.bhs * prj.y + dr * sin(da) + 0.5);
        }
        else {
          xIdx = (int) (renderer.bws * prj.x + 0.5);
          yIdx = (int) (renderer.bhs * prj.y + 0.5);
        }
      }
      if (xIdx < 0 || xIdx >= renderer.rasterWidth)
        continue;
      if (yIdx < 0 || yIdx >= renderer.rasterHeight)
        continue;

      RenderColor color;
      if (p.rgbColor) {
        color = new RenderColor();
        color.red = p.redColor;
        color.green = p.greenColor;
        color.blue = p.blueColor;
      }
      else {
        // TODO
        ////        int colorIdx = (int) (p.color * renderer.paletteIdxScl + 0.5);
        ////        color = renderer.colorMap[colorIdx];
      }

      if (iter < blurMax) {
        for (int k = yIdx - blurRadius, yk = 0; k <= yIdx + blurRadius; k++, yk++) {
          if (k >= 0 && k < rasterHeight) {
            for (int l = xIdx - blurRadius, xk = 0; l <= xIdx + blurRadius; l++, xk++) {
              if (l >= 0 && l < rasterWidth) {
                // y, x
                AbstractRasterPoint rp = renderer.raster[k][l];
                double scl = blurKernel[yk][xk];
                // TODO

                ////                rp.setRed(rp.getRed() + color.red * scl * prj.intensity);
                ////                rp.setGreen(rp.getGreen() + color.green * scl * prj.intensity);
                ////                rp.setBlue(rp.getBlue() + color.blue * scl * prj.intensity);
                rp.incCount();
                if (observers != null && observers.size() > 0) {
                  for (IterationObserver observer : observers) {
                    observer.notifyIterationFinished(this, k, l);
                  }
                }
              }
            }
          }
        }
      }
      else {
        AbstractRasterPoint rp = renderer.raster[yIdx][xIdx];
        // TODO
        ////        rp.setRed(rp.getRed() + color.red * prj.intensity);
        ////        rp.setGreen(rp.getGreen() + color.green * prj.intensity);
        ////        rp.setBlue(rp.getBlue() + color.blue * prj.intensity);
        rp.incCount();
        if (observers != null && observers.size() > 0) {
          for (IterationObserver observer : observers) {
            observer.notifyIterationFinished(this, xIdx, yIdx);
          }
        }
      }
    }
  }

  @Override
  protected FlameRenderThreadState saveState() {
    FlameRenderBlurThreadState res = new FlameRenderBlurThreadState();
    res.currSample = currSample;
    res.xfIndex = (xf != null) ? flame.getXForms().indexOf(xf) : -1;
    res.startIter = iter;
    res.affineT = affineT != null ? affineT.makeCopy() : null;
    res.varT = varT != null ? varT.makeCopy() : null;
    res.p = p != null ? p.makeCopy() : null;
    res.q = q != null ? q.makeCopy() : null;
    return res;
  }

  @Override
  protected void restoreState(FlameRenderThreadState pState) {
    FlameRenderBlurThreadState state = (FlameRenderBlurThreadState) pState;
    currSample = state.currSample;
    xf = (state.xfIndex >= 0) ? flame.getXForms().get(state.xfIndex) : null;
    startIter = state.startIter;
    affineT = state.affineT != null ? state.affineT.makeCopy() : null;
    varT = state.varT != null ? state.varT.makeCopy() : null;
    p = state.p != null ? state.p.makeCopy() : null;
    q = state.q != null ? state.q.makeCopy() : null;
  }

  @Override
  protected void preFuseIter() {
    affineT = new XYZPoint(); // affine part of the transformation
    varT = new XYZPoint(); // complete transformation
    p = new XYZPoint();
    q = new XYZPoint();
    p.x = 2.0 * randGen.random() - 1.0;
    p.y = 2.0 * randGen.random() - 1.0;
    p.z = 0.0;
    p.color = randGen.random();

    xf = flame.getXForms().get(0);
    xf.transformPoint(ctx, affineT, varT, p, p);
    for (int i = 0; i <= Constants.INITIAL_ITERATIONS; i++) {
      xf = xf.getNextAppliedXFormTable()[randGen.random(Constants.NEXT_APPLIED_XFORM_TABLE_SIZE)];
      if (xf == null) {
        return;
      }
      xf.transformPoint(ctx, affineT, varT, p, p);
    }
  }

}
