package Java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        final String RULES_PATH = ".\\src\\data\\rules.txt";
        final String FACTS_PATH = ".\\src\\data\\facts.txt";
        final String TARGET_PATH = ".\\src\\data\\target.txt";

        while (true) {
            System.out.print("Выберите алгоритм вывода: ");
            Scanner sc = new Scanner(System.in);
            String choose = sc.next();
            if (choose.equals("c") || choose.equals("C")){
                break;
            }

            List<String> text = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(RULES_PATH))) {
                String str;
                boolean flag = true;
                while ((str = reader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        if (flag) {
                            text.add(str.substring(1));
                            flag = false;
                        } else {
                            text.add(str);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<Rule> rules = parse(text);     // Список правил
            Set<String> marques = new HashSet<>();
            for (Rule rule : rules){
                for (String fact : rule.getCondition()){
                    if (Character.isUpperCase(fact.charAt(0))){
                        marques.add(fact);
                    }
                }
            }

            Set<String> models = new HashSet<>();
            for (Rule rule : rules){
                if (Character.isUpperCase(rule.getResult().charAt(0)) &&
                        !marques.contains(rule.getResult())){
                    models.add(rule.getResult());
                }
            }

            Set<String> facts = new HashSet<>();    // Множество всех фактов
            for (Rule rule : rules) {
                facts.add(rule.getResult());
                facts.addAll(rule.getCondition());
            }
            Map<String, TreeNode> map = new HashMap<>();
            for (String fact: facts){
                TreeNode node = new TreeNode(fact);
                map.put(fact, node);
            }
            for (Rule rule : rules) {                   // Заполнение дерева
                Set<TreeNode> nodeSet = new HashSet<>();
                for (String fact: rule.getCondition()){
                    nodeSet.add(map.get(fact));
                }
                map.get(rule.getResult()).branches.add(nodeSet);
            }

            Set<String> trueFacts = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(FACTS_PATH))) {
                String str;
                boolean flag = true;
                while ((str = reader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        if (flag) {
                            trueFacts.add(str.substring(1));
                            flag = false;
                        } else {
                            trueFacts.add(str);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String target = "";
            try (BufferedReader reader = new BufferedReader(new FileReader(TARGET_PATH))) {
                target = reader.readLine();
                target = target.substring(1, target.length());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (choose.equals("B")){
                backwardReasoning(map, trueFacts, target);
            }
            else if (choose.equals("b")) {
                backwardOldReasoning(rules, trueFacts, target);
            }
            else if (choose.equals("FS") || choose.equals("fs")){
                forwardReasoningSingle(rules, trueFacts, target);
            }
            else {
                forwardReasoning(rules, trueFacts, models);
            }
            System.out.println();
        }
    }

    private static List<Rule> parse(List<String> text){
        List<Rule> rules = new LinkedList<>();
        for (String line: text) {
            int arrow = line.indexOf("->");
            ArrayList<String> left = new ArrayList<>(Arrays.asList(line.substring(0, arrow - 1)
                    .split(", ")));
            String right = line.substring(arrow + 2).trim();
            rules.add(new Rule(left, right));
        }
        return rules;
    }


    private static void forwardReasoning(List<Rule> rules, Set<String> facts, Set<String> models){
        System.out.println("Прямой вывод:");
        Set<String> trueFacts = new HashSet<>(facts);
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("\nВам подойдут:\n");

        while (true){
            int trueFactsCount = trueFacts.size();
            for (Rule rule : rules) {
                if (rule.getCondition().stream().anyMatch(fact -> !(trueFacts.contains(fact)))
                        || trueFacts.contains(rule.getResult())) {
                    continue;
                }
                System.out.println(rule);
                if (models.contains(rule.getResult())){
                    recommendation.append(rule.getResult()).append("\n");
                }
                trueFacts.add(rule.getResult());
            }
            if (trueFactsCount == trueFacts.size()) {
                break;
            }
        }

        if (recommendation.length() == 15){
            recommendation.append("ничего\n");
        }
        recommendation.deleteCharAt(recommendation.length() - 1);
        System.out.println(recommendation.toString());
    }


    private static void backwardReasoning(Map<String, TreeNode> map, Set<String> trueFacts, String target){
        if (map.isEmpty()) {
            return;
        }
        TreeNode targetNode = map.get(target);
        Map<String, Set<TreeNode>> dict = new HashMap<>();
        Set<TreeNode> inference = new HashSet<>();
        Set<TreeNode> currentSet = new HashSet<>();
        inference.add(targetNode);
        currentSet.add(targetNode);

        mainloop:
        while (currentSet.size() > 0){
            for (TreeNode currentNode : currentSet) {
                System.out.print(currentNode.fact + " ");
            }
            boolean fail = true;
            for (TreeNode currentNode : currentSet) {
                if (currentNode.branches.size() != 0){
                    fail = false;
                    break;
                }
            }
            if (fail){
                System.out.println("FAIL 1");
                return;
            }

            Set<TreeNode> nextSet = new HashSet<>();
            for (TreeNode currentNode : currentSet){
                Set<TreeNode> newAdditions = new HashSet<>();
                for (Set<TreeNode> branch : currentNode.branches){
                    int count = 0;
                    for (TreeNode node: branch){
                        if (!trueFacts.contains(node.fact)){
                            newAdditions.add(node);
                            count++;
                        }
                        else{
                            inference.add(node);
                        }
                    }
                    if (count == 0){
                        newAdditions.clear();
                        break;
                    }
                }
                if (newAdditions.size() != 0) {
                    dict.put(currentNode.fact, newAdditions);
                    nextSet.addAll(newAdditions);
                }
            }
            if (currentSet.equals(nextSet)){
                System.out.println("FAIL 2");
                return;
            }
            currentSet = nextSet;
            System.out.println();
        }
        System.out.println("SUCCESS 2");
    }


    private static void forwardReasoningSingle(List<Rule> rules, Set<String> trueFacts, String target){
        Set<Node> currentSet = new HashSet<>();
        currentSet.add(new Node(trueFacts));
        Node targetNode = null;
        while (targetNode == null){
            Set<Node> nextSet = new HashSet<>();
            for (Node node : currentSet){
                for (Rule rule : rules) {
                    if (rule.getCondition().stream().anyMatch(fact -> !(node.getSet().contains(fact)))) {
                        continue;
                    }
                    Node newNode = new Node(new HashSet<>(node.getSet()), node, rule);
                    newNode.getSet().add(rule.getResult());
                    nextSet.add(newNode);
                    if (newNode.getSet().contains(target)) {
                        targetNode = newNode;
                    }
                }
            }
            if (currentSet.equals(nextSet)){
                break;
            }
            currentSet = nextSet;
        }
        if (targetNode == null){
            System.out.println("Вывод невозможен");
            return;
        }

        List<String> output = new ArrayList<>();
        while (targetNode.getParent() != null){
            output.add(targetNode.getRule().toString());
            targetNode = targetNode.getParent();
        }
        Collections.reverse(output);
        System.out.println("Прямой вывод:");
        for (String rule : output){
            System.out.println(rule);
        }
    }


    private static void backwardOldReasoning(List<Rule> rules, Set<String> trueFacts, String target){
        Set<Node> currentSet = new HashSet<>();
        Set<String> initial = new HashSet<>();
        initial.add(target);
        currentSet.add(new Node(initial));
        Node sourceNode = null;

        mainloop:
        while (true){
            Set<Node> nextSet = new HashSet<>();
            for (Node node : currentSet){
                List<Rule> activeRules = rules.stream().filter(
                        x -> !trueFacts.contains(x.getResult()) &&
                                node.getSet().contains(x.getResult()))
                        .collect(Collectors.toList());
                for (Rule rule : activeRules){
                    Set<String> condSet = new HashSet<>(node.getSet());
                    condSet.remove(rule.getResult());
                    condSet.addAll(rule.getCondition());
                    Node newNode = new Node(condSet, node, rule);
                    nextSet.add(newNode);
                    if (trueFacts.containsAll(condSet)){
                        sourceNode = newNode;
                        break mainloop;
                    }
                }
            }
            if (currentSet.equals(nextSet)){
                break;
            }
            currentSet = nextSet;
        }

        if (sourceNode == null){
            System.out.println("Вывод невозможен");
            return;
        }

        List<String> output = new ArrayList<>();
        while (sourceNode.getParent() != null){
            output.add(sourceNode.getRule().toString());
            sourceNode = sourceNode.getParent();
        }
        Collections.reverse(output);
        System.out.println("Обратный вывод:");
        for (String rule : output){
            System.out.println(rule);
        }
    }
}
