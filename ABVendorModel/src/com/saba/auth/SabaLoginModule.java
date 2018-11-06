/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
/*     */ package com.saba.auth;
/*     */ 
/*     */ import com.saba.auth.common.BaseLoginModule;
/*     */ import com.saba.encrypt.business.SabaCipherUtil;
/*     */ import com.saba.exception.PlatformMessage;
/*     */ import com.saba.exception.SabaDBException;
/*     */ import com.saba.exception.SabaException;
/*     */ import com.saba.exception.SabaFailedLoginException;
/*     */ import com.saba.hashing.SabaPasswordHash;
/*     */ import com.saba.properties.SabaProperties;
/*     */ import com.saba.security.KeyStoreManager;
/*     */ import com.saba.security.business.CertificateData;
/*     */ import com.saba.security.business.KeyStoreManagerImpl;
/*     */ import com.saba.sys.SabaSite;
/*     */ import com.saba.util.BaseDataBaseUtil;
/*     */ import com.saba.util.Debug;

import com.saba.properties.Security;
import gov.cms.cciio.common.auth.LoginLog;

/*     */ import java.io.PrintStream;
/*     */ import java.lang.reflect.Method;
/*     */ import java.security.PublicKey;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SabaLoginModule extends BaseLoginModule
/*     */ {
/*     */   private static final int kUserLoginMsgId = 1026;
/*     */   private static final int kSSOUserLoginMsgId = 1012;
/*     */   private static final String kGetParentsSql = "select  node_id domain_id ,  related_to related_to  from   tpt_dummy_flat_tree  where   node_id =  ? and   RELATION_TYPE =  'A' ";
/*     */ 
/*     */   private static void sabaLogin(Connection conn, String siteName, String userName, String hashedPassword)
/*     */     throws SabaException
/*     */   {
/*     */     //LoginLog.writeToErrorLog(SabaLoginModule.class +" sabaLogin, username " + userName + " password " + hashedPassword);
/*  72 */     String isAdmin = "false";
/*  73 */     if (userName.equalsIgnoreCase(Security.getAdminUsername())) {
/*  74 */       if (Security.disableAdminUserInUI())
/*  75 */         throw new SabaFailedLoginException();
/*  76 */       isAdmin = "true";
/*     */     }
/*     */ 
/*  84 */     Object[] bindArray = { userName, hashedPassword, isAdmin, "saba_db" };
/*     */     try {
/*  86 */       BaseDataBaseUtil.executeNoResultFgSql(conn, siteName, 1026, bindArray);
/*     */     }
/*     */     catch (SabaException e) {
/*  89 */       Throwable javaEx = e.getCause();
/*  90 */       SabaDBException newError = getSabaDBException(javaEx);
/*     */ 
/*  92 */       if (newError == null) {
/*  93 */         throw e;
/*     */       }
/*  95 */       throw newError;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static void sabaLogin(Connection conn, String siteName, String userName, String hashedPassword, String loginType)
/*     */     throws SabaException
/*     */   {
	// LoginLog.writeToErrorLog(SabaLoginModule.class +" sabaLogin, username " + userName + " password " + hashedPassword + ", loginType " + loginType);
/* 112 */     String isAdmin = "false";
/* 113 */     if (userName.equalsIgnoreCase(Security.getAdminUsername())) {
/* 114 */       isAdmin = "true";
/*     */     }
/* 116 */     Object[] bindArray = { userName, hashedPassword, isAdmin, loginType };
/*     */     try
/*     */     {
/* 119 */       BaseDataBaseUtil.executeNoResultFgSql(conn, siteName, 1026, bindArray);
/*     */     } catch (SabaException e) {
/* 121 */       Throwable javaEx = e.getCause();
/* 122 */       SabaDBException newError = getSabaDBException(javaEx);
/*     */ 
/* 124 */       if (newError == null) {
/* 125 */         throw e;
/*     */       }
/* 127 */       throw newError;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static SabaDBException getSabaDBException(Throwable t) {
/* 132 */     SabaDBException retEx = null;
/* 133 */     if (t instanceof SQLException) {
/* 134 */       retEx = new SabaDBException((SQLException)t);
/*     */     }
/* 136 */     return retEx;
/*     */   }
/*     */ 
/*     */   protected void verifyUser()
/*     */     throws SabaException
/*     */   {
/* 143 */     String siteName = getSiteName();
/* 144 */     String userName = getUserName();
/* 145 */     String password = getUserPassword();
/* 146 */     verifySabaUser(siteName, userName, password);
/*     */   }
/*     */ 
/*     */   public static void verifySabaUser(String siteName, String userName, String password)
/*     */     throws SabaException
/*     */   {
	 LoginLog.writeToErrorLog(SabaLoginModule.class +" verifySabaUser, username " + userName + " password " + password);
/* 171 */     if ((userName == null) || (siteName == null)) {
/* 172 */       if (Debug.isDebug()) {
/* 173 */         Debug.Trace("SabaLoginModule.verifySabaUser: ERROR!! user_name and site_name can not be NULL!!!");
/*     */       }
/* 175 */       System.out.println("SabaLoginModule.verifySabaUser ERROR!! user_name and site_name can not be NULL!!!");
/*     */ 
/* 178 */       throw new SabaFailedLoginException();
/*     */     }
/*     */ 
/* 182 */     if ((Security.isAnonymousUserEnabled()) && (userName != null) && (userName.equals(Security.getAnonymousUsername())))
/*     */     {
/* 184 */       if ((password == null) || (password.equals(Security.getAnonymousUserPassword())))
/*     */       {
/* 186 */         return;
/*     */       }
/* 188 */       throw new SabaFailedLoginException();
/*     */     }
/*     */ 
/* 191 */     if ((userName != null) && (userName.equals(Security.getAdminUsername()))) {
/* 192 */       SabaSecurityContext secContext = SabaSecurityContext.getContext();
/* 193 */       if (secContext != null) {
/* 194 */         String contextPassword = secContext.get();
/* 195 */         if (contextPassword.equals(password)) {
/* 196 */           return;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 201 */     if ((password == null) || (password.trim().equals(""))) {
/* 202 */       throw new SabaFailedLoginException();
/*     */     }
/*     */ 
/* 205 */     boolean isPasswordAsCertificate = false;
/*     */     try {
/* 207 */       Class sabaCertificateFactory = Class.forName("com.saba.security.business.SabaCertificateFactory");
/*     */ 
/* 209 */       Method passwordAsCertificateMethod = sabaCertificateFactory.getMethod("passwordAsCertificate", new Class[] { String.class });
/* 210 */       if (((Boolean)passwordAsCertificateMethod.invoke(null, new Object[] { password })).booleanValue())
/*     */       {
/* 212 */         isPasswordAsCertificate = true;
/*     */       }
/*     */     } catch (Exception e) {
/* 215 */       throw new SabaException(e);
/*     */     }
/*     */ 
/* 218 */     if (isPasswordAsCertificate)
/*     */     {
/* 220 */       verifySabaUserWithCertificate(siteName, userName, password);
/*     */     } else {
/* 222 */       Connection connection = getConnection(siteName);
/* 223 */       String salt = getSaltForUser(connection, userName, siteName);
/* 224 */       String hashedPassword = SabaPasswordHash.generateHash(((salt != null) && (!("".equals(salt)))) ? salt + password : password);
				LoginLog.writeToErrorLog(SabaLoginModule.class +" verifySabaUser " + hashedPassword);
/*     */       try
/*     */       {
/* 236 */         sabaLogin(connection, siteName, userName, hashedPassword);
/*     */       }
/*     */       catch (SabaException e)
/*     */       {
/* 242 */         if ((e.isMessageEquals(PlatformMessage.kDBInvalidUsernamePasswordException)) || (e.isMessageEquals(PlatformMessage.kTerminatedEmployeeException)) || (e.isMessageEquals(PlatformMessage.kExceededBadLoginAttemptsException)))
/*     */         {
/* 248 */           throw new SabaFailedLoginException();
/*     */         }
/* 250 */         if (Debug.isDebug());
/* 254 */         throw e;
/*     */       } finally {
/* 256 */         freeConnection(connection, siteName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void verifySabaUser(String siteName, String userName, String password, String loginType)
/*     */     throws SabaException
/*     */   {
	 
/* 266 */     if ((userName == null) || (siteName == null)) {
/* 267 */       if (Debug.isDebug())
/* 268 */         Debug.Trace("SabaLoginModule.verifySabaUser: ERROR!! user_name and site_name can not be NULL!!!");
/* 269 */       System.out.println("SabaLoginModule.verifySabaUser ERROR!! user_name and site_name can not be NULL!!!");
/*     */ 
/* 271 */       throw new SabaFailedLoginException();
/*     */     }
/* 273 */     if (loginType == null) {
/* 274 */       loginType = "saba_db";
/*     */     }
/*     */ 
/* 278 */     if ((Security.isAnonymousUserEnabled()) && (userName != null) && (userName.equals(Security.getAnonymousUsername())) && ((
/* 279 */       (password == null) || (password.equals(Security.getAnonymousUserPassword()))))) {
/* 280 */       return;
/*     */     }
/*     */ 
/* 285 */     if ((userName != null) && (userName.equals(Security.getAdminUsername()))) {
/* 286 */       SabaSecurityContext secContext = SabaSecurityContext.getContext();
/* 287 */       if (secContext != null) {
/* 288 */         String contextPassword = secContext.get();
/* 289 */         if (contextPassword.equals(password)) {
/* 290 */           return;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 295 */     if ((((password == null) || (password.trim().equals("")))) && (!(loginType.equals("saba_sso")))) {
/* 296 */       throw new SabaFailedLoginException();
/*     */     }
/*     */ 
/* 299 */     boolean isPasswordAsCertificate = false;
/*     */     try {
/* 301 */       Class sabaCertificateFactory = Class.forName("com.saba.security.business.SabaCertificateFactory");
/* 302 */       Method passwordAsCertificateMethod = sabaCertificateFactory.getMethod("passwordAsCertificate", new Class[] { String.class });
/* 303 */       if (((Boolean)passwordAsCertificateMethod.invoke(null, new Object[] { password })).booleanValue())
/* 304 */         isPasswordAsCertificate = true;
/*     */     }
/*     */     catch (Exception e) {
/* 307 */       throw new SabaException(e);
/*     */     }
/*     */ 
/* 310 */     if (isPasswordAsCertificate)
/*     */     {
/* 312 */       verifySabaUserWithCertificate(siteName, userName, password);
/*     */     } else {
/* 314 */       String hashedPassword = null;
/* 315 */       Connection connection = getConnection(siteName);
/* 316 */       String salt = getSaltForUser(connection, userName, siteName);
/* 317 */       if (password != null) {
/* 318 */         hashedPassword = SabaPasswordHash.generateHash(((salt != null) && (!("".equals(salt)))) ? salt + password : password);
/*     */       }
/*     */       try
/*     */       {
/* 322 */         sabaLogin(connection, siteName, userName, hashedPassword, loginType);
/*     */       }
/*     */       catch (SabaException e)
/*     */       {
/* 329 */         if (Debug.isDebug())
/* 330 */           Debug.trace("SabaLoginModule.verifyUser: Unexpected DB error", e);
/* 331 */         if ((e.isMessageEquals(PlatformMessage.kDBInvalidUsernamePasswordException)) || (e.isMessageEquals(PlatformMessage.kTerminatedEmployeeException)) || (e.isMessageEquals(PlatformMessage.kExceededBadLoginAttemptsException)));
/* 334 */         throw e;
/*     */       } finally {
/* 336 */         freeConnection(connection, siteName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private static void verifySabaUserWithCertificate(String siteName, String userName, String password)
/*     */     throws SabaFailedLoginException, SabaException
/*     */   {
/* 358 */     CertificateData cert = CertificateData.getFromPassword(password);
/*     */ 
/* 360 */     if (cert != null) {
/* 361 */       if (cert instanceof CertificateData) {
/* 362 */         CertificateData certificate = cert;
/* 363 */         if (certificate.getCertificateType().equals("SabaSSO"))
/*     */         {
/* 365 */           verifySabaSSOUser(siteName, userName, password);
/* 366 */           return;
/*     */         }
/*     */ 
/* 369 */         KeyStoreManager mgr = new KeyStoreManagerImpl(siteName);
/*     */         try
/*     */         {
/* 372 */           certificate.verify(mgr.getPublicKey(siteName));
/*     */         } catch (Exception e) {
/* 374 */           if (Debug.isDebug()) {
/* 375 */             Debug.trace(e);
/* 376 */             Debug.trace("SabaSpecialUserLoginModule: bad certificate " + e.toString());
/*     */           }
/*     */ 
/* 382 */           throw new SabaFailedLoginException();
/*     */         }
/*     */ 
/* 387 */         if ((userName.equals(certificate.getUsername())) && (siteName.equals(certificate.getSiteName())))
/*     */         {
/* 389 */           if (Debug.isDebug()) {
/* 390 */             Debug.trace("SabaSpecialUserLoginModule.login: authentication using certificate succeeded for " + userName);
/*     */           }
/*     */ 
/* 394 */           return; }
/* 395 */         if (Debug.isDebug()) {
/* 396 */           Debug.trace("SabaSpecialUserLoginModule.login: certificate user/site mismatch.  certUser=" + certificate.getUsername() + " certSite=" + certificate.getSiteName() + " userToAuth=" + userName + " siteToAuth=" + siteName);
/*     */         }
/*     */ 
/*     */       }
/* 408 */       else if ((password != null) && (password.startsWith("sabaSSOToken"))) {
/* 409 */         StringTokenizer tokens = new StringTokenizer(password, ":");
/* 410 */         if (tokens.countTokens() == 3) {
/* 411 */           tokens.nextToken();
/* 412 */           String encodeKey = tokens.nextToken();
/* 413 */           String encodedPassword = tokens.nextToken();
/*     */           try
/*     */           {
/* 416 */             password = SabaCipherUtil.decrypt(encodeKey, encodedPassword);
/*     */ 
/* 418 */             if (password.indexOf(userName) >= 0)
/* 419 */               return;
/*     */           }
/*     */           catch (Exception e) {
/* 422 */             if (Debug.isDebug()) {
/* 423 */               Debug.trace(e);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 429 */     throw new SabaFailedLoginException();
/*     */   }
/*     */ 
/*     */   private static void verifySabaSSOUser(String siteName, String userName, String password) throws SabaException
/*     */   {
	 
/* 434 */     CertificateData cert = CertificateData.getFromPassword(password);
/* 435 */     if ((isCertificateBasedSSOEnabled(siteName)) && (cert != null))
/*     */     {
/* 437 */       if (cert instanceof CertificateData) {
/* 438 */         CertificateData certificate = cert;
/* 439 */         if (!(certificate.getCertificateType().equals("SabaSSO")))
/*     */         {
/* 441 */           Debug.Trace("SabaSSOLoginModule.verifySabaUser: ERROR!! wrong certificate type!!!");
/*     */ 
/* 443 */           throw new SabaFailedLoginException();
/*     */         }
/*     */ 
/* 446 */         String worldlet = certificate.getLocale();
/*     */ 
/* 451 */         String hashedPassword = null;
/*     */ 
/* 454 */         PublicKey publicKey = SabaSSOCache.getPublicKey(siteName, worldlet);
/*     */ 
/* 456 */         if (publicKey == null) {
/* 457 */           Debug.Trace("SabaSSOLoginModule.verifySabaUser: ERROR!! failed to get the Publick Key!!!");
/*     */ 
/* 459 */           throw new SabaFailedLoginException();
/*     */         }
/*     */         try
/*     */         {
/* 463 */           Connection conn = getConnection(siteName);
/* 464 */           Vector results = getUserDetail(conn, userName, siteName);
/* 465 */           if (results == null) {
/* 466 */             if (Debug.isDebug()) {
/* 467 */               Debug.Trace(" SabaSSOLoginModule.verifyUser: " + userName + " is not a valid username\n");
/*     */             }
/* 469 */             throw new SabaFailedLoginException();
/*     */           }
/* 471 */           Vector row = (Vector)results.elementAt(0);
/* 472 */           String homeDomain = (String)row.elementAt(6);
/* 473 */           hashedPassword = (String)row.elementAt(2);
/* 474 */           if ((!(homeDomain.equals(worldlet))) && 
/* 475 */             (!(isDescendant(conn, homeDomain, worldlet)))) {
/* 476 */             if (Debug.isDebug()) {
/* 477 */               Debug.Trace(" SabaSSOLoginModule.verifyUser: " + homeDomain + " is not a descendant of " + worldlet + ".\n");
/*     */             }
/*     */ 
/* 481 */             throw new SabaFailedLoginException();
/*     */           }
/*     */ 
/* 484 */           freeConnection(conn, siteName);
/*     */         } catch (SabaException e) {
/* 486 */           if (Debug.isDebug()) {
/* 487 */             Debug.Trace(e, " SabaSSOLoginModule.verifyUser: Unexpected DB error\n");
/*     */           }
/*     */ 
/* 490 */           throw e;
/*     */         }
/*     */         try
/*     */         {
/* 494 */           certificate.verify(publicKey);
/*     */         } catch (Exception e) {
/* 496 */           if (Debug.isDebug())
/* 497 */             Debug.trace(e);
/* 498 */           if (Debug.isDebug()) {
/* 499 */             Debug.trace("SabaSSOLoginModule: bad certificate " + e.toString());
/*     */           }
/*     */ 
/* 503 */           throw new SabaFailedLoginException();
/*     */         }
/*     */ 
/* 507 */         if ((userName.equals(certificate.getUsername())) && (siteName.equals(certificate.getSiteName())))
/*     */         {
/* 509 */           if (Debug.isDebug()) {
/* 510 */             Debug.trace("SabaSSOLoginModule.login: authentication using certificate succeeded for " + userName);
/*     */           }
/*     */ 
/* 518 */           Connection conn = getConnection(siteName);
/*     */ 
/* 520 */           Debug.trace("SabaSSOLoginModule.login: Calling sabaLogin method ");
/*     */ 
/* 522 */           sabaLogin(conn, siteName, userName, hashedPassword);
/* 523 */           Debug.Trace("SabaSSOLoginModule.login: tpp_user_login entry succeeded");
/*     */ 
/* 525 */           freeConnection(conn, siteName);
/*     */ 
/* 531 */           return;
/*     */         }
/* 533 */         if (Debug.isDebug()) {
/* 534 */           Debug.trace("SabaSSOLoginModule.login: certificate user/site mismatch.  certUser=" + certificate.getUsername() + " certSite=" + certificate.getSiteName() + " userToAuth=" + userName + " siteToAuth=" + siteName);
/*     */         }
/*     */ 
/* 542 */         throw new SabaFailedLoginException();
/*     */       }
/*     */ 
/* 545 */       throw new SabaFailedLoginException();
/*     */     }
/*     */ 
/* 549 */     throw new SabaFailedLoginException();
/*     */   }
/*     */ 
/*     */   private static Vector getUserDetail(Connection conn, String userName, String siteName)
/*     */     throws SabaException
/*     */   {
/* 556 */     Object[] bindArray = { userName };
/* 557 */     Vector results = null;
/*     */     try {
/* 559 */       results = BaseDataBaseUtil.executeFgSql(conn, siteName, 1012, bindArray);
/*     */     }
/*     */     catch (SabaException e) {
/* 562 */       Throwable javaEx = e.getCause();
/* 563 */       SabaDBException newError = null;
/* 564 */       if (javaEx instanceof SQLException) {
/* 565 */         newError = new SabaDBException((SQLException)javaEx);
/*     */       }
/*     */ 
/* 568 */       if (newError == null) {
/* 569 */         throw e;
/*     */       }
/* 571 */       throw newError;
/*     */     }
/*     */ 
/* 574 */     if (results.size() == 1) {
/* 575 */       return results;
/*     */     }
/* 577 */     return null;
/*     */   }
/*     */ 
/*     */   private static boolean isDescendant(Connection conn, String domainId, String worldlet)
/*     */     throws SabaException
/*     */   {
/* 585 */     boolean result = false;
/*     */     try {
/* 587 */       PreparedStatement stmt = conn.prepareStatement("select  node_id domain_id ,  related_to related_to  from   tpt_dummy_flat_tree  where   node_id =  ? and   RELATION_TYPE =  'A' ");
/* 588 */       stmt.setString(1, domainId);
/* 589 */       ResultSet results = stmt.executeQuery();
/* 590 */       while (results.next()) {
/* 591 */         String parentId = results.getString(2);
/* 592 */         if (parentId.equals(worldlet)) {
/* 593 */           result = true;
/* 594 */           break;
/*     */         }
/*     */       }
/*     */     } catch (SQLException e1) {
/* 598 */       Debug.Trace("error encoutered when checking domain from database");
/* 599 */       Debug.printStackTrace(e1);
/* 600 */       throw new SabaFailedLoginException();
/*     */     }
/* 602 */     Debug.Trace("SabaSSOLoginModule.isDescendant: checking domain hierachy:" + result);
/*     */ 
/* 605 */     return result;
/*     */   }
/*     */ 
/*     */   private static boolean isCertificateBasedSSOEnabled(String siteName) {
/* 609 */     String isCertificateBasedSSOEnabled = null;
/*     */     try {
/* 611 */       SabaSite site = SabaSite.get(siteName);
/* 612 */       SabaProperties ldapProps = site.getSabaProperties("sabasso.properties");
/* 613 */       if (ldapProps != null)
/*     */       {
/* 615 */         isCertificateBasedSSOEnabled = ldapProps.getValue("enableCertificateBasedSSO", "false");
/*     */       }
/* 617 */       if ((isCertificateBasedSSOEnabled != null) && (isCertificateBasedSSOEnabled.equalsIgnoreCase("true")))
/* 618 */         return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 622 */       if (Debug.isDebug())
/* 623 */         Debug.trace("SabaAuthenticationAdapter: isLDAPAuthenticationEnabled : Could not fetch property useLDAPRegistry");
/*     */     }
/* 625 */     return false;
/*     */   }
/*     */ 
/*     */   private static String getSaltForUser(Connection conn, String userName, String siteName) throws SabaException
/*     */   {
/* 630 */     Vector results = getUserDetail(conn, userName, siteName);
/* 631 */     if (results == null) {
/* 632 */       if (Debug.isDebug()) {
/* 633 */         Debug.Trace(" SabaLoginModule.getSaltForUser: " + userName + "  results are null");
/*     */       }
/* 635 */       throw new SabaFailedLoginException();
/*     */     }
/* 637 */     Vector row = (Vector)results.elementAt(0);
/* 638 */     String id = (String)row.elementAt(0);
/* 639 */     String salt = (String)row.elementAt(9);
/*     */ 
/* 641 */     return salt;
/*     */   }
/*     */ }