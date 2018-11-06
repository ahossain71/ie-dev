package gov.cms.cciio.mlms.vendor.ab.file;

import gov.cms.cciio.mlms.vendor.ab.DAL.VendorModelDAL;
import gov.cms.cciio.mlms.vendor.ab.file.exception.ABVendorFileProcessException;
import gov.cms.cciio.mlms.vendor.ab.model.VendorTransferFileInfo;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.xml.sax.InputSource;

/**
 * Session Bean implementation class VendorFileTransferService
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VendorFileTransferService {

	public static final String VENDOR_FILE_DROP_DIR = "mlms.ab.vendor.file.dropzone";
	public static final String AB_VENDOR_FILE_DESTINATION_DIR = "mlms.ab.vendor.file.destination";
	public static final String AB_VENDOR_FILE_PROCESSING_DIR = "mlms.ab.vendor.file.processing";

	private Logger logger = Logger.getLogger("VendorFileTransferService.class");

	@Resource
	TimerService timerService;

	/**
	 * Default constructor.
	 */
	public VendorFileTransferService() {
		// TODO Auto-generated constructor stub
	}

	public void readFile() {
		String dropzone = System.getProperty(VENDOR_FILE_DROP_DIR);
		String destinationPath = System.getProperty(AB_VENDOR_FILE_DESTINATION_DIR);
		String processingPath = System.getProperty(AB_VENDOR_FILE_PROCESSING_DIR);

		logger.info("Configured Dropzone path: " + dropzone);
		logger.info("Configured destination path1: " + destinationPath);
		logger.info("Configured processing path1: " + processingPath);

		if (dropzone == null || "".equals(dropzone)) {
			dropzone = "/tmp/ABVendorFile/dropzone"; // c:\\temp\\dropzone";
		}
		if (processingPath == null || "".equals(processingPath)) {
			processingPath = "/tmp/ABVendorFile/processing"; // c:\\temp\\processing";
		}
		if (destinationPath == null || "".equals(destinationPath)) {
			destinationPath = "/tmp/ABVendorFile/destination"; // "c:\\temp\\destination";
		}

		logger.info("Actual Dropzone path: " + dropzone);
		logger.info("Actual destination path: " + destinationPath);
		logger.info("Actual processing path: " + processingPath);

		// if(errDirPath == null || "".equals(errDirPath))
		// errDirPath = "c:\\temp\\err";

		Path dropzoneDir = FileSystems.getDefault().getPath(dropzone);

		if (!Files.isReadable(dropzoneDir)) {
			logger.info("Dropzone: " + dropzoneDir.getFileName());
			audit("Vendon File reading failed.", "Vendor File Dropzone is not accessible.");
			logger.log(Level.SEVERE,"Vendor File reading failed. Vendor File Dropzone is not accessible.");
			return;
		}

		List<VendorTransferFileInfo> fileInfos = new ArrayList<VendorTransferFileInfo>();

		// move the file to the new directory separated by date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		Path processingDir = Paths.get(processingPath, sdf.format(new Date()));
		Path destinationDir = Paths.get(destinationPath, sdf.format(new Date()));

		// for unix system:
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
		FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
		// attr = null; // in Windows
		try {
			if (Files.notExists(processingDir)) {
				// Files.createDirectory(processingDir, attr); //for unix
				Files.createDirectory(processingDir);
			}
			if (Files.notExists(destinationDir)) {
				// Files.createDirectory(destinationDir, attr); //for unix
				Files.createDirectory(destinationDir);
			}

			// move files from drop zone to destination directory:

			DirectoryStream<Path> vendorFiles = Files.newDirectoryStream(dropzoneDir, "*.{xml,P,T,p,t}");
			for (Path vFile : vendorFiles) {
				Files.move(vFile, processingDir.resolve(vFile.getFileName()), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			}
			// read files from destination directory:

			DirectoryStream<Path> fileList = Files.newDirectoryStream(processingDir, "*.{xml,P,T,p,t}");

			logger.info("The file number readed from processing directory: " + fileList.toString());
			// ABVendorXmlFileValidator validator = new
			// ABVendorXmlFileValidator();
			ABVendorXmlFileParser fileParser = new ABVendorXmlFileParser();
			for (Path file : fileList) {
				logger.info("File readed from processing directory before processing: " + file.getFileName());
				Reader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));
				InputSource is = new InputSource(reader);
				is.setEncoding("UTF-8");
				VendorTransferFileInfo fileInfo = fileParser.parseDocument(is);
				fileInfo.setFileName(file.getFileName().toString());
				fileInfos.add(fileInfo);
				reader.close();

				Files.move(file, destinationDir.resolve(file.getFileName()), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

			}
			fileList.close();
			vendorFiles.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE,"IOException: " + e.getMessage());
			e.printStackTrace();

		}

		// Pass fileInfos and Call persistence function

		// for unit test:
		for (VendorTransferFileInfo vfile : fileInfos) {
			logger.info("================================");
			logger.info(vfile.getFileName() + " has been processed!");
			VendorModelDAL dao = new VendorModelDAL(vfile);
			try {
				dao.processFile();
				logger.info("========= File Info ============");
				logger.info("Vendor Name: " + vfile.getVendorName());
				logger.info("Vendor FileId: " + vfile.getFileID());
				logger.info("Vendor FileTime: " + vfile.getFileTime());
				logger.info("Vendor FileDate: " + vfile.getFileDate());
				logger.info("Vendor File RecordCount: " + vfile.getFileRecordCount());

				List<String> records = vfile.getRecords();
				for (String record : records) {
					logger.info("Vendor Record: " + record);
				}
				logger.info("================================");
			} catch (ABVendorFileProcessException abe) {
				logger.log(Level.SEVERE,"File persistance process failed! " + abe.getMessage());
				abe.printStackTrace();
			}
		}

	}

	@Schedule(dayOfWeek = "Mon-Sun", hour = "0-23", minute = "*/5", second = "0", persistent = false)
	public void processFiles() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

		this.readFile();

		logger.info("Files have been processed at " + dateFormat.format(new Date()));

	}

	// @Timeout
	public void processFiles(Timer timer) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

		this.readFile();

		logger.info("Files have been processed at " + dateFormat.format(new Date()));

	}

	public void configureSchedule(String sec, String min, String hour, String weekDay) {
		// String sec = "10";
		// String min = "*/5";
		// String hour = "10-12";

		ScheduleExpression schedule = new ScheduleExpression().dayOfWeek(weekDay).second(sec).minute(min).hour(hour);
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setPersistent(false);
		timerService.createCalendarTimer(schedule, timerConfig);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void audit(String status, String message) {

	}

}
