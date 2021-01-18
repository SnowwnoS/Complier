import java.io.*;
import java.util.*;

public class complier {
    static int tnum = 1;//记录临时变量个数
    static String[] option = {"+","-","*","/","(",")","#"};//操作符号表
    static String[] actionSymbol = {"G","X","Y","Z","P"};//动作符号表
    static ArrayList<String> VT = new ArrayList<>();//非终结字符表
    static ArrayList<String> Li = new ArrayList<>();//字母表
    static ArrayList<String> VN = new ArrayList<>();//终结字符表
    static Stack<String> SYN = new Stack<>();//语法栈
    static Stack<String> SEM = new Stack<>();//语义栈
    static ArrayList<String> QT = new ArrayList<>();//四元式列表
    static Stack<String> PW = new Stack<>();//用于存放PUSH元素的栈
    static String[][] table = new String[5][8];//LL（1）分析表
    static int expCon = 0;//算术表达式指针
    public static void main(String[] args){
        System.out.println("请输入需要编译的算术表达式（结尾需要有#）：");
        String[] expression = new Scanner(System.in).next().split("");//输入算术表达式并分割

        for (String s : expression) {  //初始化字母表
            if (!Arrays.asList(option).contains(s)) {
                Li.add(s);
            }
        }
        System.out.println("输入的算术表达式的字母有：");
        System.out.println(Li.toString());

        //初始化非终结字符表
        VT.add("E");
        VT.add("M");//E'
        VT.add("T");
        VT.add("H");//T'
        VT.add("F");
        VN.addAll(Li);//向终结字符表添加字母
        VN.addAll(Arrays.asList(option));//向终结字符表添加操作符

        initialTable();//初始化分析表
        SYN.push("#");
        SYN.push("E");//初始化语法栈
        try {
            while(!SYN.peek().equals("#")){
                System.out.println("当前的SYN栈："+SYN.toString());
                System.out.println("当前的SEM栈："+SEM.toString());
                String nowW = expression[expCon];//获取当前表达式指针对应的符号
                String nowX = SYN.pop();//从语法栈弹出符号
                System.out.println("目前的x和w分别为："+nowX+" "+nowW);
                if(Arrays.asList(actionSymbol).contains(nowX)){//判断是否是动作符号
                    System.out.println("读取到动作符号"+nowX);
                    doAction(nowX,nowW);//处理相应的动作符号
                    System.out.println("————————————————————————————————————————————");
                    continue;
                }
                if(nowX.equals(nowW)){//处理读取到终结符号
                    System.out.println("读取到终结符号"+nowX+",移动表达式指针");
                    expCon += 1;//读取到终结符号，移动表达式指针
                }
                else{
                    doSomething(nowX,nowW);//否则查表执行相应操作
                }
                System.out.println("————————————————————————————————————————————");
            }
        }catch(Exception e){
            System.err.println("输入的算数表达式有误！请检查表达式是否符合规范！");
        }

        System.out.println("分析结束！得到的所有四元式中间代码如下：");
        for (String s : QT) {
            System.out.println(s);
        }
    }
    public static void initialTable(){
        //所有的操作需要逆序压栈，所以根据文法所有字符串已经逆序，方便后续操作
        //E' = M,T' = H,Push(i) = P,GEQ(+)=G,GEQ(-)=X,GEQ(*)=Y,GEQ(/)=Z
        table[0][0] = "MT";//E->TE'-1
        table[0][5] = "MT";//-1
        table[1][1] = "MGT+";//E'->+T{GEQ(+)}E'-2
        table[1][2] = "MXT-";//E'->-T{GEQ(-)}E'-3
        table[1][6] = "";//E'->null-4
        table[1][7] = "";//-4
        table[2][0] = "HF";//T->FT'-5
        table[2][5] = "HF";//-5
        table[3][1] = "";//T'->null-8
        table[3][2] = "";//-8
        table[3][3] = "HYF*";//T'->*F{GEQ(*)}T'-6
        table[3][4] = "HZF/";//T'->/F{GEQ(/)T'-7
        table[3][6] = "";//-8
        table[3][7] = "";//-8
        table[4][0] = "Pi";//F->i{PUSH(i)}-9
        table[4][5] = ")E(";//F->(E)-10
        System.out.println("LL(1)法四元式翻译器的分析表如下：");
        for(int i = 0;i < 5;i++){
            System.out.println(Arrays.toString(table[i]));
        }
    }
    public static void doAction(String x,String w){//处理栈顶为动作符号时的操作
        //E' = M,T' = H,Push(i) = P,GEQ(+)=G,GEQ(-)=X,GEQ(*)=Y,GEQ(/)=Z
        if(x.equals("P")){
            SEM.push(PW.pop());//从同步的PW栈中读取要操作的元素
        }
        //以上执行Push操作，将PW中和PUSH同步的元素压入SEM栈
        if(x.equals("G")){
            SEM.push("+");
            String semop = SEM.pop();//弹出SEM栈顶的操作符
            String semw1 = SEM.pop();//弹出栈顶的第二个操作数
            String semw2 = SEM.pop();//弹出栈顶第一个操作数
            String temp = printFour(semw2,semw1,semop);//产生临时结果并打印四元式
            SEM.push(temp);//将临时结果压栈
        }
        if(x.equals("X")){
            SEM.push("-");
            String semop = SEM.pop();//弹出SEM栈顶的操作符
            String semw1 = SEM.pop();//弹出栈顶的第二个操作数
            String semw2 = SEM.pop();//弹出栈顶第一个操作数
            String temp = printFour(semw2,semw1,semop);//产生临时结果并打印四元式
            SEM.push(temp);//将临时结果压栈
        }
        if(x.equals("Y")){
            SEM.push("*");
            String semop = SEM.pop();//弹出SEM栈顶的操作符
            String semw1 = SEM.pop();//弹出栈顶的第二个操作数
            String semw2 = SEM.pop();//弹出栈顶第一个操作数
            String temp = printFour(semw2,semw1,semop);//产生临时结果并打印四元式
            SEM.push(temp);//将临时结果压栈
        }
        if(x.equals("Z")){
            SEM.push("/");
            String semop = SEM.pop();//弹出SEM栈顶的操作符
            String semw1 = SEM.pop();//弹出栈顶的第二个操作数
            String semw2 = SEM.pop();//弹出栈顶第一个操作数
            String temp = printFour(semw2,semw1,semop);//产生临时结果并打印四元式
            SEM.push(temp);//将临时结果压栈
        }
        //以上执行所有的GEQ（）动作
    }
    public static void doSomething(String x,String w){//处理栈顶为非动作符号时的操作
        System.out.println("接收到的x和w为："+x+" "+w);
        String[] checkTable = check(x,w).split("");//根据当前的x和w查找分析表
        System.out.println("查表得到的信息为："+Arrays.asList(checkTable).toString());
        for(String s : checkTable){
            //若压栈的为终结符i，获取当前终结符w并压入栈中
            if(s.equals("i")){
                SYN.push(w);
                continue;
            }
            //若读取到的是PUSH操作，则在PW栈中同步添加到时间操作的元素
            if(s.equals("P")){
                PW.push(w);
                SYN.push(s);
                continue;
            }
            //若是空字符串，则不压栈
            if(s.equals("")){
                continue;
            }
            //否则直接压栈
            SYN.push(s);
        }
    }
    public static String check(String A,String a){//分析表的查询法则
        //E' = M,T' = H,Push(i) = P,GEQ(+)=G,GEQ(-)=X,GEQ(*)=Y,GEQ(/)=Z
        if(Li.contains(a)){//若读取的a是字母，则设置为i用于查表
            a = "i";
        }
        if(A.equals("E")){
            if(a.equals("i")){
                return table[0][0];
            }
            if(a.equals("(")){
                return table[0][5];
            }
        }
        if(A.equals("M")){
            if(a.equals("+")){
                return table[1][1];
            }
            if(a.equals("-")){
                return table[1][2];
            }
            if(a.equals(")")){
                return table[1][6];
            }
            if(a.equals("#")){
                return table[1][7];
            }
        }
        if(A.equals("T")){
            if(a.equals("i")){
                return table[2][0];
            }
            if(a.equals("(")){
                return table[2][5];
            }
        }
        if(A.equals("H")){
            if(a.equals("+")){
                return table[3][1];
            }
            if(a.equals("-")){
                return table[3][2];
            }
            if(a.equals("*")){
                return table[3][3];
            }
            if(a.equals("/")){
                return table[3][4];
            }
            if(a.equals(")")){
                return table[3][6];
            }
            if(a.equals("#")){
                return table[3][7];
            }
        }
        if(A.equals("F")){
            if(a.equals("i")){
                return table[4][0];
            }
            if(a.equals("(")){
                return table[4][5];
            }
        }
        return null;
    }
    public static String printFour(String a,String b,String op){//产生四元式并返回一个临时结果
        String tempResult = "t"+tnum;
        tnum += 1;
        String siyuanshi = "("+op+" "+a+" "+b+" "+tempResult+")";
        QT.add(siyuanshi);//向QT列表添加生成的四元式
        System.out.println("输出四元式："+siyuanshi);
        return tempResult;//返回临时结果用于压栈
    }
}

