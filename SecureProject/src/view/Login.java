package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import model.Critto;
import model.Launcher;

public class Login extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final String path = "content";

	private JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
	private JPanel panel1 = new JPanel(new GridLayout(1, 2, 10, 10));
	private JPanel panel2 = new JPanel(new GridLayout(1, 2, 10, 10));
	private JPanel panel3 = new JPanel(new GridLayout(1, 2, 10, 10));

	private JLabel nickLabel = new JLabel("Nickname:");
	private JTextField nickBox = new JTextField();

	private JLabel pswLabel = new JLabel("Passphrase:");
	private JPasswordField pphraseBox = new JPasswordField();

	private JButton okBtn = new JButton("Ok");
	private JButton noBtn = new JButton("Annulla");

	private Critto critto;
	
	public Login() {

		super("Login");
		
		try{
			critto = new Critto();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(new Dimension(500, 136));
		setLayout(new BorderLayout(10, 10));

		okBtn.addActionListener(this);
		noBtn.addActionListener(this);

		panel1.add(nickLabel);
		panel1.add(nickBox);
		panel2.add(pswLabel);
		panel2.add(pphraseBox);
		panel3.add(okBtn);
		panel3.add(noBtn);

		mainPanel.add(panel1, BorderLayout.NORTH);
		mainPanel.add(panel2, BorderLayout.CENTER);
		mainPanel.add(panel3, BorderLayout.SOUTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(mainPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(okBtn)) {
			salva(nickBox.getText(), critto.toBytes(pphraseBox.getPassword()));
			pphraseBox.setText("");
			JOptionPane.showMessageDialog(null, "nuovo utente inserito correttamente");
		}
		Launcher.switchFrame(this, new Gui(), true);

	}

	private void salva(String nick, byte[] pp) {

		try {
			//ottengo l'hash della pp, poi la elimino dal sistema
			byte[] ppHash = critto.mySecureHash(pp);
			Arrays.fill(pp, (byte) 0); //wiper
			
			//genero il portachiavi di 128 bit
			byte[] segreto = critto.mySecureRandom(16);
			
			//cifro il portachiavi con l'hash della pp, poi lo elimino subito dal sistema
			byte[] cipherText = critto.encrypt(Launcher.ECB, ppHash, null, segreto);
			Arrays.fill(segreto, (byte) 0); //wiper
			
			//memorizzo il portachiavi cifrato in una cella di memoria assunta sicura
			Files.write(Paths.get(path + "/usr.bin"),cipherText);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
}
