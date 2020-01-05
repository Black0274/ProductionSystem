package Java;

import java.util.List;

public class Rule {
    private List<String> condition;
    private String result;

    public Rule(List<String> condition, String result){
        this.condition = condition;
        this.result = result;
    }

    public List<String> getCondition() {
        return condition;
    }

    public String getResult() {
        return result;
    }

    public void setCondition(List<String> condition) {
        this.condition = condition;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString(){
        return String.join(", ", condition) + " -> " + result;
    }
}
