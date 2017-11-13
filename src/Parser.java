/**
 * Parser based on the CYK algorithm.
 */

import java.io.*;
import java.util.*;

public class Parser {

    class Element{
        public List<Double> prob;
        public List<String> lhs;
        public List<RHS> rhs;
        public List<Element> left;
        public List<Element> right;
        public boolean isLeaf;
        public String word;

        public Element(){
            prob = new ArrayList<>();
            lhs = new ArrayList<>();
            rhs = new ArrayList<>();
            left = new ArrayList<>();
            right = new ArrayList<>();
            isLeaf = false;
            word = null;
        }

        public int index(String target){
            for(int i = 0; i < lhs.size(); ++i)
                if(lhs.get(i).equals(target))return i;
            return -1;
        }

        public void print(){
            if(isLeaf){
                System.out.println(word);
            }
            for(int i = 0; i < lhs.size(); ++i){
                System.out.println(lhs.get(i) + " " + prob.get(i));
            }
        }
    }

    public Grammar g;
    public Element[][] dp;

    /**
     * Constructor: read the grammar.
     */
    public Parser(String grammar_filename) {
        g = new Grammar(grammar_filename);
    }

    /**
     * Parse one sentence given in the array.
     */
    public void parse(ArrayList<String> sentence) {
        dp = new Element[sentence.size()][sentence.size()];

        for(int i = 0; i < sentence.size(); ++i){
            for(int j = i; j >= 0; --j){
                dp[j][i] = new Element();
                if(i == j){
                    dp[j][i].isLeaf = true;
                    String target = sentence.get(i);
                    List<String> lhss = g.findPreTerminals(target);
                    for(String lhs : lhss){
                        List<RHS> rhss = g.findProductions(lhs);
                        double totalProb = 0;
                        double targetProb = 0;
                        for(RHS rhs : rhss){
                            if(rhs.first().equals(target))targetProb = rhs.getProb();
                            totalProb += rhs.getProb();
                        }
                        dp[j][i].lhs.add(lhs);
                        dp[j][i].prob.add(targetProb / totalProb);
                        dp[j][i].word = target;
                    }
                }
                else{
                    for(int k = j; k < i; ++k){
                        Element leftEle = dp[j][k];
                        Element rightEle = dp[k + 1][i];
                        for(int l = 0; l < leftEle.lhs.size(); ++l){
                            for(int r = 0; r < rightEle.lhs.size(); ++r){
                                String target = leftEle.lhs.get(l) + " " + rightEle.lhs.get(r);
                                double probBase = leftEle.prob.get(l) * rightEle.prob.get(r);

                                List<String> lhss = g.findLHS(target);
                                if(lhss == null)continue;
                                for(String lhs : lhss){
                                    List<RHS> rhss = g.findProductions(lhs);
                                    double totalProb = 0;
                                    double targetProb = 0;
                                    RHS targetRhs = null;
                                    for(RHS rhs : rhss){
                                        if(target.equals(rhs.first() + " " + rhs.second())){
                                            targetProb = rhs.getProb();
                                            targetRhs = rhs;
                                        }
                                        totalProb += rhs.getProb();
                                    }
                                    int index = dp[j][i].index(lhs);
                                    double prob = (targetProb / totalProb) * probBase;
                                    if(index == -1){
                                        dp[j][i].lhs.add(lhs);
                                        dp[j][i].rhs.add(targetRhs);
                                        dp[j][i].prob.add(prob);
                                        dp[j][i].left.add(leftEle);
                                        dp[j][i].right.add(rightEle);
                                    }
                                    else if(dp[j][i].prob.get(index) < prob){
                                        dp[j][i].lhs.set(index, lhs);
                                        dp[j][i].rhs.set(index, targetRhs);
                                        dp[j][i].prob.set(index, prob);
                                        dp[j][i].left.set(index, leftEle);
                                        dp[j][i].right.set(index, rightEle);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String dfs(Element ele, String lhs){
        if(lhs == null)return "fail";
        int index = ele.index(lhs);
        if(index == -1)return "fail";
        if(ele.isLeaf)return "(" + lhs + " " + ele.word + ")";

        RHS rhs = ele.rhs.get(index);
        if(rhs == null)return "fail";

        String left = rhs.first();
        String right = rhs.second();
        if(left == null || right == null)return "fail";

        String lefts = dfs(ele.left.get(index), left);
        String rights = dfs(ele.right.get(index), right);

        if(lefts.equals("fail") || rights.equals("fail"))return "fail";

        return "(" + lhs + " " + lefts + " " + rights + ")";
    }

    /**
     * Print the parse obtained after calling parse()
     */
    public String PrintOneParse() {
        return dfs(dp[0][dp.length - 1], "S");
    }

    public static void main(String[] args) {
        // read the grammar in the file args[0]
        Parser parser = new Parser(args[0]);

        int count = 0;
        int correct = 0;

        // read a parse tree from a bash pipe
        try {
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader bufReader = new BufferedReader(isReader);
            while(true) {
                String line = null;
                if((line=bufReader.readLine()) != null) {
                    ArrayList<String> sentence = new ArrayList<>();
                    String end = line.substring(line.length() - 2, line.length() - 1);
                    String []words = line.split(" ");
                    for (String word : words) {
                        word = word.replaceAll("[^a-zA-Z]", "");
                        if (word.length() == 0) {
                            continue;
                        }
                        // use the grammar to filter out non-terminals and pre-terminals
                        if (parser.g.symbolType(word) == 0 && (!word.equals(".") && !word.equals("!"))) {
                            sentence.add(word);
                        }
                    }
                    parser.parse(sentence);
                    String res = "(ROOT " + parser.PrintOneParse() + " " + end + ")";
                    System.out.println(res);
                    if(res.equals(line)){
                        correct++;
                    }
                    else{
                        //System.out.println(line);
                    }
                    count++;
                }
                else {
                    break;
                }
            }
            bufReader.close();
            isReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println((float)correct / count);
    }
}
