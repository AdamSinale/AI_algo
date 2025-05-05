import java.util.*;

public class VEType3Solver implements VESolver {

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
        return eliminationAsk(queryVar, queryVal, evidence, getHeuristicOrder(queryVar, evidence));
    }

    public List<String> getHeuristicOrder(String queryVar, Map<String, String> evidence) {
        Set<String> hidden = new HashSet<>(ex1.variables.keySet());
        hidden.removeAll(evidence.keySet());
        hidden.remove(queryVar);

        List<String> order = new ArrayList<>();
        while (!hidden.isEmpty()) {
            String minVar = null;
            int minCost = Integer.MAX_VALUE;
            for (String var : hidden) {
                int cost = 1;
                for (Variable v : ex1.variables.values()) {
                    if (v.parents.contains(var) || v.name.equals(var)) {
                        cost *= v.outcomes.size();
                    }
                }
                if (cost < minCost) {
                    minCost = cost;
                    minVar = var;
                }
            }
            order.add(minVar);
            hidden.remove(minVar);
        }
        return order;
    }
}
