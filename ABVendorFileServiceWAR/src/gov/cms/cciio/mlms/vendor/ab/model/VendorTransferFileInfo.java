/**
 * 
 */
package gov.cms.cciio.mlms.vendor.ab.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xnieibm
 *
 */
public class VendorTransferFileInfo {
	
	private String fileName;
	private String vendorName;
	private String fileID;
	private String fileTime; //format: HH:mm:ss
	private String fileDate; //yyyy/MM/dd
	private String fileRecordCount;
	private List<String> records;
	
	public VendorTransferFileInfo(){
		records = new ArrayList<String>();
	}

	
	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getFileID() {
		return fileID;
	}
	public void setFileID(String fileID) {
		this.fileID = fileID;
	}
	public String getFileTime() {
		return fileTime;
	}
	public void setFileTime(String fileTime) {
		this.fileTime = fileTime;
	}
	public String getFileRecordCount() {
		return fileRecordCount;
	}
	public void setFileRecordCount(String fileRecordCount) {
		this.fileRecordCount = fileRecordCount;
	}
	public List<String> getRecords() {
		return records;
	}
//	public void setRecords(List<String> records) {
//		this.records = records;
//	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

}
