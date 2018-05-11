package org.domain.computerVision.test;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.JFrame;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.domain.computerVision.Blobs;
import org.domain.computerVision.Image;
import org.domain.computerVision.ImageFormat;
import org.domain.computerVision.Monitor;
import org.domain.computerVision.Pixel;
import org.domain.computerVision.Rect;

import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JToggleButton;
import javax.swing.JCheckBox;

public class DesktopMonitorPlayer implements ActionListener, Runnable, KeyListener {
	private JFrame frame;
	private JButton btnPlayPause;
	private JLabel lblImg;
	private JList<String> list;
	private JLabel lblNewLabel;
	private JSpinner spinnerFactorChange;
	private JLabel lblSmallRegion;
	private JSpinner spinnerSmallWidth;
	private JSpinner spinnerSmallHeight;
	private JButton btnPrev;
	private JButton btnNext;
	private Monitor monitor; 
	private int[] argb;
	private Image imgDisplay;
	private boolean stop;
	private Thread thread;
	static String storagePath = ".dm";
	static String dataPath = ".dm" + File.separator + Monitor.version;
	private JButton btnChangeStorage;
	private JButton btnConvert;
	private JLabel lblBigest;
	private JSpinner spinnerBigWidth;
	private JSpinner spinnerBigHeight;
	private JCheckBox chckbxDrawBorder;
	private JCheckBox chckbxDrawDiff;
	private JCheckBox chckbxStop;
	private Object activeFolder;
	
	public void tests() {
		Rect a = new Rect(10, 10, 10, 10);
		Rect b = new Rect(20, 20, 10, 10);
		boolean fail = false;
		
		int distX_ab = Rect.distanceX(a, b);
		
		if (distX_ab != 1) {
			fail = true;
			System.out.println("fail 1");;
		}
		
		int distX_ba = Rect.distanceX(b, a);
		
		if (distX_ba != 1) {
			fail = true;
			System.out.println("fail 2");;
		}
		
		int distY_ab = Rect.distanceY(a, b);
		
		if (distY_ab != 1) {
			fail = true;
			System.out.println("fail 3");;
		}
		
		int distY_ba = Rect.distanceY(b, a);
		
		if (distY_ba != 1) {
			fail = true;
			System.out.println("fail 4");;
		}
		
		if (fail == false) {
			System.out.println("tests Ok");
		}
	}

	private void record() {
		try {
			this.monitor.setStoragePath(storagePath);
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Robot robot = new Robot();
			Image img = new Image(640, 480, 3, 32);
			
			while (true) {
				Dimension screenSize = toolkit.getScreenSize();
				Rectangle screenRect = new Rectangle(screenSize);
				BufferedImage bitmapPreview = robot.createScreenCapture(screenRect);
				this.argb = ImageFormat.loadBufferedImage(img, bitmapPreview, this.argb);
				this.monitor.pushFrame(null, img);
				// TODO : tentar trocar o sleep pela detecção de algum evento de teclado/mouse/rede
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			DesktopMonitorPlayer desktopMonitorPlayer = new DesktopMonitorPlayer();
			desktopMonitorPlayer.record();
		} else {
			DesktopMonitorPlayer.storagePath = args[0];
			DesktopMonitorPlayer.dataPath = DesktopMonitorPlayer.storagePath + File.separator + Monitor.version;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						DesktopMonitorPlayer window = new DesktopMonitorPlayer();
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the application.
	 */
	public DesktopMonitorPlayer() {
		this.stop = true;
		this.imgDisplay = new Image(640, 480, 3, 32);
		this.monitor = new Monitor(Monitor.RECORD_DESKTOP_TEXT);
		this.monitor.setStoragePath(storagePath);
		initialize();
		updateList();
		tests();
	}

	/**
	 * Initialize the contents of the frame.
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addKeyListener(this);
		frame.getContentPane().setMinimumSize(new Dimension(640, 480));
		frame.setBounds(100, 100, 1024, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.WEST);
		scrollPane.setMinimumSize(new Dimension(20, 480));
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		this.list = new JList<String>();
		list.addKeyListener(this);
		scrollPane.setViewportView(this.list);
		this.list.setFixedCellWidth(100);
		this.list.setMinimumSize(new Dimension(100, 480));
		this.list.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.list.setAutoscrolls(true);
		
		JPanel panel_1 = new JPanel();
		panel_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		frame.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		lblImg = new JLabel("");
		panel_1.add(lblImg, BorderLayout.CENTER);
		lblImg.setIcon(new ImageIcon(DesktopMonitorPlayer.class.getResource("/javax/swing/plaf/metal/icons/Inform.gif")));
		
		JPanel panel_2 = new JPanel();
		panel_2.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		panel_1.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		chckbxDrawBorder = new JCheckBox("Border");
		chckbxDrawBorder.setSelected(true);
		panel_2.add(chckbxDrawBorder);
		
		chckbxDrawDiff = new JCheckBox("Diff");
		chckbxDrawDiff.setSelected(true);
		panel_2.add(chckbxDrawDiff);
		
		chckbxStop = new JCheckBox("Stop");
		panel_2.add(chckbxStop);
		
		btnPlayPause = new JButton("Play");
		panel_2.add(btnPlayPause);
		btnPlayPause.addActionListener(this);
		
		btnPrev = new JButton("Prev");
		panel_2.add(btnPrev);
		btnPrev.addActionListener(this);
		
		btnNext = new JButton("Next");
		panel_2.add(btnNext);
		btnNext.addActionListener(this);
		
		btnChangeStorage = new JButton("Storage");
		panel_2.add(btnChangeStorage);
		btnChangeStorage.addActionListener(this);
		
		btnConvert = new JButton("Convert");
		panel_2.add(btnConvert);
		btnConvert.addActionListener(this);
		
		lblNewLabel = new JLabel("Change");
		panel_2.add(lblNewLabel);
		
		spinnerFactorChange = new JSpinner();
		spinnerFactorChange.setModel(new SpinnerNumberModel(40, 1, 100, 1));
		panel_2.add(spinnerFactorChange);
		
		lblSmallRegion = new JLabel("Small");
		panel_2.add(lblSmallRegion);
		
		spinnerSmallWidth = new JSpinner();
		spinnerSmallWidth.setModel(new SpinnerNumberModel(new Integer(10), new Integer(10), null, new Integer(10)));
		panel_2.add(spinnerSmallWidth);
		
		spinnerSmallHeight = new JSpinner();
		spinnerSmallHeight.setModel(new SpinnerNumberModel(new Integer(10), new Integer(10), null, new Integer(10)));
		panel_2.add(spinnerSmallHeight);
		
		lblBigest = new JLabel("Big");
		panel_2.add(lblBigest);
		
		spinnerBigWidth = new JSpinner();
		spinnerBigWidth.setModel(new SpinnerNumberModel(new Integer(300), new Integer(50), null, new Integer(50)));
		panel_2.add(spinnerBigWidth);
		
		spinnerBigHeight = new JSpinner();
		spinnerBigHeight.setModel(new SpinnerNumberModel(new Integer(300), new Integer(50), null, new Integer(50)));
		panel_2.add(spinnerBigHeight);
	}

	public void updateList() {
		this.frame.setTitle(dataPath);
		File root = new File(dataPath);
		
		if (root.exists() && root.isDirectory()) {
			String[] items = root.list();
			Arrays.sort(items);
			List<String> list = Arrays.asList(items);
			Collections.reverse(list);
			String[] folders = (String[])list.toArray();
			this.list.setListData(folders);
		}
	}

	
	private void btnPlayPauseClicked() {
		if (this.stop == true) {
			thread = new Thread(this);
			thread.start();
		} else {
			this.stop = true;
			
			try {
				this.thread.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == this.btnPlayPause) {
			btnPlayPauseClicked();
		} else if (source == this.btnChangeStorage) {
			JFileChooser chooser = new JFileChooser(storagePath);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = chooser.showOpenDialog(this.frame);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				this.dataPath = chooser.getCurrentDirectory().getAbsolutePath();
				this.dataPath = chooser.getSelectedFile().getAbsolutePath();
				updateList();
			}
		} else if (source == this.btnNext) {
			getImage(true);
		} else if (source == this.btnPrev) {
			getImage(false);
		} else if (source == this.btnConvert) {
			for (String folder : this.list.getSelectedValuesList()) {
				String foldPath = this.dataPath + File.separator + folder;
				this.monitor.convertOldVersion(foldPath);
			}
		}
	}
	
	private int getImage(boolean forward) {
		if (this.activeFolder == null) {
			this.activeFolder = this.list.getSelectedValue();
		}
		
		if (this.activeFolder == null) {
			return 0;
		}
		
		String foldPath = this.dataPath + File.separator + this.activeFolder;
		this.monitor.setStoragePath(storagePath);
		boolean drawBorder = this.chckbxDrawBorder.isSelected();
		boolean drawDiff = this.chckbxDrawDiff.isSelected();
		int rc = this.monitor.getImage(foldPath, this.imgDisplay, forward, drawBorder, drawDiff);
		int factorChange = this.monitor.getActiveFactorChange();
		Rect roi = this.monitor.getActiveDiffRegion();
		boolean factorChangeEvent = false;
		boolean roiSizeEvent = false;
		int roiBrightPercent = getRoiBrightPercent(this.imgDisplay, roi);
		boolean isEvent = false;

		if (factorChange < (Integer)this.spinnerFactorChange.getModel().getValue()) {
			factorChangeEvent = true;
		}
		
		if (roi.width > (Integer) this.spinnerSmallWidth.getModel().getValue() && roi.height > (Integer) this.spinnerSmallHeight.getModel().getValue()) {
			if (roi.width < (Integer) this.spinnerBigWidth.getModel().getValue() && roi.height < (Integer) this.spinnerBigHeight.getModel().getValue()) {
				roiSizeEvent = true;
			}
		}
		
		
		if (factorChangeEvent && roiSizeEvent && roiBrightPercent > 75) {
			isEvent = true;
		}

		String title = String.format("%d/%d - %s - %d%% - %d x %d - %d%% - %s - %s : %s", this.monitor.getActiveFileIndex(), this.monitor.getNumFiles(), this.monitor.getActiveFileName(), factorChange, roi.width, roi.height, roiBrightPercent, factorChangeEvent, roiSizeEvent, isEvent);
		this.frame.setTitle(title);

		Rect rectOut = this.imgDisplay.getRect();
		this.argb = this.imgDisplay.getARGB(this.argb);
		BufferedImage bufferedImageOut = new BufferedImage(rectOut.width, rectOut.height, BufferedImage.TYPE_INT_ARGB);
		bufferedImageOut.setRGB(0, 0, rectOut.width, rectOut.height, argb, 0, rectOut.width);
		int width = lblImg.getWidth();
		int height = lblImg.getHeight();
		java.awt.Image imgScaled;
		
		if (rectOut.width > width || rectOut.height > height) {
			imgScaled = bufferedImageOut.getScaledInstance(width, height, java.awt.Image.SCALE_DEFAULT);
		} else {
			imgScaled = bufferedImageOut;
		}
		
		ImageIcon icon = new ImageIcon(imgScaled);
		this.lblImg.setIcon(icon);
		
		if (isEvent) {
			if (this.chckbxStop.isSelected()) {
				this.stop = true;
			} else {
//				try {
//					if (rc == 2) {
//						Thread.sleep(2000);
//					} else {
//						Thread.sleep(2000);
//					}
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			}
		}
		
		return rc;
	}
	
	private int getRoiBrightPercent(Image img, Rect roi) {
		Pixel pixelMean = new Pixel(img);
		img.mean(roi, pixelMean.values);
		int val = pixelMean.getBrightPercent();
		return val;
	}
	



	@Override
	public void run() {
		btnPlayPause.setText("Pause");
		// rc == 0 : não tem mais imagens
		// rc == 1 : tem imagem do mesmo frame
		// rc == 2 : tem imagem do próximo frame
		this.stop = false;
		int rc;

		for (String folder : this.list.getSelectedValuesList()) {
			this.activeFolder = folder;
			
			do {
				rc = getImage(true);
			} while (this.stop == false && rc > 0);

			if (this.stop == true) {
				break;
			}
		}
		
		btnPlayPause.setText("Play");
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		
		if (key == arg0.VK_LEFT) {
			getImage(false);
		} else if (key == arg0.VK_RIGHT) {
			getImage(true);
		} else if (key == arg0.VK_SPACE) {
			System.out.println("Play/Pause");
			btnPlayPauseClicked();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
