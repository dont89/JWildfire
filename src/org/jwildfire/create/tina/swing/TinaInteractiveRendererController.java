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
package org.jwildfire.create.tina.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;

import org.jwildfire.base.Prefs;
import org.jwildfire.base.ResolutionProfile;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.io.Flam3Reader;
import org.jwildfire.create.tina.io.Flam3Writer;
import org.jwildfire.create.tina.randomflame.RandomFlameGenerator;
import org.jwildfire.create.tina.randomflame.RandomFlameGeneratorList;
import org.jwildfire.create.tina.randomflame.RandomFlameGeneratorSampler;
import org.jwildfire.create.tina.render.FlameRenderThread;
import org.jwildfire.create.tina.render.FlameRenderer;
import org.jwildfire.create.tina.render.IterationObserver;
import org.jwildfire.create.tina.render.RenderInfo;
import org.jwildfire.create.tina.render.RenderedFlame;
import org.jwildfire.image.SimpleImage;
import org.jwildfire.io.ImageWriter;
import org.jwildfire.swing.ErrorHandler;
import org.jwildfire.swing.ImageFileChooser;
import org.jwildfire.swing.ImagePanel;

public class TinaInteractiveRendererController implements IterationObserver {
  private enum State {
    IDLE, RENDER
  }

  private final TinaController parentCtrl;
  private final Prefs prefs;
  private final ErrorHandler errorHandler;
  private final JButton loadFlameButton;
  private final JButton fromClipboardButton;
  private final JButton nextButton;
  private final JButton stopButton;
  private final JButton toClipboardButton;
  private final JButton saveImageButton;
  private final JButton saveFlameButton;
  private final JComboBox randomStyleCmb;
  private final JToggleButton halveSizeButton;
  private final JComboBox interactiveResolutionProfileCmb;

  private final JPanel imageRootPanel;
  private JScrollPane imageScrollPane;
  private final JTextArea statsTextArea;
  private SimpleImage image;
  private Flame currFlame;
  private List<FlameRenderThread> threads;
  private FlameRenderer renderer;
  private State state = State.IDLE;

  public TinaInteractiveRendererController(TinaController pParentCtrl, ErrorHandler pErrorHandler, Prefs pPrefs,
      JButton pLoadFlameButton, JButton pFromClipboardButton, JButton pNextButton,
      JButton pStopButton, JButton pToClipboardButton, JButton pSaveImageButton, JButton pSaveFlameButton,
      JComboBox pRandomStyleCmb, JPanel pImagePanel, JTextArea pStatsTextArea, JToggleButton pHalveSizeButton,
      JComboBox pInteractiveResolutionProfileCmb) {
    parentCtrl = pParentCtrl;
    prefs = pPrefs;
    errorHandler = pErrorHandler;

    loadFlameButton = pLoadFlameButton;
    fromClipboardButton = pFromClipboardButton;
    nextButton = pNextButton;
    stopButton = pStopButton;
    toClipboardButton = pToClipboardButton;
    saveImageButton = pSaveImageButton;
    saveFlameButton = pSaveFlameButton;
    randomStyleCmb = pRandomStyleCmb;
    halveSizeButton = pHalveSizeButton;
    interactiveResolutionProfileCmb = pInteractiveResolutionProfileCmb;
    imageRootPanel = pImagePanel;
    // interactiveResolutionProfileCmb must be already filled here!
    refreshImagePanel();
    statsTextArea = pStatsTextArea;
    state = State.IDLE;
    genRandomFlame();
    enableControls();
  }

  private ResolutionProfile getResolutionProfile() {
    ResolutionProfile res = (ResolutionProfile) interactiveResolutionProfileCmb.getSelectedItem();
    if (res == null) {
      res = new ResolutionProfile(false, 800, 600);
    }
    return res;
  }

  private void refreshImagePanel() {
    if (imageScrollPane != null) {
      imageRootPanel.remove(imageScrollPane);
      imageScrollPane = null;
    }
    ResolutionProfile profile = getResolutionProfile();
    int width = profile.getWidth();
    int height = profile.getHeight();
    if (halveSizeButton.isSelected()) {
      width /= 2;
      height /= 2;
    }
    image = new SimpleImage(width, height);
    image.fillBackground(prefs.getTinaRandomBatchBGColorRed(), prefs.getTinaRandomBatchBGColorGreen(), prefs.getTinaRandomBatchBGColorBlue());
    ImagePanel imagePanel = new ImagePanel(image, 0, 0, image.getImageWidth());
    imagePanel.setSize(image.getImageWidth(), image.getImageHeight());
    imagePanel.setPreferredSize(new Dimension(image.getImageWidth(), image.getImageHeight()));

    imageScrollPane = new JScrollPane(imagePanel);
    imageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    imageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    imageRootPanel.add(imageScrollPane, BorderLayout.CENTER);

    imageRootPanel.getParent().validate();
  }

  public void enableControls() {
    saveImageButton.setEnabled(image != null);
    stopButton.setEnabled(state == State.RENDER);
  }

  public void genRandomFlame() {
    final int IMG_WIDTH = 80;
    final int IMG_HEIGHT = 60;
    final int PALETTE_SIZE = 11;

    RandomFlameGenerator randGen = RandomFlameGeneratorList.getRandomFlameGeneratorInstance((String) randomStyleCmb.getSelectedItem(), true);
    RandomFlameGeneratorSampler sampler = new RandomFlameGeneratorSampler(IMG_WIDTH, IMG_HEIGHT, prefs, randGen, false, false, PALETTE_SIZE);
    currFlame = sampler.createSample().getFlame();
  }

  public void fromClipboardButton_clicked() {
    Flame newFlame = null;
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents(clipboard);
      if (clipData != null) {
        if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          String xml = (String) (clipData.getTransferData(
              DataFlavor.stringFlavor));
          List<Flame> flames = new Flam3Reader().readFlamesfromXML(xml);
          if (flames.size() > 0) {
            newFlame = flames.get(0);
          }
        }
      }
      if (newFlame == null) {
        throw new Exception("There is currently no valid flame in the clipboard");
      }
      else {
        currFlame = newFlame;
        cancelRender();
        renderButton_clicked();
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }

  }

  public void loadFlameButton_clicked() {
    try {
      JFileChooser chooser = new FlameFileChooser(prefs);
      if (prefs.getInputFlamePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getInputFlamePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (chooser.showOpenDialog(imageRootPanel) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        List<Flame> flames = new Flam3Reader().readFlames(file.getAbsolutePath());
        Flame newFlame = flames.get(0);
        prefs.setLastInputFlameFile(file);
        currFlame = newFlame;
        cancelRender();
        renderButton_clicked();
        enableControls();
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void renderButton_clicked() {
    ResolutionProfile profile = getResolutionProfile();
    int width = profile.getWidth();
    int height = profile.getHeight();
    if (halveSizeButton.isSelected()) {
      width /= 2;
      height /= 2;
    }
    RenderInfo info = new RenderInfo(width, height);
    Flame flame = getCurrFlame();
    double wScl = (double) info.getImageWidth() / (double) flame.getWidth();
    double hScl = (double) info.getImageHeight() / (double) flame.getHeight();
    flame.setPixelsPerUnit((wScl + hScl) * 0.5 * flame.getPixelsPerUnit());
    flame.setWidth(info.getImageWidth());
    flame.setHeight(info.getImageHeight());
    // TODO
    boolean renderHDR = prefs.isTinaRenderHighHDR();
    info.setRenderHDR(renderHDR);
    // TODO
    boolean renderHDRIntensityMap = prefs.isTinaRenderHighHDRIntensityMap();
    info.setRenderHDRIntensityMap(renderHDRIntensityMap);
    // TODO
    flame.setSpatialFilterRadius(prefs.getTinaRenderHighFilterRadius());
    renderer = new FlameRenderer(flame, prefs);
    renderer.registerIterationObserver(this);
    threads = renderer.startRenderFlame(info);

    state = State.RENDER;
    enableControls();
  }

  public void stopButton_clicked() {
    cancelRender();
    enableControls();
  }

  private void cancelRender() {
    if (state == State.RENDER) {
      while (true) {
        boolean done = true;
        for (FlameRenderThread thread : threads) {
          if (!thread.isFinished()) {
            done = false;
            thread.cancel();
            try {
              Thread.sleep(1);
            }
            catch (InterruptedException e) {
              e.printStackTrace();
            }
            break;
          }
        }
        if (done) {
          break;
        }
      }
      state = State.IDLE;
    }
  }

  public void saveImageButton_clicked() {
    try {
      JFileChooser chooser = new ImageFileChooser();
      if (prefs.getOutputImagePath() != null) {
        try {
          chooser.setCurrentDirectory(new File(prefs.getOutputImagePath()));
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      if (chooser.showSaveDialog(imageRootPanel) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        prefs.setLastOutputImageFile(file);
        RenderedFlame res = renderer.finishRenderFlame();
        new ImageWriter().saveImage(res.getImage(), file.getAbsolutePath());
        if (res.getHDRImage() != null) {
          new ImageWriter().saveImage(res.getHDRImage(), file.getAbsolutePath() + ".hdr");
        }
        if (res.getHDRIntensityMap() != null) {
          new ImageWriter().saveImage(res.getHDRIntensityMap(), file.getAbsolutePath() + ".intensity.hdr");
        }
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }

  }

  public Flame getCurrFlame() {
    return currFlame;
  }

  // TODO stats
  private long smpl = 0;

  @Override
  public void notifyIterationFinished(FlameRenderThread pEventSource, int pX, int pY) {
    int x = pX;
    int y = pY;
    if (x >= 0 && x < image.getImageWidth() && y >= 0 && y < image.getImageHeight()) {
      image.setARGB(x, y, pEventSource.getTonemapper().tonemapSample(pX, pY));
      if (smpl++ % 1000 == 0) {
        imageRootPanel.repaint();
      }
    }
  }

  public void nextButton_clicked() {
    cancelRender();
    clearScreen();
    genRandomFlame();
    renderButton_clicked();
    enableControls();
  }

  private void clearScreen() {
    image.fillBackground(prefs.getTinaRandomBatchBGColorRed(), prefs.getTinaRandomBatchBGColorGreen(), prefs.getTinaRandomBatchBGColorBlue());
    imageRootPanel.repaint();
  }

  public void saveFlameButton_clicked() {
    try {
      Flame currFlame = getCurrFlame();
      if (currFlame != null) {
        JFileChooser chooser = new FlameFileChooser(prefs);
        if (prefs.getOutputFlamePath() != null) {
          try {
            chooser.setCurrentDirectory(new File(prefs.getOutputFlamePath()));
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        if (chooser.showSaveDialog(imageRootPanel) == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          new Flam3Writer().writeFlame(currFlame, file.getAbsolutePath());
          prefs.setLastOutputFlameFile(file);
        }
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void toClipboardButton_clicked() {
    try {
      Flame currFlame = getCurrFlame();
      if (currFlame != null) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String xml = new Flam3Writer().getFlameXML(currFlame);
        StringSelection data = new StringSelection(xml);
        clipboard.setContents(data, data);
      }
    }
    catch (Throwable ex) {
      errorHandler.handleError(ex);
    }
  }

  public void halveSizeButton_clicked() {
    boolean rendering = state == State.RENDER;
    if (rendering) {
      stopButton_clicked();
    }
    refreshImagePanel();
    enableControls();
    if (rendering) {
      renderButton_clicked();
    }
  }

  public void resolutionProfile_changed() {
    if (!parentCtrl.cmbRefreshing) {
      // Nothing special here
      halveSizeButton_clicked();
    }
  }

}
