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
    private boolean eraInCombattimento = false;
    
    // Traccia se la GUI si trova attualmente nella schermata del menu principale
    private boolean inMenuPrincipale = true;

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

        // Carica la copertina iniziale per il menu principale
        this.imgSfondoCorrente = caricaImmagineCopertina();

        // Carica lo sprite del personaggio
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

                // STRATO 1: SFONDO (Copertina / Mappa / Stanza)
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
                String cmdLower = testoInserito.toLowerCase();

                // Intercetta il comando per tornare al menu principale
                if (cmdLower.equals("menu") || cmdLower.equals("menu principale") || cmdLower.equals("torna al menu")) {
                    mainFrame.resettaSessione();
                    mostraSchermataIniziale();
                    txtInputComando.setText("");
                    return;
                }

                // Intercetta l'apertura manuale dell'inventario
                if (cmdLower.equals("apri inventario") || cmdLower.equals("inventario") || cmdLower.equals("vedi inventario")) {
                    mainFrame.apriInventario();
                }

                if (cmdLower.startsWith("id:")) {
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

    private Image caricaImmagineCopertina() {
        String[] percorsiCopertina = {
            "sfondo menu principale/sfondo_menu_principale.jpg",
            "sfondo menu principale/sfondo_menu_principale.png",
            "images/copertina.png"
        };

        for (String p : percorsiCopertina) {
            ImageIcon icona = ImageCacheManager.getIcona(p);
            if (icona != null && icona.getImage() != null) {
                return icona.getImage();
            }
        }
        return null;
    }

    private Image caricaSpriteGiocatore(String pathCustom) {
        String[] percorsi = {
            pathCustom,
            "personaggi/protagonista/even.png",
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
        if (response == null) return;

        // Se la GUI è attualmente sul menu principale e la risposta del server è un residuo di Game Over/Vittoria, ignoriamo l'aggiornamento
        if (inMenuPrincipale && (response.isGameOver() || response.isVittoria())) {
            return;
        }

        // Se arriva una risposta valida di gioco in corso, usciamo dallo stato di menu principale
        if (!response.isGameOver() && !response.isVittoria() && 
           ((response.getStanzaSfondoPath() != null && !response.getStanzaSfondoPath().isEmpty()) || response.getIdGiocatore() > 0)) {
            this.inMenuPrincipale = false;
        }

        if (response.getMessaggio() != null && !response.getMessaggio().isEmpty()) {
            String msg = response.getMessaggio();
            String msgLower = msg.toLowerCase().trim();

            // Controllo per l'uscita al menu
            if (msgLower.contains("tornato al menu principale") || msgLower.equals("menu principale")) {
                mostraSchermataIniziale();
                return;
            }

            if (msg.startsWith("MAPPA_LIVELLO:")) {
                String pathMappa = msg.substring("MAPPA_LIVELLO:".length()).trim();
                SwingUtilities.invokeLater(() -> {
                    MapDialog mapDialog = new MapDialog(mainFrame);
                    mapDialog.mostraMappa(pathMappa);
                });
            } else if (msg.startsWith("INVENTARIO_LIVELLO:") || msg.startsWith("INVENTARIO:")) {
                mainFrame.apriInventario();
            } else {
                if (msg.contains("ESPLORAZIONE:") && !msg.contains("apri inventario")) {
                    msg = msg.replace("vedi mappa", "vedi mappa\n   - apri inventario          : Visualizza la finestra dell'inventario");
                }
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

        // 1. CARICAMENTO SFONDO
        String percorsoSfondo = response.getStanzaSfondoPath();
        if (percorsoSfondo == null || percorsoSfondo.isEmpty()) {
            percorsoSfondo = response.getMappaPngPath();
        }
        
        if (percorsoSfondo != null && !percorsoSfondo.isEmpty()) {
            ImageIcon iconaSfondo = ImageCacheManager.getIcona(percorsoSfondo);
            this.imgSfondoCorrente = (iconaSfondo != null) ? iconaSfondo.getImage() : null;
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

        // Gestione Game Over e Vittoria
        if (response.isGameOver() || response.isVittoria()) {
            txtInputComando.setEnabled(true);
            txtInputComando.requestFocusInWindow();
            
            if (response.isVittoria()) {
                aggiungiTestoAllaConsole("\n*** HAI VINTO! ***");
            } else {
                aggiungiTestoAllaConsole("\n*** GAME OVER ***");
            }

            aggiungiTestoAllaConsole(">> Digita 'menu' per tornare al menu principale.");
        }
    }

    /**
     * Mappa ed esegue il cambio di traccia audio in base alla posizione e allo stato di gioco
     */
    private void gestisciMusicaAmbiente(GameResponse response, String percorsoSfondo) {
        String nuovaTraccia = "";
        String messaggioServer = (response.getMessaggio() != null) ? response.getMessaggio().toLowerCase() : "";

        // 1. Vittoria finale di tutto il gioco o Game Over
        if (response.isVittoria()) {
            nuovaTraccia = "colonne_sonore/combattimento/tema_della_vittoria.mp3";
        } else if (response.isGameOver()) {
            SoundManager.stopMusic();
            tracciaMusicaleAttuale = "";
            eraInCombattimento = false;
            return;
        } else if (response.isInCombattimento()) {
            // COMBATTIMENTO IN CORSO
            eraInCombattimento = true;
            
            String spriteNemico = response.getEnemySpritePath() != null ? response.getEnemySpritePath().toLowerCase() : "";
            
            if (spriteNemico.contains("guardiano_del_tempio") || spriteNemico.contains("boss3")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/boss_finale.mp3";
            } else if (spriteNemico.contains("re_stregore_angmar") || spriteNemico.contains("boss2")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/secondo_boss.mp3";
            } else if (spriteNemico.contains("gran_inquisitore") || spriteNemico.contains("boss1")) {
                nuovaTraccia = "colonne_sonore/combattimento/boss/primo_boss.mp3";
            } else {
                nuovaTraccia = "colonne_sonore/combattimento/tema_battaglia_normale.mp3";
            }
        } else {
            // ESPLORAZIONE: Controlla se siamo appena usciti da un combattimento
            if (eraInCombattimento) {
                eraInCombattimento = false; // Reset dello stato

                // Se siamo usciti dal combattimento FUGGENDO, NON far partire la vittoria!
                if (messaggioServer.contains("fuggito") || messaggioServer.contains("fuga") || messaggioServer.contains("scappato")) {
                    // Imposta la musica della stanza di destinazione
                    nuovaTraccia = recuperaMusicaStanza(percorsoSfondo);
                } else {
                    // Scontro vinto regolarmente!
                    nuovaTraccia = "colonne_sonore/combattimento/tema_della_vittoria.mp3";
                }
            } else {
                // Normale esplorazione
                nuovaTraccia = recuperaMusicaStanza(percorsoSfondo);
            }
        }

        // Avvia la musica solo se è diversa da quella attualmente in riproduzione
        if (!nuovaTraccia.isEmpty() && !nuovaTraccia.equals(tracciaMusicaleAttuale)) {
            tracciaMusicaleAttuale = nuovaTraccia;
            SoundManager.playMusic(nuovaTraccia);
        }
    }

    /**
     * Metodo helper per recuperare la musica di sottofondo della stanza
     */
    private String recuperaMusicaStanza(String percorsoSfondo) {
        if (percorsoSfondo == null || percorsoSfondo.trim().isEmpty()) {
            return "colonne_sonore/menu/canzone_menu_principale.mp3";
        }

        String path = percorsoSfondo.toLowerCase();

        if (path.contains("bosco")) {
            return "colonne_sonore/esplorazione_primo_livello/bosco.mp3";
        } else if (path.contains("livello_2") || path.contains("secondo_livello")) {
            return "colonne_sonore/esplorazione_secondo_livello/esplorazione_secondo_livello.mp3";
        } else if (path.contains("livello_3") || path.contains("terzo_livello")) {
            return "colonne_sonore/esplorazione_terzo_livello/esplorazione_terzo_livello.mp3";
        } else {
            return "colonne_sonore/esplorazione_primo_livello/villaggio.mp3";
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
        // Attiva lo stato di menu principale e azzera la console
        this.inMenuPrincipale = true;
        this.inCombattimentoAttivo = false;
        this.imgNemicoCorrente = null;
        
        if (txtLogConsole != null) {
            txtLogConsole.setText("");
        }

        aggiungiTestoAllaConsole("Benvenuto su The Revolution Flame");
        aggiungiTestoAllaConsole("digita si se hai già giocato in precedenza, no se e' la prima volta");

        // Imposta l'immagine di copertina e aggiorna la grafica del pannello
        this.imgSfondoCorrente = caricaImmagineCopertina();
        this.repaint();

        // Traccia univoca per il menu principale
        tracciaMusicaleAttuale = "colonne_sonore/menu/canzone_menu_principale.mp3";
        SoundManager.playMusic(tracciaMusicaleAttuale);

        txtInputComando.setEnabled(true);
        txtInputComando.requestFocusInWindow(); 
    }
}