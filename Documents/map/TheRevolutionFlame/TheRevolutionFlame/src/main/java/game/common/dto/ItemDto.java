package game.common.dto;

public class ItemDto {

    private String nome;
    private String immaginePath;

    public ItemDto() {
    }

    public ItemDto(String nome, String immaginePath) {
        this.nome = nome;
        this.immaginePath = immaginePath;
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
}