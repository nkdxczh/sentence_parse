/**
 * Generate sentences from a CFG
 * 
 * @author sihong
 *
 */

import java.io.*;
import java.util.*;

public class Generator {

    private Grammar grammar;

    private Random random;

    private int MAX_LEVEL = 100;

    /**
     * Constructor: read the grammar.
     */
    public Generator(String grammar_filename) {
        grammar = new Grammar(grammar_filename);
        random = new Random();
        //grammar.printGrammarInfo();
    }

    private RHS randomRHS(ArrayList<RHS> rhss){
        double[] acc = new double[rhss.size()];
        acc[0] = rhss.get(0).getProb();
        for(int i = 1; i < rhss.size(); ++i)
            acc[i] = acc[i-1] + rhss.get(i).getProb();

        double r = random.nextFloat() * acc[rhss.size() - 1];

        for(int i = 0; i < rhss.size(); ++i){
            if(r <= acc[i]){
                return rhss.get(i);
            }
        }

        return rhss.get(0);
    }

    private String dfs(String lhs, int level){
        if(grammar.symbolType(lhs) == 0)return lhs;

        if(level >= MAX_LEVEL)return "";

        String res = "";

        ArrayList<RHS> rhss = grammar.findProductions(lhs);

        RHS rhs = randomRHS(rhss);

        String left = dfs(rhs.first(), level+1);
        if(left.equals(""))return "";
        if(rhs.second() != null){
            String right = dfs(rhs.second(), level+1);
            if(right.equals(""))return "";
            res = "(" + lhs + " " + left + " " + right + ")";
        }
        else{
            res = "(" + lhs + " " + left + ")";
        }

        return res;
    }

    /**
     * Generate a number of sentences.
     */
    public ArrayList<String> generate(int numSentences) {
        ArrayList<String> res = new ArrayList<>();
        int round = 0;
        while(round < numSentences){
            String s = dfs("ROOT", 0);
            if(!s.equals("")){
                res.add(s);
                round++;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        // the first argument is the path to the grammar file.
        Generator g = new Generator(args[0]);
        ArrayList<String> res = g.generate(100);
        for (String s : res) {
            System.out.println(s);
        }
    }
}
