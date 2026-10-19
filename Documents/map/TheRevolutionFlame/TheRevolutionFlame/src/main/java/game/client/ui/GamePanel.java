package game.client.ui;

import game.common.dto.GameResponse;
import javax.swing.*;
import java.awt.*;
import game.client.util.ImageCacheManager;

public class GamePanel extends JPanel {

    private JTextPane txtLogConsole;
    private JTextField txtInputComando;
    
    // Componenti di stato
    private JLabel lblSalute;
    private JLabel lblArmaEquipaggiata;
    private JTextArea txtInventarioScorte;
    private MainFrame mainFrame;

    // Immagini per il rendering a strati
    private Image imgSfondoCorrente = null;
    private Image imgNemicoCorrente = null;
    private Image imgGiocatoreCorrente = null;
    private boolean inCombattimentoAttivo = false;

    public GamePanel(MainFrame mainFrame) {
        this.setLayout(new BorderLayout());
        this.mainFrame = mainFrame;

        // Carica lo sprite del tuo personaggio
        ImageIcon iconaGiocatore = ImageCacheManager.getIcona("personaggi/eroe.png");
        if (iconaGiocatore != null) {
            this.imgGiocatoreCorrente = iconaGiocatore.getImage();
        }

        // Area grafica centrale
        JPanel pnlVisual = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // 1. STRATO SFONDO (Stanza o Mappa)
                if (imgSfondoCorrente != null) {
                    g2d.drawImage(imgSfondoCorrente, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                
                // 2. STRATO COMBATTIMENTO (Giocatore a sinistra, Nemico a destra)
                if (inCombattimentoAttivo) {
                    int pnlW = getWidth();
                    int pnlH = getHeight();
                    int hSprite = (int) (pnlH * 0.60);

                    // Giocatore
                    if (imgGiocatoreCorrente != null) {
                        int wOrig = imgGiocatoreCorrente.getWidth(null);
                        int hOrig = imgGiocatoreCorrente.getHeight(null);
                        if (hOrig > 0) {
                            int wGiocatore = (wOrig * hSprite) / hOrig;
                            int xGiocatore = (int) (pnlW * 0.10);
                            int yGiocatore = pnlH - hSprite - 20;
                            g2d.drawImage(imgGiocatoreCorrente, xGiocatore, yGiocatore, wGiocatore, hSprite, this);
                        }
                    }

                    // Nemico
                    if (imgNemicoCorrente != null) {
                        int wOrig = imgNemicoCorrente.getWidth(null);
                        int hOrig = imgNemicoCorrente.getHeight(null);
                        if (hOrig > 0) {
                            int wNemico = (wOrig * hSprite) / hOrig;
                            int xNemico = pnlW - wNemico - (int) (pnlW * 0.10);
                            int yNemico = pnlH - hSprite - 20;
                            g2d.drawImage(imgNemicoCorrente, xNemico, yNemico, wNemico, hSprite, this);
                        }
                    }
                }
            }
        };
        pnlVisual.setBackground(Color.BLACK);
        pnlVisual.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        
        this.add(pnlVisual, BorderLayout.CENTER);

        // Barra di stato a destra
        JPanel pnlStato = new JPanel();
        pnlStato.setLayout(new BoxLayout(pnlStato, BoxLayout.Y_AXIS)); 
        pnlStato.setPreferredSize(new Dimension(200, 0));
        pnlStato.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Stato Operativo"));
        pnlStato.setBackground(Color.DARK_GRAY);

        lblSalute = new JLabel("Salute: 100%");
        lblSalute.setForeground(Color.GREEN);
        lblSalute.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblArmaEquipaggiata = new JLabel("Arma: NESSUNA");
        lblArmaEquipaggiata.setForeground(Color.WHITE);
        
        txtInventarioScorte = new JTextArea(10, 15);
        txtInventarioScorte.setEditable(false);
        txtInventarioScorte.setBackground(Color.DARK_GRAY);
        txtInventarioScorte.setForeground(Color.LIGHT_GRAY);
        txtInventarioScorte.setText("Scorte:\n- vuoto");

        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblSalute);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblArmaEquipaggiata);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlStato.add(new JScrollPane(txtInventarioScorte));
        
        this.add(pnlStato, BorderLayout.EAST);

        // Terminale comandi in basso
        JPanel pnlTerminale = new JPanel(new BorderLayout());
        
        txtLogConsole = new JTextPane();
        txtLogConsole.setEditable(false);
        txtLogConsole.setBackground(Color.BLACK);
        txtLogConsole.setForeground(Color.WHITE);
        
        JScrollPane scrollConsole = new JScrollPane(txtLogConsole);
        scrollConsole.setPreferredSize(new Dimension(0, 150));
        
        txtInputComando = new JTextField();
        txtInputComando.setBackground(Color.BLACK);
        txtInputComando.setForeground(Color.GREEN);
        txtInputComando.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtInputComando.setCaretColor(Color.GREEN);
        txtInputComando.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        txtInputComando.setPreferredSize(new Dimension(0, 40));
        
        txtInputComando.addActionListener(e -> { 
            String testoInserito = txtInputComando.getText().trim();
            if(!testoInserito.isEmpty()){
                if (testoInserito.toLowerCase().startsWith("id:")) {
                    mainFrame.inviaAzioneAlServer(testoInserito, "");
                } else {
                    String[] parti = testoInserito.split(" ", 2); 
                    String azione = parti[0].toUpperCase(); 
                    String argomento = (parti.length > 1) ? parti[1].trim() : ""; 
                    mainFrame.inviaAzioneAlServer(azione, argomento);
                }
                txtInputComando.setText("");
            }
        });

        pnlTerminale.add(scrollConsole, BorderLayout.CENTER);
        pnlTerminale.add(txtInputComando, BorderLayout.SOUTH);

        this.add(pnlTerminale, BorderLayout.SOUTH);
    }

    public void renderizzaStato(GameResponse response) {
        if (response.getMessaggio() != null && !response.getMessaggio().isEmpty()) {
            aggiungiTestoAllaConsole(response.getMessaggio());
        }

        lblSalute.setText("Salute: " + response.getPlayerHp() + "/" + response.getPlayerMaxHp());
        
        if (response.getPlayerHp() <= 20) {
            lblSalute.setForeground(Color.RED);
        } else if (response.getPlayerHp() <= 40) {
            lblSalute.setForeground(Color.YELLOW);
        } else {
            lblSalute.setForeground(Color.GREEN);
        }

        // 1. SFONDO
        String percorsoSfondo = response.getMappaPngPath();
        if (percorsoSfondo == null || percorsoSfondo.isEmpty()) {
            percorsoSfondo = response.getStanzaSfondoPath();
        }
        
        if (percorsoSfondo != null && !percorsoSfondo.isEmpty()) {
            ImageIcon iconaSfondo = ImageCacheManager.getIcona(percorsoSfondo);
            this.imgSfondoCorrente = (iconaSfondo != null) ? iconaSfondo.getImage() : null;
        } else {
            this.imgSfondoCorrente = null;
        }

        // 2. NEMICO
        this.inCombattimentoAttivo = response.isInCombattimento();
        if (inCombattimentoAttivo && response.getEnemySpritePath() != null && !response.getEnemySpritePath().isEmpty()) {
            ImageIcon iconaNemico = ImageCacheManager.getIcona(response.getEnemySpritePath());
            this.imgNemicoCorrente = (iconaNemico != null) ? iconaNemico.getImage() : null;
        } else {
            this.imgNemicoCorrente = null;
        }

        this.repaint();

        if (response.isInCombattimento()) {
            aggiungiTestoAllaConsole(">> COMBATTIMENTO IN CORSO! HP Nemico: " + response.getEnemyHp());
        }

        if (response.isGameOver() || response.isVittoria()) {
            txtInputComando.setEnabled(false);
            txtInputComando.setText("PARTITA TERMINATA.");
            
            if (response.isVittoria()) {
                aggiungiTestoAllaConsole("\n*** HAI VINTO! ***");
            } else {
                aggiungiTestoAllaConsole("\n*** GAME OVER ***");
            }
        }
        
        txtInputComando.setText("");
    }

    private void aggiungiTestoAllaConsole(String testo) {
        try {
            javax.swing.text.Document doc = txtLogConsole.getDocument();
            doc.insertString(doc.getLength(), testo + "\n\n", null);
            txtLogConsole.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void mostraSchermataIniziale(){
        aggiungiTestoAllaConsole("Avvio del terminale...");
        aggiungiTestoAllaConsole("Benvenuto su The Revolution Flame. Attesa connessione...");
        
        txtInputComando.setEnabled(true);
        txtInputComando.requestFocusInWindow(); 
    }
}
