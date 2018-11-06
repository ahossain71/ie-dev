package gov.mlms.cciio.cms.fileProcess.ejb.schedule;

import gov.mlms.cciio.cms.mlmsshopincrementalfiletype.FileDescriptionType;
import gov.mlms.cciio.cms.model.MLMSShopXMLModel;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

@Singleton
@Startup
@LocalBean
public class ShopXMLFileProcess {

	@Resource
	TimerService timerService;

	Logger logger = Logger.getLogger("ShopXMLFileProcess");

	Timer timer;

	boolean isDebug = System.getProperty("mlms.debug", "true")
			.equalsIgnoreCase("true");
	/**
	 * Must have public constructor
	 */
	public ShopXMLFileProcess() {

	}
	
	@PostConstruct
	void createTimer() {
		String methodName = "createTimer";
		String minute = System.getProperty("shop.schedule.fileextract.minute",
				"*/5");
		String hour = System.getProperty("shop.schedule.fileextract.hour", "*");
		String day = System.getProperty("shop.schedule.fileextract.dayOfWeek",
				"*");

		timer = this.createScheduledTimer(day, hour, minute);

		System.out.println(ShopXMLFileProcess.class.getSimpleName() + " " + methodName + " PostContruct Executed Created Timer ");
		logger.logp(Level.INFO, ShopXMLFileProcess.class.getSimpleName(), methodName, " " + timer.getNextTimeout().getTime(), " PostContruct Executed  CreatedTimer ");
		logger.logp(Level.INFO, ShopXMLFileProcess.class.getSimpleName(), methodName, " " + timer.getNextTimeout().getTime(), " minute "+minute);
		logger.logp(Level.INFO, ShopXMLFileProcess.class.getSimpleName(), methodName, " " + timer.getNextTimeout().getTime(), " hour "+hour);
		logger.logp(Level.INFO, ShopXMLFileProcess.class.getSimpleName(), methodName, " " + timer.getNextTimeout().getTime(), " date "+day);
		this.printTimerInfo(timer);
	}

	public void writeFile() {
		String methodName = "writeFile";
		MLMSShopXMLModel shopXML = new MLMSShopXMLModel();
		try {
			FileDescriptionType fileDescTy = shopXML.getShopXMLFile();
			/*
			 * /EFT/commonOutApp/MLM File Name format:
			 * ABINF1.MLM.D062116.T053112290.T
			 */
	/**		StringBuilder fileName = new StringBuilder("ABINF1.MLM.D")
					.append(new SimpleDateFormat("ddMMyy").format(new Date()))
					.append(".T")
					.append(new SimpleDateFormat("HHmmssSSS")
							.format(new Timestamp(System.currentTimeMillis())))
					.append(".T");*/
			/**String path = System.getProperty("shop.eft.file.path",
					"/opt/app/WebSphere/EFT/") + fileName.toString(); */
			String path = System.getProperty("shop.eft.file.path","/EFT/commonOutApp/MLM/MLM.ABINF1.D.T.T");

			String sDate = new SimpleDateFormat("yyMMdd").format(new Date());
			String sTime = new SimpleDateFormat("HHmmssSSS").format(new Timestamp(System.currentTimeMillis()));
			path = path.replace(".D.",".D" + sDate + ".");
			path = path.replace(".T.",".T" + sTime + ".");
			
			logger.log(Level.ALL, path);
			File file = new File(path);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(FileDescriptionType.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(fileDescTy, file);
			jaxbMarshaller.marshal(fileDescTy, System.out);

		} catch (Exception e) {
			logger.severe("Failed writing into the file: " + e.toString());
		}

	}

	/**
	 * @Schedule annotation creates a declarative timer, can be implemented only
	 *           in a singleton bean, stateless session bean or message-driven
	 *           bean. With no attributes defined will fire at midnight every
	 *           day default values for second, minute, hour = 0 default values
	 *           for dayOfWeek, dayOfMonth, month, year = *
	 *
	// @Schedule(dayOfWeek = "*", hour = "*", minute = "*", persistent = false)
	public void processFiles() {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yyyy hh:mm:ss");

		this.writeFile();

		logger.info("Files have been processed at "
				+ dateFormat.format(new Date()));

	}*/

	@Timeout
	public void processFiles(Timer timer) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yyyy hh:mm:ss");

		this.writeFile();

		logger.info("Files have been processed at "
				+ dateFormat.format(new Date()));

	}

	
	/**@author sjmeyer
	 */
	private void printTimerInfo(Timer timer) {
		String methodName = "printTimerInfo";
		// List<Timer> timerList = (List<Timer>)timerService.getTimers();

		// System.out.println(" there are " + timerList.size() + " timers ");
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss:SS");

		System.out.println(ShopXMLFileProcess.class.getSimpleName() + " "
				+ methodName + "\n\t schedule day "
				+ timer.getSchedule().getDayOfWeek() + " schedule hour "
				+ timer.getSchedule().getHour() + " schedule minute "
				+ timer.getSchedule().getMinute());

		System.out.println(" Timer is persistent " + timer.isPersistent());
		System.out.println(" Next scheduled run "
				+ formatter.format(timer.getNextTimeout()));
	}

	

	

	/**
	 * create a timer requires timer config value for day may be * (all), "Mon",
	 * "Mon,Tue,Fri", Mon-Fri will set a default schedule to run every day,
	 * every hour on the hour (1:00, 2:00,3:00,....)
	 * 
	 * @param day
	 * @param hour
	 * @param minute
	 * @return
	 */
	public Timer createScheduledTimer(String day, String hour, String minute) {
		String methodName = "createScheduledTimer";
		if (day == null)
			day = "*";
		day = (day == null) ? "*" : day;
		// if (hour == null)
		// hour = "*";
		hour = (hour == null) ? "*" : hour;
		// if (minute == null)
		// minute = "0";
		minute = (minute == null) ? "0" : minute;

		ScheduleExpression schedule = null;
		TimerConfig timerConfig = null;
		/**
		 * get the timer in use
		 */

		/*
		 * if there is not a timer create one
		 */
		if (timer == null) {
			System.out.println(ShopXMLFileProcess.class.getSimpleName() + " "
					+ methodName + " 5.27 3:09 timer is null, creating");

			schedule = new ScheduleExpression();
			schedule.dayOfWeek(day);
			schedule.hour(hour);
			schedule.minute(minute);

			timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);

			timer = timerService.createCalendarTimer(schedule, timerConfig);

		} else {
			/* clear timer and create new one */
			TimerHandle timerHandle = timer.getHandle();
			timer = timerHandle.getTimer();
			System.out.println("Timer exists, canceling");
			timer.cancel();

			schedule = new ScheduleExpression();
			schedule.dayOfWeek(day);
			schedule.hour(hour);
			schedule.minute(minute);

			timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);

			timer = timerService.createCalendarTimer(schedule, timerConfig);
		}

		return timer;

	}

}
