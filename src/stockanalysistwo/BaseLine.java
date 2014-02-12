package stockanalysistwo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author garth
 */
public class BaseLine {
    public static void main(String[] args)throws IOException 
    {      
    File directory = new File("/home/garth/Desktop/stock_pre_2000");                            //directory of stock price files
        File[] files = directory.listFiles();
        List<String> allMatches = new ArrayList<String>();
            for(int i=0;i<files.length;i++){
                Matcher m = Pattern.compile(".*csv.*").matcher(files[i].toString());             //finds all files that match the regex
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
             String line;
             String[] stringArray =new String[7];
             double TotalLow= 0;
             double TotalHigh= 0;
             double averageRatio=0;
             double TotalNumYear=0;
             double AverageYears =0;
             for(int f=0;f<allMatches.toArray().length;f++){
                 
                String fileName=stockFiles[f].getAbsolutePath();
                BufferedReader br = new BufferedReader( new FileReader(fileName));
                line = br.readLine();
                int lineNumber = findNumDays(fileName);
                System.out.println("lineNumber"+lineNumber);
                double[] Prices = new double[lineNumber]; 
                
                        for(int i =0;i<lineNumber-1;i++)
                        {
                            line = br.readLine();
                            stringArray = line.toString().split(",");
                            Prices[i]=Double.parseDouble(stringArray[2]);
                        }
                        System.out.println(fileName);
                        double low=Prices[1];
                        System.out.println("end  :"+low);
                        double high=Prices[lineNumber-2];
                        System.out.println("start  :"+high);
                        double currentReturn=(high/low -1)*100;
                        double numYear=(double)lineNumber/260;
                        TotalNumYear+=numYear;
                        TotalLow += low;
                        TotalHigh+=high;
                        double ratio=low/high;
                        System.out.println("ratio  "  +ratio);
                        AverageYears+=lineNumber/260;
                        averageRatio+=ratio;
             }
             System.out.println("_____________________");
             System.out.println("aveyears "+AverageYears/(double)allMatches.toArray().length);
             double aveTotalNumYear=TotalNumYear/allMatches.toArray().length;
             double PercReturn= (1-Math.pow((TotalLow/TotalHigh),(1/(aveTotalNumYear))))*(-100);
             System.out.println("PercReturn = "+PercReturn+"%");
             averageRatio=averageRatio/allMatches.toArray().length;
             double averageLow=TotalLow/allMatches.toArray().length;
             double averageHigh=TotalHigh/allMatches.toArray().length;
             System.out.println("end = "+averageLow);
             System.out.println("start = "+averageHigh);
             double TotalReturn=(averageLow/averageHigh);
             System.out.println("TotalReturn="+TotalReturn);
    }
    
    
    //methods
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