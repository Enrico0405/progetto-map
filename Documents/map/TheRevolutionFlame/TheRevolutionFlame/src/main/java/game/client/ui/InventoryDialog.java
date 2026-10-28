package game.client.ui;

import game.client.util.ImageCacheManager;
import game.common.dto.ItemDto;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryDialog extends JDialog {

    private JLabel lblArmaEquipaggiata;
    private JLabel lblPozioneEquipaggiata;

    private JTable tblInventarioGiocatore; // Prefisso G (Scacchiera Giocatore)
    private JTable tblOggettiStanza;       // Prefisso S (Scacchiera Stanza)

    public InventoryDialog(MainFrame parent) {
        super(parent, "Inventario e Stanza", false);
        this.setSize(750, 420);
        this.setLocationRelativeTo(parent);
        inizializzaComponenti();
    }

    public void inizializzaComponenti() {
        this.setLayout(new BorderLayout(10, 10));

        // --- PANNELLO ALTO: Slot Equipaggiamento ---
        JPanel pnlEquip = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlEquip.setBorder(BorderFactory.createTitledBorder("EQUIPAGGIAMENTO ATTIVO"));
        this.lblArmaEquipaggiata = new JLabel("Arma: Nessuna", JLabel.CENTER);
        this.lblPozioneEquipaggiata = new JLabel("Pozione: Nessuna", JLabel.CENTER);
        this.lblArmaEquipaggiata.setFont(new Font("Monospaced", Font.BOLD, 13));
        this.lblPozioneEquipaggiata.setFont(new Font("Monospaced", Font.BOLD, 13));
        
        pnlEquip.add(lblArmaEquipaggiata);
        pnlEquip.add(lblPozioneEquipaggiata);
        this.add(pnlEquip, BorderLayout.NORTH);

        // --- PANNELLO CENTRALE: Le due scacchiere (3 righe x 4 colonne) ---
        JPanel pnlScacchiere = new JPanel(new GridLayout(1, 2, 10, 0));
        
        String[] colonne = {"1", "2", "3", "4"};

        // Modello tabella Giocatore (G) - 3 Righe (a, b, c) x 4 Colonne (1, 2, 3, 4)
        DefaultTableModel modelGiocatore = createGridModel(colonne);
        this.tblInventarioGiocatore = createConfiguredTable(modelGiocatore);
        
        // Modello tabella Stanza (S) - 3 Righe (a, b, c) x 4 Colonne (1, 2, 3, 4)
        DefaultTableModel modelStanza = createGridModel(colonne);
        this.tblOggettiStanza = createConfiguredTable(modelStanza);

        JPanel pnlG = new JPanel(new BorderLayout());
        pnlG.setBorder(BorderFactory.createTitledBorder("INVENTARIO GIOCATORE (G)"));
        pnlG.add(creaScrollPaneConEtichetteRiga(tblInventarioGiocatore), BorderLayout.CENTER);

        JPanel pnlS = new JPanel(new BorderLayout());
        pnlS.setBorder(BorderFactory.createTitledBorder("OGGETTI NELLA STANZA (S)"));
        pnlS.add(creaScrollPaneConEtichetteRiga(tblOggettiStanza), BorderLayout.CENTER);

        pnlScacchiere.add(pnlG);
        pnlScacchiere.add(pnlS);
        
        this.add(pnlScacchiere, BorderLayout.CENTER);
    }

    /**
     * Crea uno JScrollPane dotato di un'intestazione verticale (Row Header) 
     * contenente le lettere 'a', 'b', 'c' perfettamente allineate alle righe della tabella.
     */
    private JScrollPane creaScrollPaneConEtichetteRiga(JTable table) {
        String[] righeEtichette = {"a", "b", "c"};
        JList<String> rowHeaderList = new JList<>(righeEtichette);
        rowHeaderList.setFixedCellWidth(25);
        rowHeaderList.setFixedCellHeight(table.getRowHeight());
        rowHeaderList.setBackground(new Color(235, 235, 235));
        rowHeaderList.setForeground(Color.DARK_GRAY);
        rowHeaderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        rowHeaderList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeaderList);
        return scrollPane;
    }

    private DefaultTableModel createGridModel(String[] colonne) {
        return new DefaultTableModel(colonne, 3) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Icon.class; // Permette alla JTable di renderizzare le icone
            }
        };
    }

    private JTable createConfiguredTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(65); // Spazio verticale per le icone dei PNG
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Renderer personalizzato per mostrare sia icona che testo compatto
        table.setDefaultRenderer(Icon.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalTextPosition(JLabel.BOTTOM);
                label.setHorizontalTextPosition(JLabel.CENTER);
                
                if (value instanceof Icon) {
                    label.setIcon((Icon) value);
                    label.setText("");
                } else if (value != null) {
                    label.setIcon(null);
                    label.setText(value.toString());
                } else {
                    label.setIcon(null);
                    label.setText("");
                }
                return label;
            }
        });

        return table;
    }

    public void aggiornaGriglie(List<ItemDto> invGiocatore, List<ItemDto> invStanza) {
        DefaultTableModel modelGiocatore = (DefaultTableModel) this.tblInventarioGiocatore.getModel();
        DefaultTableModel modelStanza = (DefaultTableModel) this.tblOggettiStanza.getModel();

        // 1. Pulizia preventiva di tutte le 12 celle per entrambe le griglie
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                modelGiocatore.setValueAt(null, r, c);
                modelStanza.setValueAt(null, r, c);
            }
        }

        // 2. Popola la scacchiera del Giocatore (G) tramite slotIndex diretto
        if (invGiocatore != null) {
            for (ItemDto item : invGiocatore) {
                if (item != null) {
                    int slotIndex = item.getSlotIndex();
                    int riga = slotIndex / 4;
                    int col = slotIndex % 4;

                    if (riga >= 0 && riga < 3 && col >= 0 && col < 4) {
                        ImageIcon icona = ImageCacheManager.getIcona(item.getImmaginePath());
                        if (icona != null && icona.getImage() != null) {
                            Image img = icona.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            modelGiocatore.setValueAt(new ImageIcon(img), riga, col);
                        } else {
                            modelGiocatore.setValueAt(item.getNome(), riga, col);
                        }
                    }
                }
            }
        }

        // 3. Popola la scacchiera della Stanza (S) tramite slotIndex diretto
        if (invStanza != null) {
            for (ItemDto item : invStanza) {
                if (item != null) {
                    int slotIndex = item.getSlotIndex();
                    int riga = slotIndex / 4;
                    int col = slotIndex % 4;

                    if (riga >= 0 && riga < 3 && col >= 0 && col < 4) {
                        ImageIcon icona = ImageCacheManager.getIcona(item.getImmaginePath());
                        if (icona != null && icona.getImage() != null) {
                            Image img = icona.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            modelStanza.setValueAt(new ImageIcon(img), riga, col);
                        } else {
                            modelStanza.setValueAt(item.getNome(), riga, col);
                        }
                    }
                }
            }
        }

        // 4. Aggiorna la resa grafica delle tabelle
        this.tblInventarioGiocatore.repaint();
        this.tblOggettiStanza.repaint();
    }

    public void aggiornaEquipaggiamento(String arma, String pozione) {
        this.lblArmaEquipaggiata.setText("Arma: " + (arma != null && !arma.isBlank() ? arma : "Nessuna"));
        this.lblPozioneEquipaggiata.setText("Pozione: " + (pozione != null && !pozione.isBlank() ? pozione : "Nessuna"));
    }
}