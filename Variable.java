import java.util.*;

public class Variable {
    public String name;
    public List<String> outcomes = new ArrayList<>();
    public List<String> parents = new ArrayList<>();
    public Map<String, Double> cpt = new HashMap<>();

    public Variable(String name) {
        this.name = name;
    }
}
