
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

class FactorRow {
    Map<String, String> assignments;
    double prob;
    FactorRow(Map<String, String> assignments, double prob) {
        this.assignments = new HashMap<>(assignments);
        this.prob = prob;
    }
}

class Factor {
    List<String> vars;
    List<FactorRow> rows;

    Factor(Variable var, Map<String, String> evidence) {
        this.vars = new ArrayList<>();
        this.rows = new ArrayList<>();

        List<String> relevantVars = new ArrayList<>(var.parents);  // get var's parents
        relevantVars.add(var.name);                                // add the var itself

        Map<String, List<String>> domain = new HashMap<>();
        for (String v : relevantVars) {
            domain.put(v, ex1.variables.get(v).outcomes);          // create (var,outcomes) for var & parents
        }

        List<Map<String, String>> assignments = cartesianProduct(domain);  // get all possible combinations
        for (Map<String, String> assignment : assignments) {               // 
            boolean skip = false;
            for (Map.Entry<String,String> e : evidence.entrySet()) {                                        
                if(assignment.containsKey(e.getKey()) && !assignment.get(e.getKey()).equals(e.getValue())){  // skip if assignment doesnt have evidence or it's value
                    skip = true;
                    break;
                }
            }
            if(skip) continue;

            int parentIndex = ex1.getParentIndex(var, assignment);          // 
            int varIndex = var.outcomes.indexOf(assignment.get(var.name));  // 
            String key = parentIndex + "," + varIndex;                      // key by parent and var's value
            double prob = var.cpt.get(key);                                 // get probability by key 
            this.vars = new ArrayList<>(assignment.keySet());               // add assignment vars
            this.rows.add(new FactorRow(assignment, prob));                 // add assigment and prob
        }
    }

    Factor(List<String> vars, List<FactorRow> rows) {
        this.vars = new ArrayList<>(vars);
        this.rows = new ArrayList<>(rows);
    }

    Factor multiply(Factor other) {
        List<String> newVars = new ArrayList<>(new LinkedHashSet<>(this.vars));
        for (String v : other.vars) {
            if (!newVars.contains(v)) newVars.add(v);
        }

        List<FactorRow> newRows = new ArrayList<>();

        for (FactorRow r1 : this.rows) {
            for (FactorRow r2 : other.rows) {
                boolean match = true;
                for (String v : this.vars) {
                    if (other.vars.contains(v)) {
                        if (!r1.assignments.get(v).equals(r2.assignments.get(v))) {
                            match = false;
                            break;
                        }
                    }
                }
                if (match) {
                    Map<String, String> newAssign = new HashMap<>(r1.assignments);
                    newAssign.putAll(r2.assignments);
                    double newProb = r1.prob * r2.prob;
                    ex1.multiplicationCount++;
                    newRows.add(new FactorRow(newAssign, newProb));
                }
            }
        }
        return new Factor(newVars, newRows);
    }

    Factor sumOut(String var) {
        List<String> newVars = new ArrayList<>(this.vars);
        newVars.remove(var);

        Map<Map<String, String>, Double> table = new HashMap<>();

        for (FactorRow row : this.rows) {
            Map<String, String> reducedAssign = new HashMap<>(row.assignments);
            reducedAssign.remove(var);

            double sum = table.getOrDefault(reducedAssign, 0.0);
            sum += row.prob;
            ex1.additionCount++;
            table.put(reducedAssign, sum);
        }

        List<FactorRow> newRows = new ArrayList<>();
        for (Map.Entry<Map<String, String>, Double> entry : table.entrySet()) {
            newRows.add(new FactorRow(entry.getKey(), entry.getValue()));
        }

        return new Factor(newVars, newRows);
    }

    private List<Map<String, String>> cartesianProduct(Map<String, List<String>> domain) {
        List<Map<String, String>> results = new ArrayList<>();
        List<String> keys = new ArrayList<>(domain.keySet());
        generateAssignments(domain, keys, 0, new HashMap<>(), results);
        return results;
    }
    private void generateAssignments(Map<String,List<String>> domain, List<String> keys,
                                     int index, Map<String,String> current, List<Map<String,String>> results) {
        if (index == keys.size()){                                     // when goes through all vars and vals for key
            results.add(new HashMap<>(current));                       // adds the list of current combination
            return;                                                    // returns
        }
        String key = keys.get(index);                                  // gets current key
        for (String value : domain.get(key)) {                         // goes through all vals of the key
            current.put(key, value);                                   // adds (key,val) for all key vals
            generateAssignments(domain,keys,index+1,current,results);  // goes though all vars and vals for current key
        }
    }
}
