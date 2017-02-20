package model;

import javax.swing.JFrame;

import view.Gui;

public class Launcher {
	public static final int ECB = 1;
	public static final int CBC = 2;
	
	public static void switchFrame(JFrame original, JFrame dest, boolean hide){
		dest.setVisible(true);
		original.setVisible(!hide);
	}
	
    public static void main(String[] args) {
			Gui g = new Gui();
	    	g.setVisible(true);	
    }
}