import java.util.Set;

public class AddressingTable {
    private Set<String> neighbours;
    private String sender;

    public Set<String> getNeighbours() {
        return neighbours;
    }
    
    public void setNeighbours(Set<String> neighbours) {
        this.neighbours = neighbours;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public AddressingTable(Set<String> neighbours, String sender) {
        this.neighbours = neighbours;
        this.sender = sender;
    }
}
