package edu.buffalo.gsda;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Timer;
import java.util.Calendar;
import java.util.Date;
/**
 * Application Lifecycle Listener implementation class UpdateResultListener
 *
 */
public class UpdateResultListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
	private Timer timer = null; 
	
    public UpdateResultListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce)  { 
         // TODO Auto-generated method stub
    	timer.cancel();
    	sce.getServletContext().log("destroy the timer");
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce)  
    { 
        // TODO Auto-generated method stub
    	// set up the first updating time -- one  minute after starting up on the server
    	Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE)+1;
		
		if(minute == 00)
		{
			hour = hour+1;
		}
		
		// set up the time of initiating the ServletContextListener of automatic updating
		calendar.set(year, month, day, hour, minute, 40);
		Date defaultdate = calendar.getTime();
		Date updateDate = defaultdate;
		
		// if server starts after the default time
		if (defaultdate.before(new Date())) 
		{
			// set first updating time to the same time on tomorrow 
			calendar.add(Calendar.DATE, 1);
			updateDate = calendar.getTime();
		}
		
		/*---- Reactivating the updating task everyday ----*/	
    	timer = new Timer(true);    
    	UpdateResultTask MoanthlyTask = new UpdateResultTask();
        timer.schedule(MoanthlyTask,updateDate,24*3600*1000); 
    }
	
}
