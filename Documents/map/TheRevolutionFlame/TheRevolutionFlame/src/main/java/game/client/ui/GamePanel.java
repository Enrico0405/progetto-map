package game.client.ui;

import game.common.dto.GameResponse;
import game.client.util.ImageCacheManager;
import game.client.util.SoundManager;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

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

    // Tracciamento della traccia musicale attualmente in riproduzione
    private String tracciaMusicaleAttuale = "";

    // Palette Colori Retro Style
    private static final Color SFONDO_RETRO = new Color(20, 20, 28);
    private static final Color CORNICE_ESTERNA = new Color(180, 140, 60);
    private static final Color CORNICE_INTERNA = new Color(40, 40, 55);
    private static final Color TESTO_VERDE = new Color(50, 220, 100);

    public GamePanel(MainFrame mainFrame) {
        this.setLayout(new BorderLayout(5, 5));
        this.setBackground(SFONDO_RETRO);
        this.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        this.mainFrame = mainFrame;

        // Carica lo sprite del personaggio provando i vari percorsi possibili
        this.imgGiocatoreCorrente = caricaSpriteGiocatore(null);

        // ---------------------------------------------------------------------
        // 1. AREA GRAFICA CENTRALE (PIXEL ART & LAYERED RENDERING)
        // ---------------------------------------------------------------------
        JPanel pnlVisual = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Attiva la resa Pixel Art nitida
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // STRATO 1: SFONDO (Mappa / Stanza)
                if (imgSfondoCorrente != null) {
                    g2d.drawImage(imgSfondoCorrente, 0, 0, w, h, this);
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, w, h);
                }

                // STRATO 2: PERSONAGGI IN COMBATTIMENTO
                if (inCombattimentoAttivo) {
                    int hSpriteGiocatore = (int) (h * 0.48);
                    int hSpriteNemico = (int) (h * 0.68);

                    // Giocatore a sinistra
                    if (imgGiocatoreCorrente != null) {
                        int wOrig = imgGiocatoreCorrente.getWidth(null);
                        int hOrig = imgGiocatoreCorrente.getHeight(null);
                        if (hOrig > 0) {
                            int wGiocatore = (wOrig * hSpriteGiocatore) / hOrig;
                            int xGiocatore = (int) (w * 0.08);
                            int yGiocatore = h - hSpriteGiocatore - 15;
                            g2d.drawImage(imgGiocatoreCorrente, xGiocatore, yGiocatore, wGiocatore, hSpriteGiocatore, this);
                        }
                    }

                    // Nemico a destra
                    if (imgNemicoCorrente != null) {
                        int wOrig = imgNemicoCorrente.getWidth(null);
                        int hOrig = imgNemicoCorrente.getHeight(null);
                        if (hOrig > 0) {
                            int wNemico = (wOrig * hSpriteNemico) / hOrig;
                            int xNemico = w - wNemico - (int) (w * 0.08);
                            int yNemico = h - hSpriteNemico - 15;
                            g2d.drawImage(imgNemicoCorrente, xNemico, yNemico, wNemico, hSpriteNemico, this);
                        }
                    }
                }
            }
        };

        pnlVisual.setBackground(Color.BLACK);
        pnlVisual.setBorder(new CompoundBorder(
                new LineBorder(CORNICE_ESTERNA, 2),
                new BevelBorder(BevelBorder.LOWERED, CORNICE_INTERNA, Color.BLACK)
        ));
        
        this.add(pnlVisual, BorderLayout.CENTER);

        // ---------------------------------------------------------------------
        // 2. PANNELLO LATERALE (STATO OPERATIVO)
        // ---------------------------------------------------------------------
        JPanel pnlStato = new JPanel();
        pnlStato.setLayout(new BoxLayout(pnlStato, BoxLayout.Y_AXIS)); 
        pnlStato.setPreferredSize(new Dimension(210, 0));
        pnlStato.setBackground(SFONDO_RETRO);
        pnlStato.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(CORNICE_ESTERNA, 1), 
                " STATO EROI ", 
                0, 0, 
                new Font("Monospaced", Font.BOLD, 12), 
                CORNICE_ESTERNA
        ));

        lblSalute = new JLabel("Salute: 100%");
        lblSalute.setForeground(TESTO_VERDE);
        lblSalute.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        lblArmaEquipaggiata = new JLabel("Arma: NESSUNA");
        lblArmaEquipaggiata.setForeground(Color.CYAN);
        lblArmaEquipaggiata.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        txtInventarioScorte = new JTextArea(10, 15);
        txtInventarioScorte.setEditable(false);
        txtInventarioScorte.setBackground(new Color(12, 12, 18));
        txtInventarioScorte.setForeground(Color.LIGHT_GRAY);
        txtInventarioScorte.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtInventarioScorte.setText("Scorte:\n- Vuoto");
        txtInventarioScorte.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane scrollInventario = new JScrollPane(txtInventarioScorte);
        scrollInventario.setBorder(new LineBorder(CORNICE_INTERNA, 1));

        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblSalute);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlStato.add(lblArmaEquipaggiata);
        pnlStato.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlStato.add(scrollInventario);
        
        this.add(pnlStato, BorderLayout.EAST);

        // ---------------------------------------------------------------------
        // 3. TERMINALE COMANDI IN BASSO (RETRO CONSOLE)
        // ---------------------------------------------------------------------
        JPanel pnlTerminale = new JPanel(new BorderLayout(0, 4));
        pnlTerminale.setBackground(SFONDO_RETRO);
        pnlTerminale.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(CORNICE_ESTERNA, 1), 
                " DIARIO & COMANDI ", 
                0, 0, 
                new Font("Monospaced", Font.BOLD, 12), 
                CORNICE_ESTERNA
        ));
        
        txtLogConsole = new JTextPane();
        txtLogConsole.setEditable(false);
        txtLogConsole.setBackground(new Color(10, 10, 15));
        txtLogConsole.setForeground(Color.WHITE);
        txtLogConsole.setFont(new Font("Monospaced", Font.PLAIN, 13));
        
        JScrollPane scrollConsole = new JScrollPane(txtLogConsole);
        scrollConsole.setPreferredSize(new Dimension(0, 140));
        scrollConsole.setBorder(new LineBorder(CORNICE_INTERNA, 1));
        
        txtInputComando = new JTextField();
        txtInputComando.setBackground(Color.BLACK);
        txtInputComando.setForeground(TESTO_VERDE);
        txtInputComando.setFont(new Font("Monospaced", Font.BOLD, 15));
        txtInputComando.setCaretColor(TESTO_VERDE);
        txtInputComando.setBorder(new CompoundBorder(
                new LineBorder(TESTO_VERDE, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtInputComando.setPreferredSize(new Dimension(0, 36));
        
        txtInputComando.addActionListener(e -> { 
            String testoInserito = txtInputComando.getText().trim();
            if (!testoInserito.isEmpty()) {
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

    private Image caricaSpriteGiocatore(String pathCustom) {
        String[] percorsi = {
            pathCustom,
            "personaggi/protagonista/Even.png",
            "personaggi/eroe.png",
            "personaggi/protagonista/eroe.png",
            "personaggi/protagonista/Even.PNG"
        };

        for (String p : percorsi) {
            if (p != null && !p.trim().isEmpty()) {
                ImageIcon icona = ImageCacheManager.getIcona(p);
                if (icona != null && icona.getImage() != null) {
                    return icona.getImage();
                }
            }
        }
        return null;
    }

    public void renderizzaStato(GameResponse response) {
        if (response.getMessaggio() != null && !response.getMessaggio().isEmpty()) {
            String msg = response.getMessaggio();

            // Intercetta il messaggio della mappa per aprire la finestra pop-up MapDialog
            if (msg.startsWith("MAPPA_LIVELLO:")) {
                String pathMappa = msg.substring("MAPPA_LIVELLO:".length()).trim();
                SwingUtilities.invokeLater(() -> {
                    MapDialog mapDialog = new MapDialog(mainFrame);
                    mapDialog.mostraMappa(pathMappa);
                });
            } else {
                aggiungiTestoAllaConsole(msg);
            }
        }

        lblSalute.setText("Salute: " + response.getPlayerHp() + "/" + response.getPlayerMaxHp());
        
        if (response.getPlayerHp() <= 20) {
            lblSalute.setForeground(Color.RED);
        } else if (response.getPlayerHp() <= 40) {
            lblSalute.setForeground(Color.YELLOW);
        } else {
            lblSalute.setForeground(TESTO_VERDE);
        }

        // 1. CARICAMENTO SFONDO (Priorità allo sfondo della stanza attiva)
        String percorsoSfondo = response.getStanzaSfondoPath();
        if (percorsoSfondo == null || percorsoSfondo.isEmpty()) {
            percorsoSfondo = response.getMappaPngPath();
        }
        
        if (percorsoSfondo != null && !percorsoSfondo.isEmpty()) {
            ImageIcon iconaSfondo = ImageCacheManager.getIcona(percorsoSfondo);
            this.imgSfondoCorrente = (iconaSfondo != null) ? iconaSfondo.getImage() : null;
        } else {
            this.imgSfondoCorrente = null;
        }

        // 2. CARICAMENTO GIOCATORE E NEMICO
        this.inCombattimentoAttivo = response.isInCombattimento();
        
        if (this.imgGiocatoreCorrente == null) {
            this.imgGiocatoreCorrente = caricaSpriteGiocatore(null);
        }

        if (inCombattimentoAttivo && response.getEnemySpritePath() != null && !response.getEnemySpritePath().isEmpty()) {
            ImageIcon iconaNemico = ImageCacheManager.getIcona(response.getEnemySpritePath());
            this.imgNemicoCorrente = (iconaNemico != null) ? iconaNemico.getImage() : null;
        } else {
            this.imgNemicoCorrente = null;
        }

        // 3. SELEZIONE E RIPRODUZIONE DELLA COLONNA SONORA DINAMICA
        gestisciMusicaAmbiente(response, percorsoSfondo);

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
    }

    /**
     * Mappa ed esegue il cambio di traccia audio in base alla posizione e allo stato di gioco
     */
    private void gestisciMusicaAmbiente(GameResponse response, String percorsoSfondo) {
        String nuovaTraccia = "";

        if (response.isVittoria()) {
            nuovaTraccia = "colonne_sonore/combattimento/tema_della_vittoria.mp3";
        } else if (response.isGameOver()) {
            SoundManager.stopMusic();
            tracciaMusicaleAttuale = "";
            return;
        } else if (response.isInCombattimento()) {
            String spriteNemico = response.getEnemySpritePath() != null ? response.getEnemySpritePath().toLowerCase() : "";
            
            if (spriteNemico.contains("boss_finale") || spriteNemico.contains("boss3")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/boss_finale.mp3";
            } else if (spriteNemico.contains("secondo_boss") || spriteNemico.contains("boss2")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/secondo_boss.mp3";
            } else if (spriteNemico.contains("primo_boss") || spriteNemico.contains("boss1")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/primo_boss.mp3";
            } else {
                nuovaTraccia = "colonne_sonore/combattimento/tema_battaglia_normale.mp3";
            }
        } else {
            // Modalità Esplorazione: determina la traccia in base allo sfondo/livello
            String path = (percorsoSfondo != null) ? percorsoSfondo.toLowerCase() : "";

            if (path.contains("bosco")) {
                nuovaTraccia = "colonne_sonore/esplorazione_primo_livello/bosco.mp3";
            } else if (path.contains("livello_2") || path.contains("secondo_livello")) {
                nuovaTraccia = "colonne_sonore/esplorazione_secondo_livello/esplorazione_secondo_livello.mp3";
            } else if (path.contains("livello_3") || path.contains("terzo_livello")) {
                nuovaTraccia = "colonne_sonore/esplorazione_terzo_livello/esplorazione_terzo_livello.mp3";
            } else {
                // Sfondo del villaggio o inizio livello 1
                nuovaTraccia = "colonne_sonore/esplorazione_primo_livello/villaggio.mp3";
            }
        }

        // Avvia la musica solo se è cambiata rispetto a quella attualmente in riproduzione
        if (!nuovaTraccia.isEmpty() && !nuovaTraccia.equals(tracciaMusicaleAttuale)) {
            tracciaMusicaleAttuale = nuovaTraccia;
            SoundManager.playMusic(nuovaTraccia);
        }
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
    
    public void mostraSchermataIniziale() {
        aggiungiTestoAllaConsole("Avvio del terminale operativo...");
        aggiungiTestoAllaConsole("Benvenuto su The Revolution Flame. Attesa connessione...");
        
        // Traccia del menu iniziale (puoi impostarla se hai una canzone dedicata nel menu)
        tracciaMusicaleAttuale = "colonne_sonore/esplorazione_primo_livello/villaggio.mp3";
        SoundManager.playMusic(tracciaMusicaleAttuale);

        txtInputComando.setEnabled(true);
        txtInputComando.requestFocusInWindow(); 
    }
}