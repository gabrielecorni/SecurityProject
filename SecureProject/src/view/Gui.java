package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
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
import javax.swing.JTextArea;

import model.Critto;
import model.Launcher;

public class Gui extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final int maxUsers = 1;
	private static final String path = "content";

	private JLabel helloLabel = new JLabel();
	private JButton newBtn = new JButton("Registrazione Utente");
	private JButton removeBtn = new JButton("Rimozione Utente");
	private JButton clearBtn = new JButton("Pulisci Display");

	private JTextArea display = new JTextArea();

	private JLabel pswLabel = new JLabel("Passphrase:");
	private JPasswordField pphraseBox = new JPasswordField();
	private JButton inserisciBtn = new JButton("Inserisci pagina");
	private JButton leggiBtn = new JButton("Leggi diario");

	private JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
	private JPanel panel1 = new JPanel(new BorderLayout());
	private JPanel panel2 = new JPanel(new GridLayout(1, 2, 10, 10));
	private JPanel panel3 = new JPanel(new GridLayout(1, 2, 10, 10));

	private Critto critto;
	private byte[] iv;

	public Gui() {

		super("Cyber Diary Security Edition");

		try {
			critto = new Critto();
			this.iv = critto.mySecureRandom(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(600, 600));
		setResizable(false);

		decide();

		inserisciBtn.setEnabled(false);
		leggiBtn.setEnabled(false);

		newBtn.addActionListener(this);
		removeBtn.addActionListener(this);
		clearBtn.addActionListener(this);
		inserisciBtn.addActionListener(this);
		leggiBtn.addActionListener(this);

		pphraseBox.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				test();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				test();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				test();
			}
		});

		setLayout(new BorderLayout(10, 10));

		panel1.add(display, BorderLayout.CENTER);

		panel2.add(helloLabel);
		panel2.add(newBtn);
		panel2.add(removeBtn);
		panel2.add(clearBtn);

		panel3.add(pswLabel);
		panel3.add(pphraseBox);
		panel3.add(inserisciBtn);
		panel3.add(leggiBtn);

		mainPanel.add(panel2, BorderLayout.NORTH);
		mainPanel.add(panel1, BorderLayout.CENTER);
		mainPanel.add(panel3, BorderLayout.SOUTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(mainPanel);
	}

	private void decide() {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		if (folder.listFiles().length == maxUsers) {
			newBtn.setEnabled(false);
			removeBtn.setEnabled(true);
			helloLabel.setText("Ciao, " + folder.listFiles()[0].getName().split("[.]")[0].toUpperCase());
		} else {
			newBtn.setEnabled(true);
			removeBtn.setEnabled(false);
			helloLabel.setText("Per favore, registrati");
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source.equals(newBtn)) {
			//passo alla schermata di registrazione
			Launcher.switchFrame(this, new Login(), true);
		} else if (source.equals(removeBtn)) {
			//elimino il file di login e il file col testo cifrato
			if (conferma(critto.toBytes(pphraseBox.getPassword()))) {
				File folder = new File(path);
				for (File f : folder.listFiles()) {
					f.delete();
				}
				new File("story.bin").delete();
				decide();
				pphraseBox.setText("");
				display.setText("");
				JOptionPane.showMessageDialog(null, "utente rimosso correttamente");
			} else {
				JOptionPane.showMessageDialog(null, "passphrase errata (RIMOZIONE)");
			}
		} else if (source.equals(clearBtn)) {
			display.setEditable(true);
			display.setText("");
			pphraseBox.setText("");
		} else if (source.equals(inserisciBtn)) {
			cifra(critto.toBytes(pphraseBox.getPassword()), display.getText());
			pphraseBox.setText("");

		} else if (source.equals(leggiBtn)) {
			decifra(critto.toBytes(pphraseBox.getPassword()));
		}

	}

	private boolean conferma(byte[] pp) {
		try {
			// ottengo l'hash della pp, poi la elimino dal sistema
			byte[] ppHash = critto.mySecureHash(pp);
			Arrays.fill(pp, (byte) 0); // wiper

			// con ppHash devo leggere il file user e recuperare il segreto cifrato
			byte[] segretoCifrato = Files.readAllBytes(Paths.get(path + "/usr.bin"));

			// decifro il segreto
			byte[] ss = critto.decrypt(Launcher.ECB, ppHash, null, segretoCifrato);
			Arrays.fill(ss, (byte) 0); // wiper
			
			//se arrivo qui, ho decifrato con successo
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void test() {
		if (pphraseBox.getPassword().length > 0) {
			inserisciBtn.setEnabled(true);
			leggiBtn.setEnabled(true);
		} else {
			inserisciBtn.setEnabled(false);
			leggiBtn.setEnabled(false);
		}
	}

	private void cifra(byte[] pp, String text) {
		try {

			// ottengo l'hash della pp, poi la elimino dal sistema
			byte[] ppHash = critto.mySecureHash(pp);
			Arrays.fill(pp, (byte) 0); // wiper

			// con ppHash devo leggere il file user.txt e recuperare il segreto cifrato
			byte[] segretoCifrato = Files.readAllBytes(Paths.get(path + "/usr.bin"));

			// costruisco il nuovo msg da cifrare
			byte[] storiaCifrata = null;
			byte[] plainStory = null;
			boolean firstPage = false;
			int len = 0;

			try {
				// leggo la storia cifrata, se esiste continuo, sennò vado oltre
				storiaCifrata = Files.readAllBytes(Paths.get("story.bin"));

			} catch (Exception e) {
				// do nothing, è la prima pagina scritta
				firstPage = true;
			}

			// decifro il segreto
			byte[] ss = critto.decrypt(Launcher.ECB, ppHash, null, segretoCifrato);

			if (!firstPage) {
				// decifro la storia
				plainStory = critto.decrypt(Launcher.CBC, ss, iv, storiaCifrata);
				len = plainStory.length;
			}
			
			//dopo che ho decifrato il vecchio contenuto, cambio iv per la nuova cifratura
			//iv deve cambiare sempre per cifrare lo stesso file, così ho maggiore robustezza
			//per appendere, decifro il testo precedente, aggiungo il nuovo e cifro tutto con padding
			
			this.iv = critto.mySecureRandom(16);
			
			// appendo il testo alla plainStory
			int index = 0;
			byte[] separator = "\n============\n".getBytes();
			byte[] newStory = critto.toBytes(text.toCharArray());

			int siz = len + newStory.length + separator.length;
			byte[] toWr = new byte[siz];

			for (int i = 0; i < len; i++, index++) {
				toWr[index] = plainStory[i];
			}
			if (len > 0) {
				for (int i = 0; i < separator.length; i++, index++) {
					toWr[index] = separator[i];
				}
			}
			for (int i = 0; i < newStory.length; i++, index++) {
				toWr[index] = newStory[i];
			}

			// cifro il text col segreto
			byte[] cipherStory = critto.encrypt(Launcher.CBC, ss, iv, toWr);
			Arrays.fill(ss, (byte) 0); // wiper

			Files.write(Paths.get("story.bin"), cipherStory);

			JOptionPane.showMessageDialog(null, "passphrase valida, ho cifrato e inserito il messaggio");
			this.display.setText("");
			pphraseBox.setText("");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "passphrase errata (CIFRATURA)");
			this.pphraseBox.setText("");
			this.display.setText("");
		}

	}

	private void decifra(byte[] pp) {
		try {
			// leggo la storia cifrata
			byte[] storiaCifrata = Files.readAllBytes(Paths.get("story.bin"));

			// ottengo l'hash della pp, poi la elimino dal sistema
			byte[] ppHash = critto.mySecureHash(pp);
			Arrays.fill(pp, (byte) 0); // wiper

			// con ppHash devo leggere il file usr.bin e recuperare il segreto cifrato
			byte[] segretoCifrato = Files.readAllBytes(Paths.get(path + "/usr.bin"));

			try {

				// decifro il segreto
				byte[] ss = critto.decrypt(Launcher.ECB, ppHash, null, segretoCifrato);

				// decifro la storia
				byte[] plainStory = critto.decrypt(Launcher.CBC, ss, iv, storiaCifrata);
				Arrays.fill(ss, (byte) 0); // wiper

				// la metto a video in chiaro
				display.setText(new String(plainStory));

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "passphrase errata (DECIFRAZIONE)");
				this.pphraseBox.setText("");
				this.display.setText("");
				return;
			}

			JOptionPane.showMessageDialog(null, "passphrase corretta, decifro e visualizzo");
			this.pphraseBox.setText("");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "non c'è nessuna pagina inserita (DECIFRAZIONE)");
			this.pphraseBox.setText("");
			this.display.setText("");
		}

	}
}