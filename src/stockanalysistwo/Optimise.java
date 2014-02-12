package stockanalysistwo;
/**
 *
 * @author garth
 */
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimise {
    
      public static void main(String[] args)throws IOException 
    {        
        double finalAnswer=-99999;
        double finalcurvePoints=0;
        double finalPeriod=0;
        double finalPercDif=0;
        double finalminSRDif=0;
        double finalTP=0;
        double[] PortfolioValue=new double[60*260];
        double[] BaseValue=new double[60*260];
        
        for(int curvePoints =6;curvePoints<=6;curvePoints++){                   //4
            for(int period=2;period<=2;period++){                               //2
                for(int difPercenti=34;difPercenti<=34;difPercenti++){          //28
                    for(int minSRDif=37;minSRDif<=37;minSRDif++){               //34
                        for(int TPi=14;TPi<=14;TPi++){                          //32
                            
                            double difPercent=(double)difPercenti/100;
                            double minSRDifin=(double)minSRDif/1000;
                            double TP=(double)TPi/1000;
                            double answer=OptimiseParameters(minSRDifin,curvePoints,period,difPercent, TP,PortfolioValue,BaseValue);
                            System.err.println(answer);
                            if(finalAnswer<answer){
                                finalminSRDif=minSRDifin;
                                finalAnswer=answer;
                                finalcurvePoints=curvePoints;
                                finalPeriod=period;
                                finalPercDif=difPercent;
                                finalTP=TP;
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("The best parameters are : ");
        System.out.println("period : "+finalPeriod);
        System.out.println("curvePoints : "+finalcurvePoints);
        System.out.println("PercentDifferenceNeeded : "+finalPercDif);
        System.out.println("finalminSRDif : "+finalminSRDif);
        System.out.println("TP : "+finalTP);
        System.out.println("The best effective annual return for these parameters is : "+finalAnswer);
        PrintWriter out = new PrintWriter(new FileWriter("PortfolioValue.csv"));
        for(int i=0;i<PortfolioValue.length;i++){
            out.println(PortfolioValue[i]/40+","+BaseValue[i]/40+","+(PortfolioValue[i]-BaseValue[i])/40);        //output to file
        }
        
    }
      
      
      //methods________________________________________________________________________________________

      
      public static double[] doLinearRegressionFromTo(int start,int end,double[][] args)                //input int start, int end
      {                                                                                                 //input double[1]=array of prices
          double[] answer = new double[3];
          int n = 0;
          int numDays=end-start;
          double[] x = new double[numDays];
          double[] y = new double[numDays];
          for(int i=0;i<numDays;i++){x[i]=args[0][i+start];}
          for(int i=0;i<numDays;i++){y[i]=args[1][i+start];}
          double sumx = 0.0, sumy = 0.0;
          for(int i=0;i<x.length;i++) {
                sumx  += x[i];
                sumy  += y[i];
                n++;
            }
        double xbar = sumx / n;
        double ybar = sumy / n;
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;
        double ssr = 0.0;
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        answer[0]=beta1;                                                        //returns m(gradient)
        answer[1]=beta0;                                                        //returns c(y-intercept)
        answer[2]=R2;                                                           //returns R-squared
          return answer;
      }
      
      public static int findNumDays(String fileName)
      {
          int lineNumber = 0;
          String line;
          try {
			BufferedReader br = new BufferedReader( new FileReader(fileName));
			while( (line = br.readLine()) != null)
			{
                                lineNumber++;                                   //counts number of entries in file 
			}
          }
          catch (FileNotFoundException e) 
          {
              e.printStackTrace();
          }
          catch (IOException e)
          {
              e.printStackTrace();
	  }
          return lineNumber;
      }

    private static double[] getSupportPoints(int curvePoints,int lineNumber, double[] lowPrices) {
        double[] sPoints = new double[lineNumber];
        for(int i =0;i<lineNumber-1;i++){
             double price = 999999999;
             for(int j=-(curvePoints);j<=0;j++){
                 if((i+j>=0) && (i+j<lineNumber-1) && lowPrices[i+j]<price){  
                     price =lowPrices[i+j];
                     sPoints[i]=price;
                 }
              }
        }
        return sPoints;
    }
    
    private static double[] getResistPoints(int curvePoints,int lineNumber, double[] highPrices) {
        double[] rPoints = new double[lineNumber];
        for(int i =0;i<lineNumber-1;i++){
            double price = 0;
            for(int j=-curvePoints;j<=0;j++){
                if((i+j>=0) && (i+j<lineNumber-1) && highPrices[i+j]>price){
                    price =highPrices[i+j];
                    rPoints[i]=price;
                }
            }
        }
        return rPoints;
    }
    private static double getValueFromCurve(int day, double[] supportCurve) {
        double answer= day*supportCurve[0]+supportCurve[1];
        return answer;
    }
    
    //special method :p
    public static double OptimiseParameters(double minSRDifin,int curvePointsin,int periodin,double difPercentin , double TP, double[] PortfolioValue, double[] BaseValue) 
    {        
        File directory = new File("/home/garth/Desktop/stock_post_2000");                            //directory of program files
        File[] files = directory.listFiles(); 
        List<String> allMatches = new ArrayList<String>();
            for(int i=0;i<files.length;i++){
                Matcher m = Pattern.compile(".*csv.*").matcher(files[i].toString());       //finds all files that match the regex
                while (m.find())
                {
                    allMatches.add(m.group());
                }
            }
            File[] stockFiles = new File[allMatches.toArray().length];
            for(int i =0;i<allMatches.toArray().length;i++)
            {
                File temp=new File(allMatches.toArray()[i].toString());
                stockFiles[i]=temp;
            }                                                                              //create array of files for analysis
        
            
            
            double totalAnnualReturn=0;
            double annualReturn=0;
            double total=0;
            double finalTotal=0;
            int sumYears=0;
            int years =0;
            for(int f=0;f<allMatches.toArray().length;f++){
        String fileName=stockFiles[f].getAbsolutePath();
        System.out.println(fileName);
        int totalTransactions=0;
		try {
			int lineNumber = findNumDays(fileName);
                        String line = new String();
                        String[] stringArray =new String[7];
                        double[] lowPrices = new double[lineNumber];
                        double[] highPrices = new double[lineNumber];
                        BufferedReader br = new BufferedReader( new FileReader(fileName));
                        line = br.readLine();
                        for(int i =0;i<lineNumber-1;i++)
                        {
                            line = br.readLine();
                            stringArray = line.toString().split(",");
                            lowPrices[lineNumber-1-i]= Double.parseDouble(stringArray[3]);           //initialise array of daily lows
                            highPrices[lineNumber-1-i]= Double.parseDouble(stringArray[2]);          //initialise array of daily highs
                        }
//                        System.out.println("------------------------------------------------------------");     //reads in data
                        double[] supportPoints = new double[lineNumber];
                        supportPoints= getSupportPoints(curvePointsin,lineNumber,lowPrices);
                        
                     //get supportCurve
                        double[][] supportInput =new double[2][supportPoints.length];
                        for(int i=0;i<lineNumber;i++)
                        {
                            supportInput[0][i]=(double)i;
                            supportInput[1][i]=supportPoints[i];
                        }                                                            //different format for input to linear regression
                        double[] resistPoints = new double[lineNumber];
                        resistPoints=getResistPoints(curvePointsin, lineNumber,highPrices);
                        double[][] resistInput =new double[2][resistPoints.length];
                       
                        for(int i=0;i<lineNumber;i++)
                        {
                            resistInput[0][i]=(double)i;
                            resistInput[1][i]=resistPoints[i];
                        }
                     //done reading in data______________________________________________________
                        
                        

                        double R2=0;                 
                        int period =periodin;
                        double difPercent=difPercentin;
                                                      //algorithm parameters
                        
                        double commission = 0.02;      //cost of transaction as a percentage
                        double money = 100;
                        double shares = 0;
                        int NumTransactions=0;
                        double currentValue=0;
                        double BaseShares =100/lowPrices[1];
                        for(int i=0;i<lineNumber-period;i++){
                            double[] tempResist = doLinearRegressionFromTo(i,i+period,resistInput);
                            double[] tempSupport = doLinearRegressionFromTo(i,i+period,supportInput);
                            double currentSValue = getValueFromCurve(i+period,tempSupport);
                            double currentRValue = getValueFromCurve(i+period,tempResist);
                            currentValue = (highPrices[i+period-1]+lowPrices[i+period]-1)/2;
                            double minSRDif = currentValue*minSRDifin;
                            double currentSRDif = currentRValue-currentSValue ;
                            
                            double extraDif=(difPercent/100)*currentValue;
                            
                            if( currentSRDif>minSRDif){
                                
                                double TradePercent=TP;
                                if(currentValue<currentSValue-extraDif){
                                        money = money - money*TradePercent ;
                                        shares=shares + ((money*TradePercent)-(money*TradePercent*commission))/currentValue;
                                        NumTransactions++;
                                }
                                if(currentValue>currentRValue+extraDif){
                                        money = money + shares*TradePercent*currentValue -(shares*TradePercent*currentValue*commission);
                                        shares=shares-shares*TradePercent ;                                           
                                        NumTransactions++;
                                }
                            }
                            PortfolioValue[60*260-lineNumber+i-1]+=money+shares*currentValue;
                            BaseValue[60*260-lineNumber+i-1]+=BaseShares*currentValue;
                        }
                        total=money+(shares*currentValue);
                        System.out.println("Final money = "+total);
                        years=lineNumber/260;
                        System.out.println("years: "+years);
                        System.out.println("number of transactions for " +fileName+ " = "+NumTransactions);
                        annualReturn=(1-Math.pow((total/100),(1/(double)years)))*(-100);
              //          System.out.println("average annual return = "+annualReturn+"%");
                        totalTransactions = NumTransactions;
                      System.out.println("Transactions: "+totalTransactions);

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
                if(annualReturn<99999999){
                    totalAnnualReturn+=annualReturn;
                }
                
                finalTotal+=total;
                sumYears+=years;
//                System.out.println("=============================================================");
                }
            System.out.println("The total number of stocks analysed is "+files.length);
            System.out.println("Total made starting with R100 per stock is : R"+finalTotal);
            System.out.println("So the average return is "+finalTotal/files.length);
            System.out.println("Average annual return is : "+(totalAnnualReturn/files.length)+" %");
            System.out.println("Effective annual return is : "+(1-Math.pow(((finalTotal/files.length)/100),(1/((double)sumYears/files.length))))*(-100)+" %");
            double EffectiveAnnualReturn=(1-Math.pow(((finalTotal/files.length)/100),(1/((double)sumYears/files.length))))*(-100);
            System.out.println("Returning:"+EffectiveAnnualReturn);
            System.out.println("if evaluates to "+(EffectiveAnnualReturn<99999999));
            
            if(EffectiveAnnualReturn<99999999){
                return EffectiveAnnualReturn;
            }
            else {
                return 0;	}
    }
}
