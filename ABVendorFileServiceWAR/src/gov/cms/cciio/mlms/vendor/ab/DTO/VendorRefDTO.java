package gov.cms.cciio.mlms.vendor.ab.DTO;

public class VendorRefDTO {
	private String id;
	private String vendor_code;
	private String vendor_name;
	
	public String getId(){
		return id;
	}
	
	public String getVendorCode(){
		return vendor_code;
	}
	
	public String getVendorName(){
		return vendor_name;
	}
	
	public  void  setId(String id){
		this.id = id;
	}
	
	public  void  setVendorCode(String vendorCode){
		this.vendor_code = vendorCode;
	}
	
	public void   setVendorName(String vendorName){
		this.vendor_name = vendorName;
	}
}
