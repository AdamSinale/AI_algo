import java.io.*;
import java.util.*;

public class ex2 {

    static class Variable {
        String name;
        List<String> outcomes = new ArrayList<>();
        List<String> parents = new ArrayList<>();
        Map<String, Double> cpt = new HashMap<>();

        public Variable(String name) {
            this.name = name;
        }
    }

    static Map<String, Variable> variables = new HashMap<>();
    static int multiplicationCount = 0;
    static int additionCount = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

        String xmlFile = reader.readLine();
        parseXML(xmlFile.trim());

        String query;
        while ((query = reader.readLine()) != null) {
            query = query.trim();
            if (query.isEmpty()) continue;

            multiplicationCount = 0;
            additionCount = 0;

            if (query.contains("|") == false) {
                double result = jointProbability(query);
                writer.write(String.format("%.5f,%d,%d\n", result, additionCount, multiplicationCount));
            } else {
                double result = variableElimination(query);
                writer.write(String.format("%.5f,%d,%d\n", result, additionCount, multiplicationCount));
            }
        }

        reader.close();
        writer.close();
    }

    static void parseXML(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        Variable currentVar = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<VARIABLE>")) {
                currentVar = null;
            } else if (line.startsWith("<NAME>")) {
                String name = line.replace("<NAME>", "").replace("</NAME>", "").trim();
                currentVar = new Variable(name);
                variables.put(name, currentVar);
            } else if (line.startsWith("<OUTCOME>")) {
                String outcome = line.replace("<OUTCOME>", "").replace("</OUTCOME>", "").trim();
                if (currentVar != null) currentVar.outcomes.add(outcome);
            } else if (line.startsWith("<DEFINITION>")) {
                currentVar = null;
            } else if (line.startsWith("<FOR>")) {
                String name = line.replace("<FOR>", "").replace("</FOR>", "").trim();
                currentVar = variables.get(name);
            } else if (line.startsWith("<GIVEN>")) {
                String parent = line.replace("<GIVEN>", "").replace("</GIVEN>", "").trim();
                if (currentVar != null) currentVar.parents.add(parent);
            } else if (line.startsWith("<TABLE>")) {
                line = line.replace("<TABLE>", "").replace("</TABLE>", "").trim();
                String[] probs = line.split(" ");

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

    static double jointProbability(String query) {
        String[] assignments = query.substring(2, query.length() - 1).split(",");
        Map<String, String> evidence = new HashMap<>();
        for (String assign : assignments) {
            String[] parts = assign.split("=");
            evidence.put(parts[0].trim(), parts[1].trim());
        }

        double prob = 1.0;
        for (Variable var : variables.values()) {
            StringBuilder key = new StringBuilder();
            for (String parent : var.parents) {
                key.append(evidence.get(parent)).append(",");
            }
            key.append(evidence.get(var.name));
            int parentIndex = getParentIndex(var, evidence);
            int varIndex = var.outcomes.indexOf(evidence.get(var.name));
            String cptKey = parentIndex + "," + varIndex;
            double p = var.cpt.get(cptKey);
            prob *= p;
            multiplicationCount++;
        }
        return prob;
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

    static double variableElimination(String query) {
        // TO-DO: הוספת מימוש מלא של Variable Elimination (בהודעה הבאה כי זה חלק נפרד)
        return 0.0;
    }
}
