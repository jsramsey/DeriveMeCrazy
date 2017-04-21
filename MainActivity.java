package edu.fandm.jramsey.derivemecrazy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    public ArrayList<String> oldEqns = new ArrayList<String>();

    //keeps track so that each number can have a max of 1 decimal in it
    public boolean decimal;

    public static double curAnswer = 0;
    public static String curSign = "";
    //What the user sees
    public String display = "";
    //Text on the current button
    private String buttonText="";
    //for infix to postfix implementation, holds the last number typed by user
    private String curNum = "";

    private String[] postFix = new String[50];
    //to keep track of how many things have been put in postFix array
    private int idxCount = 0;
    private Stack operatorStack = new Stack();
    String history = "";
    //are we seeing the first character in expression?
    private boolean isFirst=true;
    //doesnt allow user to input 2 operations in a row
    public boolean isOperator = false;
    //makes sure user doesnt try to use the stored number after clearing
    public boolean wasCleared = true;
    //doesnt allow user to press enter twice without having numbers in equation
    //public boolean wasEntered = true;

    public int numLeftParen = 0;
    public int numRightParen = 0;
    ArrayList<String> infixEq= new ArrayList<String>();

    //one case not handles: single decimal point

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView)findViewById(R.id.display);
        tv.setText("enter equation");

        if(savedInstanceState != null){
            //restores all necessary values
            oldEqns = savedInstanceState.getStringArrayList("oldEqns");
            display = savedInstanceState.getString("display");
            decimal = savedInstanceState.getBoolean("decimal");
            curAnswer = savedInstanceState.getDouble("curAnswer");
            curSign = savedInstanceState.getString("curSign");
            buttonText = savedInstanceState.getString("buttonText");
            curNum = savedInstanceState.getString("curNum");
            postFix = savedInstanceState.getStringArray("postFix");
            idxCount = savedInstanceState.getInt("idxCount");
            isFirst = savedInstanceState.getBoolean("isFirst");
            isOperator = savedInstanceState.getBoolean("isOperator");
            wasCleared = savedInstanceState.getBoolean("wasCleared");
            //wasEntered = savedInstanceState.getBoolean("wasEntered");
            tv.setText(display);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("oldEqns", oldEqns);
        savedInstanceState.putString("display", display);
        savedInstanceState.putBoolean("decimal", decimal);
        savedInstanceState.putDouble("curAnswer",curAnswer);
        savedInstanceState.putString("curSign",curSign);
        savedInstanceState.putString("buttonText",buttonText);
        savedInstanceState.putString("curNum",curNum);
        savedInstanceState.putStringArray("postFix",postFix);
        savedInstanceState.putInt("idxCount",idxCount);
        savedInstanceState.putBoolean("isFirst",isFirst);
        savedInstanceState.putBoolean("isOperator",isOperator);
        savedInstanceState.putBoolean("wasCleared",wasCleared);
        //savedInstanceState.putBoolean("wasEntered",wasEntered);
    }
    public void onBackspace(View v) {
        ArrayList<String> operators = new ArrayList<String>(Arrays.asList("+", "-", "/", "*", "^"));
        ArrayList<String> numerics = new ArrayList<String>(Arrays.asList(".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        if (display.length() > 0) {
            String trigCheck="";
            String symbol = display.substring(display.length() - 1, display.length());
            String symbolBefore = "";
            if (display.length() > 1) {
                symbolBefore = display.substring(display.length() - 2, display.length() - 1);
            }
            if (display.length() > 3) {
                trigCheck = display.substring(display.length() - 4, display.length());
            }

            if (numerics.contains(symbol)) {
                display = display.substring(0, display.length() - 1);
                if (curNum.length() > 0) {
                    curNum = curNum.substring(0, curNum.length() - 1);
                }
                if (symbol.equals(".")) {
                    decimal = false;
                }

            } else if (operators.contains(symbol)) {
                display = display.substring(0, display.length() - 1);
                //remove symbol from equation
                infixEq.remove(infixEq.size() - 1);

                //if thing before was number, move back to curNum and remove from stack
                if (numerics.contains(infixEq.get(infixEq.size() - 1))) {
                    curNum = infixEq.get(infixEq.size() - 1);
                    infixEq.remove(infixEq.size() - 1);
                }
            } else if (trigCheck.equals("sin(") || trigCheck.equals("cos(") || trigCheck.equals("tan(")) { //the symbol is trig
                display = display.substring(0, display.length() - 4);
                infixEq.remove(infixEq.size() - 1); //remove left parenthesis
                numLeftParen -= 1;
                infixEq.remove(infixEq.size() - 1); //remove trig
                if (display.length()>0 && !display.substring(display.length() - 1, display.length()).equals(infixEq.get(infixEq.size() - 1))) {
                    infixEq.remove(infixEq.size() - 1); //remove implicit operator

                    curNum = infixEq.get(infixEq.size() - 1); //remove previous number and make curNum
                    infixEq.remove(infixEq.size() - 1);
                }
            } else if (symbol.equals(")")) {
                display = display.substring(0, display.length() - 1);
                //only thing that can precede it is a number
                infixEq.remove(infixEq.size() - 1);
                curNum = infixEq.get(infixEq.size() - 1);
                infixEq.remove(infixEq.size() - 1);
                numRightParen -= 1;
            } else if (symbol.equals("(")) {
                display = display.substring(0, display.length() - 1);
                //check display versus infixEq for whether multiplication was implicit
                infixEq.remove(infixEq.size() - 1);
                numLeftParen -= 1;
                if (symbolBefore.equals("")) {
                    display = display.substring(0, display.length() - 1);
                    infixEq.remove(infixEq.size() - 1);
                    isFirst = true;
                    numLeftParen -= 1;
                }
                if (!symbolBefore.equals(infixEq.get(infixEq.size() - 1))) {
                    infixEq.remove(infixEq.size() - 1); //remove implicit operator

                    curNum = infixEq.get(infixEq.size() - 1); //remove previous number and make curNum
                    infixEq.remove(infixEq.size() - 1);
                }
            }
            if (display.length() == 0) {
                isFirst = true;
            }
            TextView tv = (TextView) findViewById(R.id.display);
            tv.setText(display);
        }

    }

    public void onX(View v){
        TextView tv = (TextView) findViewById(R.id.display);
        Button b = (Button) v;
        buttonText = b.getText().toString();
        ArrayList<String> operators = new ArrayList<String>(Arrays.asList("+", "-", "/", "*", "^"));
        ArrayList<String> numerics = new ArrayList<String>(Arrays.asList(".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9","x"));

        if (!isFirst) {
            if (numerics.contains(display.substring(display.length() - 1, display.length()))){
                if (!display.substring(display.length() - 1, display.length()).equals("x")){
                    infixEq.add(curNum);
                    curNum="";
                }
                infixEq.add("*");



            } else if(display.substring(display.length() - 1, display.length()).equals(")")) {
                //display += "*";
                //add implicit multiplication
                infixEq.add("*");
            } else if (operators.contains(display.substring(display.length() - 1, display.length()))){
                //thing before it is an operator
            }
        }
        infixEq.add(buttonText);
        numLeftParen += 1;
        display += buttonText;
        tv.setText(display);



    }

    //keeps adding to curNum string (for multi digit numbers)
    public void onNumber(View v) {
        //list containing all operators to easily distinguish if the input is an operator
        ArrayList<String> operators = new ArrayList<String>(Arrays.asList("+","-","/","*","^"));
        wasCleared = false;
        //wasEntered = false;
        isOperator = false;
        TextView tv = (TextView) findViewById(R.id.display);
        Button b = (Button) v;
        buttonText = b.getText().toString();

        if (isFirst){
            tv.setText("");
            display = "";
            curNum = "";
            isFirst=false;
            numLeftParen=0;
            numRightParen=0;
            //if first, reset infixEq string
            infixEq = new ArrayList<String>();
        }
        if(buttonText.equals(".")){
            if(!decimal) {
                decimal = true;
                display += buttonText;
                tv.setText(display);
                curNum+=buttonText;
            }
            else{
                //Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show();
            }
        } else{
            display += buttonText;
            tv.setText(display);
            curNum+=buttonText;
        }

        if(operators.contains(buttonText)){
            decimal = false;
        }
    }

    public void onOperator(View v) {
        ArrayList<String> numerics = new ArrayList<String>(Arrays.asList(".","0","1","2","3","4","5","6","7","8","9"));
        ArrayList<String> invalids = new ArrayList<String>(Arrays.asList("+","-","/","*","^","("));
        TextView tv = (TextView) findViewById(R.id.display);
        Button b = (Button) v;
        buttonText = b.getText().toString();

        if(!wasCleared && !invalids.contains(display.substring(display.length()-1,display.length()))){
            Log.d(TAG,"it is a "+buttonText+ " operator");
            if (isFirst) {
                numLeftParen=0;
                numRightParen=0;
                curNum = Double.toString(curAnswer);
                display = Double.toString(curAnswer);
                //infixEq = new ArrayList<String>();
                //infixEq.add(Double.toString(curAnswer));
                isFirst = false;
            }

            decimal = false;
            if (!display.substring(display.length()-1,display.length()).equals(")")){
                infixEq.add(curNum);
            }
            curNum = "";
            display += buttonText;
            infixEq.add(buttonText);
            tv.setText(display);
        } else if((buttonText.equals("-") && (wasCleared || !numerics.contains(display.substring(display.length()-1,display.length()))))){
            //its a negative sign
            if (isFirst) {
                Log.d(TAG,"- sign is first");
                numLeftParen=0;
                numRightParen=0;
                infixEq = new ArrayList<String>();
                isFirst = false;
            }
            Log.d(TAG,"its a - sign");
            curNum += "-";
            display+="-";
            tv.setText(display);
        } else if(wasCleared){
            Toast.makeText(this, "Invalid Input, No stored value.", Toast.LENGTH_SHORT).show();
        }
        else{
            //Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
        }

    }


    public void onTrig(View v){
        ArrayList<String> numerics = new ArrayList<String>(Arrays.asList(".","0","1","2","3","4","5","6","7","8","9"));
        if(isFirst){
            curNum="";
            display="";
            isFirst = false;
        }

        TextView tv = (TextView) findViewById(R.id.display);
        Button b = (Button) v;
        buttonText = b.getText().toString();

        if(display.length()>0) {
            if (numerics.contains(display.substring(display.length() - 1, display.length()))) {
                infixEq.add(curNum);
                infixEq.add("*");
                curNum = "";
                decimal = false;
            } else if (display.substring(display.length() - 1, display.length()).equals(")")){
                infixEq.add("*");
            }
        }
        numLeftParen+=1;
        infixEq.add(buttonText.substring(0,3));
        infixEq.add(buttonText.substring(3,4));
        display += buttonText;
        tv.setText(display);
    }

    public void onParen(View v) {
        TextView tv = (TextView) findViewById(R.id.display);
        Button b = (Button) v;
        buttonText = b.getText().toString();
        ArrayList<String> operators = new ArrayList<String>(Arrays.asList("+", "-", "/", "*", "^"));
        ArrayList<String> numerics = new ArrayList<String>(Arrays.asList(".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        if (buttonText.equals(")")) {
            if (display.length() > 0) {
                if (operators.contains(display.substring(display.length() - 1, display.length())) || numRightParen == numLeftParen ||
                        display.substring(display.length() - 1, display.length()).equals("(")) {
                    //Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                } else if (display.substring(display.length() - 1, display.length()).equals(")")){
                    numRightParen += 1;
                    display += buttonText;
                    //infixEq.add(curNum);
                    infixEq.add(buttonText);
                } else{
                    numRightParen += 1;
                    display += buttonText;
                    infixEq.add(curNum);
                    infixEq.add(buttonText);
                }
            }
        } else {           //else its a left paren
            //add an implicit multiplication is a parenthesis follows a number or a right parenthesis
            Log.d(TAG, "cur Display: "+display);
            if (!isFirst) {
                if (numerics.contains(display.substring(display.length() - 1, display.length()))){
                    Log.d(TAG, "this is the case we're looking for");
                    infixEq.add(curNum);
                    infixEq.add("*");
                    infixEq.add(buttonText);
                    curNum="";


                } else if(display.substring(display.length() - 1, display.length()).equals(")")) {
                    //display += "*";
                    //add implicit multiplication
                    infixEq.add("*");
                    infixEq.add(buttonText);
                } else if (operators.contains(display.substring(display.length() - 1, display.length()))){
                    //thing before it is an operator
                    infixEq.add(buttonText);
                }
            } else{
                //Log.d(TAG, "( is first");
                //its first, get old num
                if (isFirst ) {
                    //Log.d(TAG, "( is first");
                    numLeftParen=0;
                    numRightParen=0;
                    if (!curNum.equals("")) {
                        curNum = Double.toString(curAnswer);
                        display = Double.toString(curAnswer);
                        Log.d(TAG, "curAns: " + curAnswer);
                        //infixEq = new ArrayList<String>();
                        //infixEq.add(Double.toString(curAnswer));
                        isFirst = false;
                        infixEq.add(curNum);
                        infixEq.add("*");
                    }
                    infixEq.add(buttonText);
                }

            }
            numLeftParen += 1;
            display += buttonText;

        }
        curNum = "";
        isOperator = false;
        isFirst = false;
        decimal = false;
        tv.setText(display);
    }

    public String printInfixEq(){
        String tmp="";
        for (int i = 0; i<infixEq.size();i++){
            tmp+= infixEq.get(i);
            if (infixEq.get(i).equals("")){
                tmp+=" _ ";
            }
        }
        return tmp;
    }


    public void enter(View v) {
        Log.d(TAG,"infixEq: "+ printInfixEq());
        ArrayList<String> invalidEnds = new ArrayList<String>(Arrays.asList("+" , "-" , "/" , "*" , "^" , "sin(" , "cos(" , "tan(","(" ) );
        if (numLeftParen != numRightParen){
            Toast.makeText(this, "Parentheses Mismatch", Toast.LENGTH_SHORT).show();
        } else if (display.length()==0){
            //do nothing
        } else if (invalidEnds.contains(display.substring(display.length()-1,display.length()))){
            Toast.makeText(this, "Invalid Expression", Toast.LENGTH_SHORT).show();
        } else {
            TextView tv = (TextView) findViewById(R.id.display);

            history += display;
            if(!curNum.equals("")){
                infixEq.add(curNum);
            }

            postFix = parseString();
            curAnswer = compute(postFix);
            curAnswer = Math.round(curAnswer * 10000.0) / 10000.0;
            tv.setText(Double.toString(curAnswer));
            display = Double.toString(curAnswer);
            //postFix = new String[50];
            curSign = "";
            curNum = Double.toString(curAnswer);
            //Log.d(TAG, "curNum after enter: " + curNum);
            //idxCount = 0;
            isFirst = true;
            history += " = " + display + "\n";
            Log.d(TAG, history);

            curNum = "";
            String newEqn = history;
            oldEqns.add(newEqn);
            history = "";
            infixEq=new ArrayList<String>();
            numLeftParen=0;
            numRightParen=0;
            //wasEntered = true;
        }
    }

    //print postFix, for debugging purposes
    public String printPostFix(){
        String postFixString="";
        for (int i = 0; i<idxCount ;i++){
            postFixString += postFix[i];
        }
        return postFixString;
    }

    //Does calculation on postfix
    public double compute(String[] postFix) throws IllegalArgumentException {
        try {
            double answer = 0;
            double result;
            Deque<Double> stack = new ArrayDeque<Double>();
            for (String item : postFix) {
                if (item != null) {
                    //Log.d(TAG, "item: " + item);
                    try {
                        //Log.d(TAG, "got into try");
                        double num = Double.parseDouble(item);
                        stack.addFirst(num);
                    } catch (NumberFormatException nfe) {
                        //Log.d(TAG, "got into catch"+item);
                        if (item.equals("+")) {
                            result = stack.removeFirst() + stack.removeFirst();
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("-")) {
                            double num2 = stack.removeFirst();
                            double num1 = stack.removeFirst();
                            result = num1 - num2;
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("*")) {
                            result = stack.removeFirst() * stack.removeFirst();
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("/")) {
                            double num2 = stack.removeFirst();
                            double num1 = stack.removeFirst();
                            result = num1 / num2;
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("^")) {
                            double num2 = stack.removeFirst();
                            double num1 = stack.removeFirst();
                            result = Math.pow(num1, num2);
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("sin")) {
                            result = Math.sin(stack.removeFirst());
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("cos")) {
                            result = Math.cos(stack.removeFirst());
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else if (item.equals("tan")) {
                            result = Math.tan(stack.removeFirst());
                            stack.addFirst(result);
                            //Log.d(TAG, Double.toString(result));
                        } else {
                            //Log.d(TAG, "There is an invalid sign going into postfix");
                        }
                    }
                }
            }
            Log.d(TAG, "we are done calculating");
            answer = stack.removeFirst();
            if (stack.isEmpty()) {
                return answer;
            } else {
                Toast.makeText(this, "Not a valid expression", Toast.LENGTH_SHORT).show();
                return 0;


            }
        }
        catch (IllegalArgumentException e1){
            Log.d(TAG, "Illegal Argument Exception)");
            return 0;
        }

    }

    //function to replace variables like x and e with their numerical values
    public void removeVariables(ArrayList postfix, float x){
        for (int i=0; i<postfix.size();i++){
            if (postfix.get(i)=="x"){
                postfix.set(i,x);
            } else if ( postfix.get(i)=="e"){
                postfix.set(i, Math.E);
            }
        }


    }

    //NEW WAY TO IMPLEMENT POSTFIX BELOW
    //sin(5)+sin(5) breaks it
    //55sin+sin <- postfix
    //This method and helper methods based off of code from http://www.dreamincode.net/forums/topic/305743-parsing-functions-in-postfix/
    public String[] parseString() {
        Log.d(TAG,"infixEq: "+ printInfixEq());
        Log.d(TAG, "input String: "+display);
        ArrayList postfixEquation = new ArrayList();
        Stack stack = new Stack();
        String curNum = "";     // Holds current number
        String curToken = "";   // Holds current token
        String curOp = "";      //Holds current operator or function

        for (int i = 0; i < infixEq.size(); i++) {
            curToken = (String)infixEq.get(i);
            Log.d(TAG, "retrieved from infix: "+ curToken);
            if (isNumber(curToken))     //If the current token is a number
            {
                curNum += curToken;     //Add it to the curNumber string

                if (!curOp.isEmpty())    // There is an operator or function in curOp
                {
                    Log.d(TAG, "pushed to stack: "+ curOp);
                    pushToStack(curOp, stack, postfixEquation); // Push it onto the stack
                    curOp = ""; // Reset curOp
                }
            } else                        // Not a number
            {

                if (!curNum.isEmpty())               // If there is a number in curNum
                {
                    postfixEquation.add(curNum);    // Add the current number to the postfix string
                    curNum = "";                    // Reset curNum
                }


                if (curToken.equals("("))    // If an opening parenthesis is encountered
                {

                    if(!curOp.equals("")) {  // Push the current operator or function onto the stack
                        stack.push(curOp);
                        Log.d(TAG, "pushed to stack: "+ curOp);
                        // Reset curOp
                        curOp = "";
                    }

                    Log.d(TAG, "pushed to stack: "+ curToken);
                    stack.push(curToken);   // Push opening parenthesis onto stack

                } else if (curToken.equals(")")) {
                    while (!stack.empty()) {   // Pop everything off the stack until a left parenthesis is encountered

                        if (stack.peek().equals("(")) {
                            stack.pop(); // Discard the opening parenthesis
                            if (stack.peek().equals("sin") ||stack.peek().equals("cos") ||stack.peek().equals("tan")){ //if trig, add to postfix
                                postfixEquation.add(stack.pop());
                            }
                            break;
                        }
                        Log.d(TAG, "added to postfix: "+ stack.peek());
                        postfixEquation.add(stack.pop());
                    }
                }
                //This automatically pushes operators onto the stack. This is to avoid something like 4+sin(45) coming out as 4 45 +sin
                else if (curToken.equals("*")) {
                    pushToStack(curToken, stack, postfixEquation);
                    Log.d(TAG, "pushed to stack: " + curToken);
                }
                else if (curToken.equals("/")) {
                    pushToStack(curToken, stack, postfixEquation);
                    Log.d(TAG, "pushed to stack: " + curToken);
                }
                else if (curToken.equals("+")) {
                    pushToStack(curToken, stack, postfixEquation);
                    Log.d(TAG, "pushed to stack: " + curToken);
                }
                else if (curToken.equals("-")) {
                    pushToStack(curToken, stack, postfixEquation);
                    Log.d(TAG, "pushed to stack: " + curToken);
                }
                else if (curToken.equals("^")) {
                    pushToStack(curToken, stack, postfixEquation);
                    Log.d(TAG, "pushed to stack: " + curToken);
                }
                else
                    curOp += curToken;
            }
        }
        if (!curNum.isEmpty())
            postfixEquation.add(curNum);    // Add any remaining numbers to the equation
        if (!curOp.isEmpty())
            pushToStack(curOp, stack, postfixEquation); // Add any remaining operators or functions onto the stack
        while (!stack.empty())
            postfixEquation.add(stack.pop());   // Add remaining operators and functions to the equation
        String output = "";
        postFix = new String[postfixEquation.size()];
        for (int i = 0; i < postfixEquation.size(); i++) {
            output += (postfixEquation.get(i));
            postFix[i] = (String) postfixEquation.get(i);
        }
        Log.d(TAG, "postfix: "+output);
        Log.d(TAG, "Answer is: "+compute(postFix));
        return postFix;
    }

    //for implementing parseString
    public void pushToStack(String curOp, Stack stack, ArrayList postfixEquation ){
        if (curOp.equals("+") || curOp.equals("-")) {
            //if top of stack has higher presidence, pop off, add to postFix, and then push new op onto stack
            while (!stack.isEmpty() && (stack.peek().equals("*") || stack.peek().equals("/") || stack.peek().equals("^") || stack.peek().equals("-") || stack.peek().equals("+") )) {
                postfixEquation.add(stack.pop().toString());
            }
            stack.push(curOp);
        } else if (curOp.equals("*") || curOp.equals("/")) {
            while (!stack.isEmpty() && (stack.peek().equals("*") || stack.peek().equals("/") || stack.peek().equals("^") )) {
                postfixEquation.add(stack.pop().toString());
            }
            stack.push(curOp);
        } else if (curOp.equals("^")) {
            while (!stack.isEmpty() && (stack.peek().equals("^") )) {
                postfixEquation.add(stack.pop().toString());
            }
            stack.push(curOp);
        } else{
            Log.d(TAG, "we are in this case" +curOp);
            stack.push(curOp);
        }
    }


    //For implementing parseString
    public boolean isNumber(String currToken) {

        try
        {
            Double.parseDouble(currToken);
            Log.d(TAG, "it's a number!");
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }


    //resets everything
    public void clear(View v) {
        curAnswer = 0;
        curSign = "";
        display = "";
        curNum = "";
        history ="";
        isOperator=false;
        postFix = new String[50];
        infixEq = new ArrayList<String>();
        idxCount = 0;
        TextView tv = (TextView)findViewById(R.id.display);
        tv.setText("enter equation");
        isFirst=true;
        wasCleared=true;
        //wasEntered=true;
        operatorStack.clear();
        numLeftParen=0;
        numRightParen=0;
    }


    public void history(View v){
        for(int i=0;i<oldEqns.size();i++){
            Log.d("Helpppp",oldEqns.get(i));
        }

        Intent i = new Intent(this, edu.fandm.jramsey.derivemecrazy.HistoryActivity.class);
        i.putExtra("list", oldEqns);

        startActivity(i);
    }
    public void graph(View v){


        Intent i = new Intent(this, edu.fandm.jramsey.derivemecrazy.Graph.class);


        startActivity(i);
    }
    public void calculate(View v){


        Intent i = new Intent(this, edu.fandm.jramsey.derivemecrazy.Calculate.class);


        startActivity(i);
    }
}
