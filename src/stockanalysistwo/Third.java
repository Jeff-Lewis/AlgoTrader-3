package stockanalysistwo;
/**
 *
 * @author garth
 */
import java.io.*;
import java.util.StringTokenizer;

public class Third {
    
      public static void main(String[] args)throws IOException 
    {        
        String fileName="/home/garth/Desktop/stock/stock_post_2000/GE.csv";
        System.out.println(fileName);
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
                            lowPrices[i]= Double.parseDouble(stringArray[3]);           //initialise array of daily lows
                            highPrices[i]= Double.parseDouble(stringArray[2]);          //initialise array of daily highs
                        }
                        System.out.println("-------------------------------------------------------------------");     //reads in data
                        double[] supportPoints = new double[lineNumber];
                        supportPoints= getSupportPoints(lineNumber,lowPrices);
                        
                     //get supportCurve
                        double[][] supportInput =new double[2][supportPoints.length];
                        for(int i=0;i<lineNumber;i++)
                        {
                            supportInput[0][i]=(double)i;
                            supportInput[1][i]=supportPoints[i];
                        }                                                            //different format for inpt to linear regression
                        double[] supportCurve = doLinearRegression(supportInput);
                     //got support curve
                        double[] resistPoints = new double[lineNumber];
                        resistPoints=getResistPoints(lineNumber,highPrices);
                     //get resistCurve
                        double[][] resistInput =new double[2][resistPoints.length];
                       
                        for(int i=0;i<lineNumber;i++)
                        {
                            resistInput[0][i]=(double)i;
                            resistInput[1][i]=resistPoints[i];
                        }
                        double[] resistCurve = doLinearRegression(resistInput);
                     //got resistCurve

                        int bestPeriod=0;
                        double bestR2=0;
                        double bestReturn=0;
                        //for(int x=20;x<50;x++){           //find good period
                        //for(int x=0;x<100;x++){          
                        double R2=0;                 //find good R2
                        
                        int period =30;
                        double money = 100;
                        double shares = 0;
                        int NumTransactions=0;
                        double total=0;
                        double currentValue=0;
                        for(int i=0;i<lineNumber-period;i++){
                            double[] tempResist = doLinearRegressionFromTo(i,i+period,resistInput);
                            double[] tempSupport = doLinearRegressionFromTo(i,i+period,supportInput);
                            double currentSValue = getValueFromCurve(i+period,tempSupport);
                            double currentRValue = getValueFromCurve(i+period,tempResist);
                            currentValue = (highPrices[i+period-2]+lowPrices[i+period]-2)/2;
                            double minSRDif = currentValue*0.04;
                            double currentSRDif = currentRValue-currentSValue ;
                            if(tempResist[2]>R2 && tempSupport[2]>R2 && currentSRDif>minSRDif){
                                
                                if(currentValue<currentSValue){
                                    System.out.println("day number "+(i+period)+" :");
                                    System.out.println("buy");
                                    if(money>=10){
                                        money = money - 10 ;
                                        shares=shares + 10/currentValue;
                                        System.out.println("money = "+money);
                                        System.out.println("val of shares = "+shares*currentValue);
                                        System.out.println("total = "+(money+shares*currentValue));
                                        System.out.println("______________________________________");
                                        NumTransactions++;
                                    }
                                }
                                if(currentValue>currentRValue){
                                    System.out.println("day number "+(i+period)+" :");
                                    System.out.println("sell");
                                        money = money + shares*currentValue/2 ;
                                        shares=shares/2 ;                                           //sell half of shares
                                        System.out.println("money = "+money);
                                        System.out.println("val of shares = "+shares*currentValue);
                                        System.out.println("total = "+(money+shares*currentValue));
                                        System.out.println("______________________________________");
                                        NumTransactions++;
                                }
                            }
                        

                        }
                        total=money+(shares*currentValue);
                        System.out.println("Final money = "+total);
                        int years=lineNumber/260;
                        System.out.println("years: "+years);
                        System.out.println("number of transactions = "+NumTransactions);
                        double annualReturn=(1-Math.pow((total/100),(1/(double)years)))*(-100);
                        System.out.println("average annual return = "+annualReturn+"%");
                        
                        
                       // if(annualReturn>bestReturn){bestPeriod=x;bestReturn=annualReturn;}
                       //}
                       //if(annualReturn>bestReturn){bestR2=R2;bestReturn=annualReturn;}
                       //}
                        //System.out.println("best period ="+bestPeriod);
                        //System.out.println("best R2 ="+bestR2);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
      
      
      //methods
      
      
      public static double[] doLinearRegression(double[][] args)                //input double[0]=array of day number
      {                                                                         //input double[1]=array of prices
          double[] answer = new double[3];
          int n = 0;
          double[] x = new double[args[0].length];
          double[] y = new double[args[1].length];
          for(int i=0;i<args[0].length;i++){x[i]=args[0][i];}
          for(int i=0;i<args[0].length;i++){y[i]=args[1][i];}
          double sumx = 0.0, sumy = 0.0;
          for(int i=0;i<args[0].length;i++) {
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
        //System.out.println("y   = " + beta1 + " * x + " + beta0);
        double ssr = 0.0;
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        //System.out.println("R^2          = " + R2);
        answer[0]=beta1;                                                        //returns m(gradient)
        answer[1]=beta0;                                                        //returns c(y-intercept)
        answer[2]=R2;                                                           //returns R-squared
          return answer;
      }
      
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
        //System.out.println("y   = " + beta1 + " * x + " + beta0);
        double ssr = 0.0;
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        //System.out.println("R^2          = " + R2);
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

    private static double[] getSupportPoints(int lineNumber, double[] lowPrices) {
        double[] sPoints = new double[lineNumber];
        for(int i =0;i<lineNumber-1;i++){
             double price = 999999999;
             for(int j=-5;j<=0;j++){
                 if((i+j>=0) && (i+j<lineNumber-1) && lowPrices[i+j]<price){  
                     price =lowPrices[i+j];
                     sPoints[i]=price;
                 }
              }
        }
        return sPoints;
    }
    
    private static double[] getResistPoints(int lineNumber, double[] highPrices) {
        double[] rPoints = new double[lineNumber];
        for(int i =0;i<lineNumber-1;i++){
            double price = 0;
            for(int j=-5;j<=0;j++){
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
    
    private static boolean isBelowSup(int day, double[] supportCurve, double currentVal) {
        double currentSupVal= getValueFromCurve(day,supportCurve) ;
        boolean answer= (currentVal<currentSupVal);
        return answer;
    }
    private static boolean isAboveRes(int day, double[] resistCurve, double currentVal) {
        double currentSupVal= getValueFromCurve(day,resistCurve) ;
        boolean answer= (currentVal>currentSupVal);
        return answer;
    }
}
