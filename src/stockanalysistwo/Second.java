package stockanalysistwo;

import java.io.*;
import java.util.StringTokenizer;
/**
 *
 * @author garth
 */
public class Second {
    
      public static void main(String[] args)throws IOException 
    {        
        String fileName="/home/garth/Desktop/stock/GE.csv";
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
                            lowPrices[i]= Double.parseDouble(stringArray[3]);           //array of daily lows
                            highPrices[i]= Double.parseDouble(stringArray[2]);          //array of daily highs
                        }
                        System.out.println("-------------------------------------------------------------------");     //reads in data
                        double[] supportPoints = new double[lineNumber];
                        supportPoints= getSupportPoints(lineNumber,lowPrices);
                        double[][] supportInput =new double[2][supportPoints.length];
                        for(int i=0;i<lineNumber;i++)
                        {
                            supportInput[0][i]=(double)i;
                            supportInput[1][i]=supportPoints[i];
                        }
                        double[] supportCurve = doLinearRegression(supportInput);
                        System.out.println("supportpoints:");
                        System.out.println("y="+supportCurve[0]+"x + "+supportCurve[1]+"   R^2= "+supportCurve[2]);
                        System.out.println("-------------------------------------------------------------------");    //gets support points and curve
                        double[] resistPoints = new double[lineNumber];
                        resistPoints=getResistPoints(lineNumber,highPrices);
                        double[][] resistInput =new double[2][resistPoints.length];
                       
                        for(int i=0;i<lineNumber;i++)
                        {
                            resistInput[0][i]=(double)i;
                            resistInput[1][i]=resistPoints[i];
                        }
                        double[] resistCurve = doLinearRegression(resistInput);
                        System.out.println("resistpoints:");
                        System.out.println("y="+resistCurve[0]+"x + "+resistCurve[1]+"   R^2= "+resistCurve[2]);
                        System.out.println("-------------------------------------------------------------------");     //gets resist points and curve
                        
                        int day=700;
                        double supportValue=getValueFromCurve(day,supportCurve);
                        double resistValue=getValueFromCurve(day,resistCurve);
                        System.out.println("Support Value="+supportValue);
                        System.out.println("Resist Value="+resistValue);
                        double mean= (supportValue+resistValue)/2;
                        double diff= (resistValue-supportValue);
                        double bidOfferPercentage= (diff/mean)*100;
                        System.out.println("mean="+mean);
                        System.out.println("difference="+diff);
                        System.out.println("The difference between the support and resistance at the current time as a percentage of the mean value is "+bidOfferPercentage+"%");
                        System.out.println("-------------------------------------------------------------------");

                        int period =50;
                        double SupportR2=0;
                        double ResistR2=0;
                        int Rday = 0;
                        int Sday=0;
                        int bestDay=0;
                        double averageR2=0;
                        double difference=0;
                        double perc=0;
                        for(int i=0; i<(lineNumber-period)-1;i++)
                        {   
                            double[] tempSupport = doLinearRegressionFromTo(i,i+period-2,supportInput);
                            double[] tempResist = doLinearRegressionFromTo(i,i+period-2,resistInput);
                            double currentResistR2=tempResist[2];
                            double currentSupportR2=tempSupport[2];
                            
                            if(currentResistR2>ResistR2){
                                ResistR2=currentResistR2;
                                Rday=i;
                            }
                            if(currentSupportR2>SupportR2){
                                SupportR2=currentSupportR2;
                                Sday=i;
                            }
                            if((currentSupportR2+currentResistR2)/2>averageR2){
                                averageR2=(currentSupportR2+currentResistR2)/2;
                                bestDay=i;
                                double currentSupportValue=getValueFromCurve(i+period,tempSupport);
                                double currentResistValue=getValueFromCurve(i+period,tempResist);
                                difference = currentResistValue-currentSupportValue;
                                perc= (currentResistValue+currentSupportValue)/2;
                            }
                        }
                        System.out.println("Best RR2 = "+ResistR2+" on the "+Rday+" day.");
                        System.out.println("Best SR2= "+SupportR2+" on the "+ Sday+" day.");
                        System.out.println("Best average= "+averageR2+" on the "+ bestDay+" day. The difference in values 50 days after this day was "+difference);
                        System.out.println("as a % this difference is "+perc);
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
