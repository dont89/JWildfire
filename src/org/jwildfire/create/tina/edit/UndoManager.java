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
package org.jwildfire.create.tina.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndoManager<T extends Assignable<T>> {
  private final Map<T, List<UndoItem<T>>> undoStack = new HashMap<T, List<UndoItem<T>>>();
  private final Map<T, Integer> undoStackPosition = new HashMap<T, Integer>();
  private boolean enabled = true;

  public void initUndoStack(T pInitialState) {
    if (undoStack.get(pInitialState) == null) {
      List<UndoItem<T>> items = new ArrayList<UndoItem<T>>();
      undoStack.put(pInitialState, items);
      undoStackPosition.put(pInitialState, -1);
    }
  }

  private List<UndoItem<T>> getUndoStack(T pInitialState) {
    List<UndoItem<T>> items = undoStack.get(pInitialState);
    if (items == null)
      throw new RuntimeException("initUndoStack was not called");
    return items;
  }

  public Integer getUndoStackPosition(T pInitialState) {
    Integer pos = undoStackPosition.get(pInitialState);
    if (pos == null)
      throw new RuntimeException("initUndoStack was not called");
    return pos;
  }

  public Integer getUndoStackSize(T pInitialState) {
    List<UndoItem<T>> items = undoStack.get(pInitialState);
    return items != null ? items.size() : 0;
  }

  public void saveUndoPoint(T pInitialState) {
    if (enabled) {
      List<UndoItem<T>> stack = getUndoStack(pInitialState);
      UndoItem<T> prevState = stack.size() > 0 ? stack.get(stack.size() - 1) : null;
      UndoItem<T> currState = new UndoItem<T>(pInitialState.makeCopy());
      if (prevState == null || !prevState.getData().isEqual(currState.getData())) {
        stack.add(currState);
      }
      undoStackPosition.put(pInitialState, stack.size() - 1);
    }
  }

  public void undo(T pInitialState) {
    List<UndoItem<T>> stack = getUndoStack(pInitialState);
    if (stack.size() > 0) {
      Integer pos = getUndoStackPosition(pInitialState);
      if (pos >= 0 && pos == stack.size() - 1) {
        UndoItem<T> prevState = stack.size() > 0 ? stack.get(stack.size() - 1) : null;
        UndoItem<T> currState = new UndoItem<T>(pInitialState.makeCopy());
        if (prevState == null || !prevState.getData().isEqual(currState.getData())) {
          stack.add(currState);
        }
        else {
          pos--;
        }
      }
      else {
        pos--;
      }
      undoStackPosition.put(pInitialState, pos);
      if (pos >= 0 && pos < stack.size()) {
        pInitialState.assign(stack.get(pos).getData());
      }
    }
  }

  public void redo(T pInitialState) {
    List<UndoItem<T>> stack = getUndoStack(pInitialState);
    Integer pos = getUndoStackPosition(pInitialState);
    pos++;
    undoStackPosition.put(pInitialState, pos);
    if (pos >= 0 && pos < stack.size()) {
      pInitialState.assign(stack.get(pos).getData());
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}