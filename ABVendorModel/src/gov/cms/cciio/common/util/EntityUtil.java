package gov.cms.cciio.common.util;

/**
 * A common class that does the entity lookups. 
 * @author Sudha Uppaluri
 * @since 05-09-06
 * @version 1.0
 */
/*
 * 2009-02-06   Feilung Wong    Defect 282: Removed entity related functions from the util package.
 *                                          Moved Entity lookup related helper functions from ALMSUtil to EntityUtil class
 *
 * 2010-07-21   Feilung Wong    Moved from Saba 5.3 code: mil.army.dls.interfaces.delegate.EntityLookUp.java
 *                              Moved methods from mil.army.dls.interfaces.entity.SequenceGenerator.java
 * 2010-07-29   Feilung Wong    Removed the use of Hashtable, updated JNDI references
 *                              Since only the AudienceType ID is read, and there is no creation of the bean,
 *                              the use of ForeignDiscAudienceTypeMap Entity bean is dropped in 5.5
 *                              The relevant helper methods now makes a simple DB query
 * 2011-05-09   Feilung Wong    Using new DBUtil methods to better log and release DB resources
 *                              atrrsAudienceIdList and allAudienceIdList are the only methods used in ALMS 3.0
 *                              They can be moved to CommonUtil and this class can be removed in the next release
 * 2011-10-03   Feilung Wong    QC 854: Use Java PreparedStatement bind variables as much as possible to improve caching/performance
 * 2011-10-18   Feilung Wong    Removed unused method
 */
// This class has been deprecated and removed from builds
public class EntityUtil {
}

