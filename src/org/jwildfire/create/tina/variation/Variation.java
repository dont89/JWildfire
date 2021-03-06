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
package org.jwildfire.create.tina.variation;

import static org.jwildfire.base.mathlib.MathLib.EPSILON;
import static org.jwildfire.base.mathlib.MathLib.fabs;

import java.io.Serializable;

import org.jwildfire.create.tina.animate.AnimAware;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.edit.Assignable;

public class Variation implements Assignable<Variation>, Serializable {
  private static final long serialVersionUID = 1L;
  @AnimAware
  private double amount;
  @AnimAware
  private VariationFunc func;

  public Variation() {

  }

  public Variation(double pAmount, VariationFunc pFunc) {
    amount = pAmount;
    func = pFunc;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double pAmount) {
    this.amount = pAmount;
  }

  public VariationFunc getFunc() {
    return func;
  }

  public void setFunc(VariationFunc func) {
    this.func = func;
  }

  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP) {
    func.transform(pContext, pXForm, pAffineTP, pVarTP, amount);
  }

  @Override
  public String toString() {
    return func.getName() + "(" + amount + ")";
  }

  @Override
  public void assign(Variation var) {
    amount = var.amount;
    func = VariationFuncList.getVariationFuncInstance(var.func.getName());

    // params
    {
      String[] paramNames = var.func.getParameterNames();
      if (paramNames != null) {
        for (int i = 0; i < paramNames.length; i++) {
          Object val = var.func.getParameterValues()[i];
          if (val instanceof Double) {
            func.setParameter(paramNames[i], (Double) val);
          }
          else if (val instanceof Integer) {
            func.setParameter(paramNames[i], Double.valueOf(((Integer) val)));
          }
          else {
            throw new IllegalStateException();
          }
        }
      }
    }
    // ressources
    {
      String[] ressNames = var.func.getRessourceNames();
      if (ressNames != null) {
        for (int i = 0; i < ressNames.length; i++) {
          byte[] val = var.func.getRessourceValues()[i];
          func.setRessource(ressNames[i], val);
        }
      }
    }

  }

  @Override
  public Variation makeCopy() {
    Variation res = new Variation();
    res.assign(this);
    return res;
  }

  @Override
  public boolean isEqual(Variation pSrc) {
    if (fabs(amount - pSrc.amount) > EPSILON ||
        ((func != null && pSrc.func == null) || (func == null && pSrc.func != null) ||
        (func != null && pSrc.func != null && !func.getName().equals(pSrc.func.getName())))) {
      return false;
    }
    // param values
    {
      Object vals[] = func.getParameterValues();
      if (vals != null) {
        Object srcVals[] = pSrc.func.getParameterValues();
        for (int i = 0; i < vals.length; i++) {
          Object o = vals[i];
          Object s = srcVals[i];
          if ((o != null && s == null) || (o == null && s != null)) {
            return false;
          }
          else if (o != null && s != null) {
            if (o instanceof Integer) {
              if (((Integer) o).intValue() != ((Integer) s).intValue()) {
                return false;
              }
            }
            else if (o instanceof Double) {
              if (fabs(((Double) o).doubleValue() - ((Double) s).doubleValue()) > EPSILON) {
                return false;
              }
            }
            else {
              throw new IllegalStateException();
            }
          }
        }
      }
    }
    // ressources
    {
      byte vals[][] = func.getRessourceValues();
      if (vals != null) {
        byte srcVals[][] = pSrc.func.getRessourceValues();
        for (int i = 0; i < vals.length; i++) {
          byte[] o = vals[i];
          byte[] s = srcVals[i];
          if ((o != null && s == null) || (o == null && s != null) || (o != null && s != null && o.length != s.length)) {
            return false;
          }
          if (o != null && s != null) {
            for (int j = 0; j < o.length; j++) {
              if (o[j] != s[j]) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }
}
