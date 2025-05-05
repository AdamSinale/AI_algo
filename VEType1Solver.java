// VEType1Solver.java - עם enumerateAll רקורסיבי לפי המצגת + Cache חכם

import java.util.*;

public class VEType1Solver implements VESolver {

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

        return enumerationViaJointProbability(queryVar, queryVal, evidence);
    }

    private double enumerationViaJointProbability(String queryVar, String queryVal, Map<String, String> evidence) {
        Map<Map<String, String>, Double> jointCache = new HashMap<>();
        List<String> hidden = new ArrayList<>();
        for (String var : ex1.variables.keySet()) {
            if (!evidence.containsKey(var) && !var.equals(queryVar)) {
                hidden.add(var);
            }
        }

        double numerator = 0.0;
        List<Map<String, String>> hiddenCombinations = generateCombinations(hidden);
        if (!hiddenCombinations.isEmpty()) {
            Map<String, String> firstFull = new HashMap<>(evidence);
            firstFull.put(queryVar, queryVal);
            firstFull.putAll(hiddenCombinations.get(0));
            jointCache.computeIfAbsent(firstFull, ex1::jointProbability);
            numerator = jointCache.get(firstFull);
            for (int i = 1; i < hiddenCombinations.size(); i++) {
                Map<String, String> full = new HashMap<>(evidence);
                full.put(queryVar, queryVal);
                full.putAll(hiddenCombinations.get(i));
    
                jointCache.computeIfAbsent(full, ex1::jointProbability);
                numerator += jointCache.get(full);
                ex1.additionCount++;
            }
        } else {
            Map<String, String> full = new HashMap<>(evidence);
            full.put(queryVar, queryVal);
            jointCache.computeIfAbsent(full, ex1::jointProbability);
            numerator = jointCache.get(full);
        }

        double denominator = numerator; // reuse numerator values
        for (String altVal : ex1.variables.get(queryVar).outcomes) {
            if (altVal.equals(queryVal)) continue;

            for (Map<String, String> assign : hiddenCombinations) {
                Map<String, String> full = new HashMap<>(evidence);
                full.put(queryVar, altVal);
                full.putAll(assign);

                if (!jointCache.containsKey(full)) {
                    jointCache.put(full, ex1.jointProbability(full));
                }
                denominator += jointCache.get(full);
                ex1.additionCount++;
            }
        }

        return numerator / denominator;
    }

    private List<Map<String, String>> generateCombinations(List<String> vars) {
        List<Map<String, String>> results = new ArrayList<>();
        if (vars.isEmpty()) {
            results.add(new HashMap<>());
            return results;
        }
        String first = vars.get(0);
        List<String> rest = vars.subList(1, vars.size());
        List<Map<String, String>> suffixes = generateCombinations(rest);
        for (String val : ex1.variables.get(first).outcomes) {
            for (Map<String, String> suffix : suffixes) {
                Map<String, String> full = new HashMap<>(suffix);
                full.put(first, val);
                results.add(full);
            }
        }
        return results;
    }
}
