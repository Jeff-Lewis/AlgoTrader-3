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
public class ContOpt extends ApplicationFrame {
    
    public ContOpt(XYSeriesCollection dataset,final String title) {

        super(title);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
        setContentPane(chartPanel);

    }
    
     public static void main(String[] args)throws IOException 
    {        
        File directory = new File("/home/garth/Desktop/stock");                            //directory of program files
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
            }   
            
            for(int f=0;f<allMatches.toArray().length;f++){
                double money=100;
                double shares=0;
                double sharePrice=0;
                String fileName=stockFiles[f].getAbsolutePath();
                double finalAnswer=-99;
                final XYSeriesCollection dataset = new XYSeriesCollection();
                XYSeries portfolio = new XYSeries("Portfolio");
                XYSeries base = new XYSeries("Base");
                double[] bestParams= new double[2];
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
                double Base=100/Prices[100];
                for(int day=100;day<400;day++){
                    bestParams=getOptimalParams(day,dataset,fileName,lineNumber,Prices);
                    double[] Money=new double[3];
                    Money=takePosition(day+1,bestParams,fileName,lineNumber,Prices,money,shares);
                    money=Money[0];
                    shares=Money[1];
                    sharePrice=Money[2];
                    
                    //System.err.println("money"+money+"  shares"+shares);
                }
                Base=Base*Prices[400];
                System.out.println("Portfolio: "+(money+shares*sharePrice));
                System.out.println("base: "+Base);
                dataset.removeAllSeries();
                dataset.addSeries(portfolio);
                dataset.addSeries(base);
                //JFreeChart chart = createChart(dataset);
                //final ChartPanel chartPanel = new ChartPanel(chart);
                //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
                //final CalcWithJFreeChart demo = new CalcWithJFreeChart(dataset,"Line Chart Demo 6");
                //demo.pack();
                //RefineryUtilities.centerFrameOnScreen(demo);
                //demo.setVisible(true);

            }
        System.out.println("The best parameters are : ");
    }
      
      
      //methods________________________________________________________________________________________

    public static double[] getOptimalParams(int day,XYSeriesCollection dataset, String fileName, int lineNumber,double[] Prices) throws FileNotFoundException, IOException
    {
        double[] answer=new double[2];
        int testStartDay=day-20;
        double tempResult=0;

        for(int longPeriod=10;longPeriod<80;longPeriod++)
        {
            for(int shortPeriod=1;shortPeriod<longPeriod;shortPeriod++)
            {   
                double shares=0;
                double money=100;
                double Result=0;
                for(int testday=testStartDay;testday<day;testday++)
                {
                        double longAve =0;
                        double shortAve =0;
                        for(int i=testStartDay-longPeriod;i<testStartDay;i++){longAve=longAve+Prices[i];}
                        for(int i=testStartDay-shortPeriod;i<testStartDay;i++){shortAve=shortAve+Prices[i];}
                        longAve=longAve/longPeriod;
                        shortAve=shortAve/shortPeriod;
                        double currentValue=Prices[testday];
                        double dif=longAve-shortAve;
                        double currentDifNeeded=(0.5/100)*currentValue;
                        double TradePercent=0.2;
                        if( Math.abs(dif)<Math.abs(currentDifNeeded)){

                                if(dif>0){
                                        shares=shares + ((money*TradePercent)-(money*TradePercent*0.002))/currentValue;
                                        money = money - money*TradePercent ;
                                }
                                if(dif<0){
                                        money = money + shares*TradePercent*currentValue -(shares*TradePercent*currentValue*0.002);
                                        shares=shares-shares*TradePercent ;                                           
                                }
                            }
                }
                Result=money+shares*Prices[day];
                if(Result>tempResult)
                {
                    tempResult=Result;
                    answer[0]=shortPeriod;
                    answer[1]=longPeriod;
                }
            }
        }
       // System.out.println(answer[0]+"  "+answer[1]);
        return answer;
    }
    
    
    public static double[] takePosition(int day,double[] bestParams,String fileName,int lineNumber,double[] Prices,double money,double shares){
        
        
        double longAve =0;
        double shortAve =0;
        for(int i=day-(int)bestParams[0];i<day;i++){longAve=longAve+Prices[i];}
        for(int i=day-(int)bestParams[1];i<day;i++){shortAve=shortAve+Prices[i];}
        longAve=longAve/bestParams[0];
        shortAve=shortAve/bestParams[1];
        double currentValue=Prices[day];
        double dif=longAve-shortAve;
        double currentDifNeeded=(0.5/100)*Prices[day];
        double TradePercent=0.2;
        if( Math.abs(dif)<Math.abs(currentDifNeeded)){
            if(dif>0){
               shares=shares + ((money*TradePercent)-(money*TradePercent*0.002))/Prices[day];
               money = money - money*TradePercent ;
            }
            if(dif<0){
               money = money + shares*TradePercent*Prices[day] -(shares*TradePercent*Prices[day]*0.002);
               shares=shares-shares*TradePercent ;                                           
            }
        }
        double[] answer=new double[3];
        answer[0]=money;
        answer[1]=shares;
        answer[2]=Prices[day];
        return answer;
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
        renderer.setSeriesShapesVisible(1, false);
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