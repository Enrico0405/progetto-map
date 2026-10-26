package game.common.dto;

public class ItemDto {

    private String nome;
    private String immaginePath;
    private int slotIndex;

    public ItemDto() {
    }

    public ItemDto(String nome, String immaginePath) {
        this.nome = nome;
        this.immaginePath = immaginePath;
    }

    public ItemDto(String nome, String immaginePath, int slotIndex) {
        this.nome = nome;
        this.immaginePath = immaginePath;
        this.slotIndex = slotIndex;
    }

    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getImmaginePath() { 
        return immaginePath; 
    }
    
    public void setImmaginePath(String immaginePath) { 
        this.immaginePath = immaginePath; 
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }
}