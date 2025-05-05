import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ex1 {

    public static Map<String, Variable> variables = new HashMap<>();
    static int multiplicationCount = 0;
    static int additionCount = 0;

    public static void main(String[] args) throws Exception {
        // BufferedReader reader = new BufferedReader(new FileReader("input_alarm.txt"));
        // BufferedWriter writer = new BufferedWriter(new FileWriter("output_alarm.txt"));
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        // BufferedReader reader = new BufferedReader(new FileReader("input_big.txt"));
        // BufferedWriter writer = new BufferedWriter(new FileWriter("output_big.txt"));

        String xmlFile = reader.readLine();
        if (xmlFile == null || !xmlFile.trim().toLowerCase().endsWith(".xml")) {
            writer.write("ERROR: First line must be a .xml file name\n");
            reader.close();
            writer.close();
            return;
        }
        parseXML(xmlFile.trim());

        String query;
        Pattern jointPattern = Pattern.compile("^P\\(([^|=]+=[TF](,[^|=]+=[TF])*)\\)$");
        Pattern condPattern = Pattern.compile("^P\\(([^|=]+=[TF])\\|([^|=]+=[TF](,[^|=]+=[TF])*)\\),[123]$");

        while ((query = reader.readLine()) != null) {
            query = query.trim();
            if (query.isEmpty()) continue;

            multiplicationCount = 0;
            additionCount = 0;

            if (jointPattern.matcher(query).matches()) {
                double result = jointProbability(setup_joibProbability(query));
                writer.write(String.format("%.5f,%d,%d\n", result, additionCount, multiplicationCount));
            } else if (condPattern.matcher(query).matches()) {
                int type = Integer.parseInt(query.substring(query.length() - 1));
                VESolver solver = VESolverFactory.getSolver(type);
                double result = solver.solve(query);
                writer.write(String.format("%.5f,%d,%d\n", result, additionCount, multiplicationCount));
            } else {
                writer.write("ERROR: Invalid query format\n");
            }
        }

        reader.close();
        writer.close();
    }

    static String extractTagValue(String line, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    static void parseXML(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        Variable currentVar = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<VARIABLE>")) {
                currentVar = null;
            } else if (line.contains("<NAME>")) {
                String name = extractTagValue(line, "NAME");
                currentVar = new Variable(name);
                variables.put(name, currentVar);
            } else if (line.contains("<OUTCOME>")) {
                String outcome = extractTagValue(line, "OUTCOME");
                if (currentVar != null) currentVar.outcomes.add(outcome);
            } else if (line.startsWith("<DEFINITION>")) {
                currentVar = null;
            } else if (line.contains("<FOR>")) {
                String name = extractTagValue(line, "FOR");
                currentVar = variables.get(name);
            } else if (line.contains("<GIVEN>")) {
                String parent = extractTagValue(line, "GIVEN");
                if (currentVar != null) currentVar.parents.add(parent);
            } else if (line.contains("<TABLE>")) {
                String content = extractTagValue(line, "TABLE");
                String[] probs = content.split(" ");

                int total = 1;
                for (String parent : currentVar.parents) {
                    total *= variables.get(parent).outcomes.size();
                }
                int varOutcomes = currentVar.outcomes.size();

                int index = 0;
                for (int i = 0; i < total; i++) {
                    for (int j = 0; j < varOutcomes; j++) {
                        String key = i + "," + j;
                        currentVar.cpt.put(key, Double.parseDouble(probs[index++]));
                    }
                }
            }
        }
        reader.close();
    }

    static int getParentIndex(Variable var, Map<String, String> evidence) {
        int idx = 0;
        int mul = 1;
        List<String> reversed = new ArrayList<>(var.parents);
        Collections.reverse(reversed);
        for (String parent : reversed) {
            int pos = variables.get(parent).outcomes.indexOf(evidence.get(parent));
            idx += pos * mul;
            mul *= variables.get(parent).outcomes.size();
        }
        return idx;
    }

    static Map<String, String> setup_joibProbability(String query){
        String[] assignments = query.substring(2, query.length() - 1).split(",");
        Map<String, String> evidence = new HashMap<>();
        for (String assign : assignments) {
            String[] parts = assign.split("=");
            evidence.put(parts[0].trim(), parts[1].trim());
        }
        return evidence;
    }
    static double jointProbability(Map<String, String> evidence) {
        Iterator<Variable> it = variables.values().iterator();
        if (!it.hasNext()) return 1.0;
        Variable firstVar = it.next();
        int firstParentIndex = getParentIndex(firstVar, evidence);
        int firstVarIndex = firstVar.outcomes.indexOf(evidence.get(firstVar.name));
        String firstKey = firstParentIndex + "," + firstVarIndex;
        double prob = firstVar.cpt.get(firstKey);
        while (it.hasNext()) {
            Variable var = it.next();
            int parentIndex = getParentIndex(var, evidence);
            int varIndex = var.outcomes.indexOf(evidence.get(var.name));
            String cptKey = parentIndex + "," + varIndex;
            double p = var.cpt.get(cptKey);
            prob *= p;
            multiplicationCount++;
        }
        return prob;
    }

    static double variableElimination(String query) {
        String[] queryAndType = query.substring(2, query.length() - 1).split("\\),");  // ["B=T|J=T,M=T" , "1"]
        String probPart = queryAndType[0];                           // get query ("B=T|J=T,M=T")
        int type = Integer.parseInt(queryAndType[1]);                // gey type (1)
        String[] parts = probPart.split("\\|");                // split to query and evidence ["B=T" , "J=T,M=T"]
        String queryPart = parts[0].trim();                          // query
        String[] querySplit = queryPart.split("=");            // split to var and val
        String queryVar = querySplit[0].trim();                      // save query var
        String queryVal = querySplit[1].trim();                      // save query val
    
        String[] evidenceParts = parts[1].split(",");          // split evidence
        Map<String, String> evidence = new HashMap<>();              // map to save as (var,val)
        for (String assign : evidenceParts) {                        // go over evidence
            String[] kv = assign.split("=");                   // split each to var and val
            evidence.put(kv[0].trim(), kv[1].trim());                // save as (var,val)
        }
    
        Set<String> hiddenVars = new HashSet<>(variables.keySet());  // start with all vars (end with unused vars)
        hiddenVars.removeAll(evidence.keySet());                     // remove evidence
        hiddenVars.remove(queryVar);                                 // remove query
    
        List<Factor> factors = new ArrayList<>();       // array of factors
        for (Variable var : variables.values()) {       // for each variable
            Factor f = new Factor(var, evidence);       // create a factor for var and evidence
            if(f.rows.size() > 0){ factors.add(f); }    // add factor 
        }
    
        // Eliminate hidden variables
        for (String hidden : hiddenVars) {              // go through hidden vars
            List<Factor> involved = new ArrayList<>();
            Iterator<Factor> it = factors.iterator();   // 
            while (it.hasNext()) {                      // 
                Factor f = it.next();
                if (f.vars.contains(hidden)) {
                    involved.add(f);
                    it.remove();
                }
            }
    
            if (!involved.isEmpty()) {
                Factor product = involved.get(0);
                for (int i = 1; i < involved.size(); i++) {
                    product = product.multiply(involved.get(i));
                }
                Factor summed = product.sumOut(hidden);
                factors.add(summed);
            }
        }
    
        // Multiply remaining factors
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = result.multiply(factors.get(i));
        }
    
        // Normalize
        double total = 0.0;
        double prob = 0.0;
        for (FactorRow row : result.rows) {
            double val = row.prob;
            total += val;
            if (row.assignments.get(queryVar).equals(queryVal)) {
                prob = val;
            }
        }
    
        return prob / total;
    }
}
