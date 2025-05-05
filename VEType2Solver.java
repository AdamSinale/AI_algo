import java.util.*;

public class VEType2Solver implements VESolver {

    @Override
    public double solve(String query) {
        String[] parts = query.substring(2, query.length() - 1).split("\\),");
        String probPart = parts[0];
        String[] mainParts = probPart.split("\\|");
        String[] querySplit = mainParts[0].trim().split("=");
        String queryVar = querySplit[0].trim();
        String queryVal = querySplit[1].trim();

        Map<String, String> evidence = new HashMap<>();
        if (mainParts.length > 1) {
            for (String e : mainParts[1].split(",")) {
                String[] kv = e.split("=");
                evidence.put(kv[0].trim(), kv[1].trim());
            }
        }
        return eliminationAsk(queryVar, queryVal, evidence, getFixedOrder(queryVar, evidence));
    }
    public List<String> getFixedOrder(String queryVar, Map<String, String> evidence) {
        List<String> order = new ArrayList<>(ex1.variables.keySet());
        order.removeAll(evidence.keySet());
        order.remove(queryVar);
        return order;
    }
}
