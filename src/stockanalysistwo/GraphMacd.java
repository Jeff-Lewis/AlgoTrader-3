package stockanalysistwo;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
/**
 *
 * @author garth
 */
public class GraphMacd extends ApplicationFrame {
    
    public GraphMacd(XYSeriesCollection dataset,final String title) {

        super(title);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
        setContentPane(chartPanel);

    }
    
     public static void main(String[] args)throws IOException 
    {        
        double finalAnswer=-99;
        double finalShortPeriod=0;
        double finalLongPeriod=0;
        double finalPercDif=0;
        double[] PortfolioValue=new double[60*260];
        double[] BaseValue=new double[60*260];
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
        for(int longPeriod=95;longPeriod<=95;longPeriod++){  
            for(int shortPeriod =46;shortPeriod<=46;shortPeriod++){                                          
                for(int difPercent=98;difPercent<=98;difPercent++){    
                            double difpercentinNew=99.2;
                            double answer=OptimiseParameters(shortPeriod,longPeriod,difpercentinNew,PortfolioValue,BaseValue,dataset);
                            System.err.println(answer);
                            if(finalAnswer<answer){
                                
                                finalAnswer=answer;
                                finalShortPeriod=shortPeriod;
                                finalLongPeriod=longPeriod;
                                finalPercDif=difPercent;
                            }
                        }
                    }
                }
        
        System.out.println("The best parameters are : ");
        System.out.println("ShortPeriod : "+finalShortPeriod);
        System.out.println("LongPeriod : "+finalLongPeriod);
        System.out.println("PercentDifferenceNeeded : "+finalPercDif);
        System.out.println("The best effective annual return for these parameters is : "+finalAnswer);
        
        

    }
      
      
      //methods________________________________________________________________________________________

    public static double OptimiseParameters(int shortPeriod,int longPeriod,double difPercentin, double[] PortfolioValue, double[] BaseValue,XYSeriesCollection dataset) 
    {        
        File directory = new File("/home/garth/Desktop/singleFile");                            //directory of program files
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
            
            XYSeries portfolio = new XYSeries("Portfolio");
            XYSeries base = new XYSeries("Base");
            for(int f=0;f<allMatches.toArray().length;f++){
        String fileName=stockFiles[f].getAbsolutePath();
        System.out.println(fileName);
        int totalTransactions=0;
		try {
			int lineNumber = findNumDays(fileName);
                        String line = new String();
                        String[] stringArray =new String[7];
                        double[] Prices = new double[lineNumber];
                        BufferedReader br = new BufferedReader( new FileReader(fileName));
                        line = br.readLine();
                        for(int i =0;i<lineNumber-1;i++)
                        {
                            line = br.readLine();
                            stringArray = line.toString().split(",");
                            Prices[lineNumber-1-i]= Double.parseDouble(stringArray[3]);           //initialise array of daily lows
                        }
double[] DifArray= new double[60*260];
                        
                        double commission = 0.002;      //cost of transaction as a percentage
                        double money = 100;            //shares starting funds
                        double shares = 0;             //initial shares
                        int NumTransactions=0;
                        double currentValue=0;
                        double BaseShares =100/Prices[1];
                        for(int i=longPeriod;i<lineNumber;i++){
                            currentValue=Prices[i];
                            double BigAveValue = 0;
                            for(int j=i-longPeriod;j<i;j++){BigAveValue+=Prices[j];}
                            BigAveValue=BigAveValue/longPeriod;
                            
                            double smallAveValue = 0;
                            for(int j=i-shortPeriod;j<i;j++){smallAveValue+=Prices[j];}
                            smallAveValue=smallAveValue/shortPeriod;
 
                            double Dif=BigAveValue-smallAveValue;
                            double currentDifNeeded=(difPercentin/100)*currentValue;
                            //transactions:
                            double TradePercent=0.1;
                            DifArray[i]=BigAveValue-smallAveValue;

                           if( Math.abs(Dif)<Math.abs(currentDifNeeded)){

                                if(Dif>0 && DifArray[i]>DifArray[i-1]){
                                        shares=shares + ((money*TradePercent)-(money*TradePercent*commission))/currentValue;
                                        money = money - money*TradePercent ;
                                        NumTransactions++;
                                }
                                if(Dif<0 && DifArray[i]>DifArray[i-1]){
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

                }
            for(int i=0;i<60*260;i++){
                        portfolio.add(i,PortfolioValue[i]/files.length);
                        base.add(i,BaseValue[i]/files.length);
            }

            dataset.removeAllSeries();
                        dataset.addSeries(portfolio);
                        dataset.addSeries(base);
                        JFreeChart chart = createChart(dataset);
                final ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
                final CalcWithJFreeChart demo = new CalcWithJFreeChart(dataset,"Line Chart Demo 6");
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
        
            System.out.println("The total number of stocks analysed is "+files.length);
            System.out.println("Total made starting with R100 per stock is : R"+finalTotal);
            System.out.println("So the average return is "+finalTotal/files.length);
            System.out.println("Average annual return is : "+(totalAnnualReturn/files.length)+" %");
            System.out.println("Effective annual return is : "+(1-Math.pow(((finalTotal/files.length)/100),(1/((double)sumYears/files.length))))*(-100)+" %");
            double EffectiveAnnualReturn=(1-Math.pow(((finalTotal/files.length)/100),(1/((double)sumYears/files.length))))*(-100);
            System.out.println("Returning:"+EffectiveAnnualReturn);
            System.out.println("if evaluates to "+(EffectiveAnnualReturn<99999999));
            try{
                PrintWriter out = new PrintWriter(new FileWriter("PortfolioValue.csv"));
                for(int i=0;i<PortfolioValue.length;i++){
                    out.println(PortfolioValue[i]/files.length+","+BaseValue[i]/files.length+","+(PortfolioValue[i]-BaseValue[i])/files.length);        //output to file
                }
            }
            catch(Exception e){;}
            if(EffectiveAnnualReturn<99999999){
                return EffectiveAnnualReturn;
            }
            else {
                return 0;	
            }
    }

     static JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "Line Chart Demo 6",      // chart title
            "X",                      // x axis label
            "Y",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        chart.setBackgroundPaint(Color.white);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;   
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
}