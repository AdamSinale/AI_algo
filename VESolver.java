import java.util.*;

public interface VESolver {
    double solve(String query);

    default double eliminationAsk(String queryVar, String queryVal, Map<String, String> evidence, List<String> eliminationOrder) {
        Set<String> hiddenVars = new HashSet<>(ex1.variables.keySet());
        hiddenVars.removeAll(evidence.keySet());
        hiddenVars.remove(queryVar);

        List<Factor> factors = new ArrayList<>();
        for (Variable var : ex1.variables.values()) {
            Factor f = new Factor(var, evidence);
            if (!f.rows.isEmpty()) {
                factors.add(f);
            }
        }

        for (String varToEliminate : eliminationOrder) {
            List<Factor> toCombine = new ArrayList<>();
            Iterator<Factor> it = factors.iterator();

            while (it.hasNext()) {
                Factor f = it.next();
                if (f.vars.contains(varToEliminate)) {
                    toCombine.add(f);
                    it.remove();
                }
            }

            if (!toCombine.isEmpty()) {
                Factor product = toCombine.get(0);
                for (int i = 1; i < toCombine.size(); i++) {
                    product = product.multiply(toCombine.get(i));
                }
                product = product.sumOut(varToEliminate);
                factors.add(product);
            }
        }

        // Final multiplication (remaining relevant factors)
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = result.multiply(factors.get(i));
        }

        double total = 0.0;
        double prob = 0.0;
        for (FactorRow row : result.rows) {
            double val = row.prob;
            total += val;
            ex1.additionCount++;
            if (row.assignments.get(queryVar).equals(queryVal)) {
                prob = val;
            }
        }

        return prob / total;
    }
}
