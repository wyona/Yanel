/*
 * Copyright 2009 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yanel.impl.resources.forgotpw;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.management.timer.Timer;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.validator.EmailValidator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wyona.commons.xml.XMLHelper;
import org.wyona.security.core.api.User;
import org.wyona.yanel.impl.resources.BasicXMLResource;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;


/**
 * This resource is responsible for managing the forgot password functionality.
 * The following constant control the flow with the UI.
 * {@value #SUBMITFORGOTPASSWORD}  is passed when the user clicks on the first screen to submit email.
 *
 * {@value #SUBMITNEWPW}  is passed when the user enter the new password and submits the form.
 *
 * If the query string has pwresetid then we know that the user clicked on the link sent via email.
 */
public class ForgotPassword extends BasicXMLResource {

    private static Logger log = Logger.getLogger(ForgotPassword.class);
    private long totalValidHrs;

    private static final String PW_RESET_ID = "pwresetid";
    private static final String SUBMITFORGOTPASSWORD = "submitForgotPW";
    private static final String SUBMITNEWPW = "submitNewPW";
    private static final String NAMESPACE = "http://www.wyona.org/yanel/1.0";

    private static final String SMTP_HOST_PROPERTY_NAME = "smtpHost";
    private static final String SMTP_PORT_PROPERTY_NAME = "smtpPort";

    private static final String HOURS_VALID_PROPERTY_NAME = "num-hrs-valid";
    private static final long  DEFAULT_TOTAL_VALID_HRS = 24L;

    private static final String UUID_TAG = "guid";
    private static final String UUID_PARAM = "guid";

    /**
     * This is the main method that handles all view request. The first time the request
     * is made to enter the data.
     */
    @Override
    protected InputStream getContentXML(String viewId) throws Exception {
        HttpServletRequest request = getEnvironment().getRequest();

        try {
            String hrsValid = getResourceConfigProperty(HOURS_VALID_PROPERTY_NAME);
            if(hrsValid != null && !hrsValid.equals("")) {
                totalValidHrs = Long.parseLong(hrsValid);
            } else {
                totalValidHrs = DEFAULT_TOTAL_VALID_HRS;
            }
        } catch(Exception ex) {
            log.error("num-hrs-valid flag not properly set: " + ex, ex);
            totalValidHrs = DEFAULT_TOTAL_VALID_HRS;
        }
        Document adoc = XMLHelper.createDocument(NAMESPACE, "yanel-forgotpw");
        processUserAction(request, adoc);
        DOMSource source = new DOMSource(adoc);
        StringWriter xmlAsWriter = new StringWriter();
        StreamResult result = new StreamResult(xmlAsWriter);
        TransformerFactory.newInstance().newTransformer().transform(source, result);
        // write changes
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));
        return inputStream;
    }

    /**
     * @param adoc XML DOM Document which will be used to generate response
     */
    private void processUserAction(HttpServletRequest request, Document adoc) throws Exception {
        String action = determineAction(request);
        log.debug("action performed: " + action);

        Element rootElement = adoc.getDocumentElement();
        String resetPasswordRequestUUID = getForgotPasswordRequestUUID(request);
        if (action.equals(SUBMITFORGOTPASSWORD)) {
            String email = request.getParameter("email");
            Element messageElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "show-message"));
            Element cpeElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "change-password-email"));
            cpeElement.setAttribute("submitted-email", email);
            try {
                String uuid = generateForgotPasswordRequest(email);
                if (uuid != null) {
                    messageElement.setTextContent("Password change request was successful. Please check your email for further instructions on how to complete your request.");
                    cpeElement.setAttribute("status", "200");
                    if (getResourceConfigProperty("include-change-password-link") != null && getResourceConfigProperty("include-change-password-link").equals("true")) {
                        log.warn("Change password link will be part of response! Because of security reasons this should only be done for development or testing environments.");
                        cpeElement.setAttribute("change-password-link", getURL(uuid));
                        cpeElement.setAttribute("uuid", uuid);
                    }
                } else {
                    log.warn("No forgot password request UUID!");
                }
            } catch(Exception e) {
                log.warn(e.getMessage());
                messageElement.setTextContent(e.getMessage());
                cpeElement.setAttribute("status", "400");
            }

        } else if (resetPasswordRequestUUID != null && !resetPasswordRequestUUID.equals("") && !action.equals(SUBMITNEWPW)){
            log.debug("Reset password request UUID: " + resetPasswordRequestUUID);
            if(!existsRequestUUID(resetPasswordRequestUUID)) {
                String errorMsg ="Unable to find forgot password request with request UUID '" + resetPasswordRequestUUID + "'. Maybe request UUID has a typo or request has expired or got deleted by administrator. Please try again.";
                log.warn(errorMsg);
                Element statusElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "show-message"));
                statusElement.setTextContent(errorMsg);
            } else {
                Element requestpwElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "requestnewpw"));
                Element guidElement = (Element) requestpwElement.appendChild(adoc.createElementNS(NAMESPACE, UUID_TAG));
                guidElement.setTextContent(resetPasswordRequestUUID);
            }
        } else if(action.equals(SUBMITNEWPW)) {
            Element messageElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "show-message")); // INFO: We need to keep this element for backwards compatibility reasons!
            Element pwUpdateElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "password-update")); // INFO: We have introduced this element, because the "show-message" element is ambiguous, because it is also used while generating a password change request
            pwUpdateElement.setAttribute(UUID_TAG, request.getParameter(UUID_PARAM));

            try {
                updatePassword(request.getParameter("newPassword"), request.getParameter("newPasswordConfirmation"), request.getParameter(UUID_PARAM));
                messageElement.setTextContent("Password has been successfully reset. Please login with your new password.");
                pwUpdateElement.setAttribute("status", "200");
            } catch(Exception e) {
                log.warn(e.getMessage());
                messageElement.setTextContent(e.getMessage());
                pwUpdateElement.setAttribute("status", "400");
            }
        } else {
            log.debug("default handler");
            String smtpEmailServer = getResourceConfigProperty(SMTP_HOST_PROPERTY_NAME);
            String smtpEmailServerPort = getResourceConfigProperty(SMTP_PORT_PROPERTY_NAME);
            if ((smtpEmailServer != null && smtpEmailServerPort != null) || (getYanel().getSMTPHost() != null && getYanel().getSMTPPort() >= 0)) {
                String from = getResourceConfigProperty("smtpFrom");
                if (from != null) {
                    Element requestEmailElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "requestemail")); // INFO: A phone application might have cached the email address and hence wants to auto-complete the form...
                    String emailAddress = getEnvironment().getRequest().getParameter("email");
                    if (emailAddress != null) {
                        requestEmailElement.appendChild(adoc.createTextNode(emailAddress));
                    }
                } else {
                    Element exceptionElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "exception"));
                    String resConfigFilename = "global-resource-configs/user-forgot-pw_yanel-rc.xml";
                    if (getConfiguration().getNode() != null) {
                        resConfigFilename = getConfiguration().getNode().getPath(); 
                    }
                    exceptionElement.setTextContent("The FROM address has not been configured yet. Please make sure to configure the FROM address inside the resource configuration '" + resConfigFilename + "' (either globally or per realm)");
                }
            } else {
                Element exceptionElement = (Element) rootElement.appendChild(adoc.createElementNS(NAMESPACE, "exception"));
                String resConfigFilename = "global-resource-configs/user-forgot-pw_yanel-rc.xml";
                if (getConfiguration().getNode() != null) {
                    resConfigFilename = getConfiguration().getNode().getPath(); 
                }
                exceptionElement.setTextContent("SMTP host/port has not been configured yet. Please make sure to configure the various smtp properties at: " + resConfigFilename + " (Or within WEB-INF/classes/yanel.xml)");
            }
        }
    }

    /**
     * Check whether password forgot request UUID still exists (For example it might has expired)
     * @param uuid Password forgot request UUID
     */
    protected boolean existsRequestUUID(String uuid) throws Exception {
        User usr = getUserForRequest(uuid, totalValidHrs);
        if(usr != null) {
            return true;
        } else {
            log.warn("Password forgot request with UUID '" + uuid + "' does not exist (maybe has expired)");
            return false;
        }
    }

    /**
     * Get user for a specific request ID
     * @param requestID Request ID
     */
    private User getUserForRequest(String requestID, long duration_hour) throws Exception {
        log.debug("Find user for request with ID: " + requestID);
        if (getRealm().getRepository().existsNode(getPersistentRequestPath(requestID))) {
            Node requestNode = getRealm().getRepository().getNode(getPersistentRequestPath(requestID));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestNode.getInputStream());
            Element rootElem = doc.getDocumentElement();
            String userid = rootElem.getAttribute("id");

            Element requestTimeElem = org.wyona.commons.xml.XMLHelper.getChildElements(rootElem, "request-time", null)[0];
            long savedDateTime = new Long(requestTimeElem.getAttribute("millis")).longValue();
            log.warn("Request time: " + savedDateTime);
            if(isExpired(savedDateTime, duration_hour)) {
                log.warn("Request is expired");
                return null;
            }

            return realm.getIdentityManager().getUserManager().getUser(userid);
        } else {
            log.warn("No such request ID: " + requestID);
            return null;
        }
    }

    /**
     * Check if request is expired
     */
    private boolean isExpired(long starDT, long duration_hour) throws Exception {
        long currentDT = new Date().getTime();
        long expireTime= starDT + duration_hour * Timer.ONE_HOUR;

        return (expireTime < currentDT);
    }

    private String getTextValue(Element ele, String tagName) throws Exception {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }


    /* Determine the requested view: defaultView, submitProfile,
    * submitPassword,submitGroup, submitDelete
    *
    * @param request
    * @return name of the desired view
    */
   private String determineAction(HttpServletRequest request) throws Exception {
       boolean submit = false;
       String action = "defaultView";

       Enumeration<?> enumeration = request.getParameterNames();
       while (enumeration.hasMoreElements() && !submit) {
           action = enumeration.nextElement().toString();
           if (action.startsWith("submit")) {
               submit = true;
           }
       }
       return action;
   }

    /**
     * Generate password change request
     * @param email E-Mail address of user
     * @return request UUID if user with specific email address exists and email was sent, return null or throw an exception otherwise
     */
    private String generateForgotPasswordRequest(String email) throws Exception {
        String exceptionMsg;
        if (email == null || ("").equals(email)) {
            exceptionMsg = "E-mail address is empty.";
        } else if (! EmailValidator.getInstance().isValid(email)) {
            exceptionMsg = email + " is not a valid E-mail address.";
        } else {
            User user = getUser(email);
            if (user == null) {
                exceptionMsg = "Unable to find user based on the " + email + " E-mail address.";
            } else {
                String uuid = UUID.randomUUID().toString();
                uuid = sendEmail(uuid, user.getEmail());
                if (uuid != null) {
                    ResetPWExpire pwexp = new ResetPWExpire(user.getID(), new Date().getTime(), uuid, user.getEmail());
                    writeXMLOutput(getPersistentRequestPath(uuid), generateXML(pwexp));
                    return uuid;
                } else {
                    exceptionMsg = "No forgot password request UUID was generated (please check log file to check what went wrong)";
                    log.warn(exceptionMsg);
                    throw new Exception(exceptionMsg);
                }
            }
        }
        log.warn(exceptionMsg);
        throw new Exception(exceptionMsg);
    }

    /**
     * Get user which is associated with an email address
     * @param email Email address of user
     */
    protected User getUser(String email) throws Exception {
        log.warn("TODO: Checking every user by her/his email does not scale!");
        User[] userList = realm.getIdentityManager().getUserManager().getUsers(true);
        for(int i=0; i< userList.length; i++) {
            if (userList[i].getEmail().equals(email)) {
                return userList[i];
            }
        }
        log.warn("No user found with email addres: " + email);
        return null;
    }

    /**
     * Generate XML containing request information which will be saved persistently
     */
    private String generateXML(ResetPWExpire resetObj) throws Exception {
        org.w3c.dom.Document adoc = org.wyona.commons.xml.XMLHelper.createDocument(NAMESPACE, "user");
        Element userElement = adoc.getDocumentElement();
        userElement.setAttribute("id", resetObj.getUserId());

        Element emailElement = (Element) userElement.appendChild(adoc.createElement("email"));
        emailElement.setTextContent(resetObj.getEmail());

        Element startTimeElement = (Element) userElement.appendChild(adoc.createElement("request-time"));
        startTimeElement.setAttribute("millis", Long.toString(resetObj.getDateTime()));
        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        startTimeElement.setTextContent(dateFormat.format(resetObj.getDateTime()));

        Element guidElement = (Element) userElement.appendChild(adoc.createElement(UUID_TAG));
        guidElement.setTextContent(resetObj.getGuid());


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer t = factory.newTransformer(); // identity transform
        DOMSource source = new DOMSource(adoc);
        StreamResult result = new StreamResult(baos);
        t.transform(source, result);

        return baos.toString();
    }

    /**
     * Write reset password request into Yarep node
     * @param path Yarep node path
     * @param content XML content
     */
    private void writeXMLOutput(String path, String content) throws Exception {
        Node fileToStore = null;
        if (getRealm().getRepository().existsNode(path)) {
            fileToStore = getRealm().getRepository().getNode(path);
        } else {
            fileToStore = getRealm().getRepository().getRootNode().addNode(path, NodeType.RESOURCE);
        }
        InputStream in = new ByteArrayInputStream(content.getBytes());
        OutputStream out = fileToStore.getOutputStream();
        byte buffer[] = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
    }

    /**
     * Validate and change new user password.
     * @param newPassword New password
     * @param confirmedPassword New password confirmed
     * @param uuid UUID of forgot password request
     * @return "success" if validation and updating new user password was successful, otherwise return exception message
     */
    protected void updatePassword(String newPassword, String confirmedPassword, String uuid) throws Exception {
        if (newPassword == null || newPassword.length() == 0) {
            String exceptionMsg = "No password was submitted!";
            log.warn(exceptionMsg);
            throw new Exception(exceptionMsg);
        }

        // INFO: The confirmed password is optional, but if provided, then it will be compared
        if (confirmedPassword != null && !newPassword.equals(confirmedPassword)) {
            String exceptionMsg = "Password and confirmed password do not match!";
            log.warn(exceptionMsg);
            throw new Exception(exceptionMsg);
        }

        User user = getUserForRequest(uuid, totalValidHrs);
        if(user !=null) {
            user.setPassword(newPassword);
            user.save();
            getRealm().getRepository().delete(new org.wyona.yarep.core.Path(getPersistentRequestPath(uuid))); // DEPRECATED
            //TODO: YarepUtil.deleteNode(getRealm().getRepository(), getPersistentRequestPath(uuid));
        } else {
            throw new Exception("Unable to find user for password reset UUID: " + uuid);
        }
    }

    /**
     * @see
     */
    @Override
    public boolean exists() throws Exception {
        return true;
    }

    /**
     * Get forgot password URL which will be sent via E-Mail (also see YanelServlet#getRequestURLQS(HttpServletRequest, String, boolean))
     * @param uuid UUID of forgot password request
     */
    private String getURL(String uuid) throws Exception {
        //https://192.168.1.69:8443/yanel" + request.getServletPath().toString()
        URL url = new URL(request.getRequestURL().toString());
        org.wyona.yanel.core.map.Realm realm = getRealm();
        if (realm.isProxySet()) {
            // TODO: Finish proxy settings replacement

            String proxyHostName = realm.getProxyHostName();
            log.debug("Proxy host name: " + proxyHostName);
            if (proxyHostName != null) {
                url = new URL(url.getProtocol(), proxyHostName, url.getPort(), url.getFile());
            }

            int proxyPort = realm.getProxyPort();
            if (proxyPort >= 0) {
                url = new URL(url.getProtocol(), url.getHost(), proxyPort, url.getFile());
            } else {
                url = new URL(url.getProtocol(), url.getHost(), url.getDefaultPort(), url.getFile());
            }

            String proxyPrefix = realm.getProxyPrefix();
            if (proxyPrefix != null) {
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile().substring(proxyPrefix.length()));
            }
        } else {
            log.warn("No proxy set.");
        }

        return url.toString() + "?" + PW_RESET_ID + "=" + uuid;
    }

    /**
     * Get base path (collection path) where reset password requests will be saved permanently
     */
    private String getResetPasswordRequestsBasePath() throws Exception {
        String configuredBasePath = getResourceConfigProperty("change-password-requests-path");
        String basePath;
        if (configuredBasePath != null) {
            if (!configuredBasePath.startsWith("/")) {
                basePath = "/" + configuredBasePath;
            } else {
                basePath = configuredBasePath;
            }
        } else {
            String DEFAULT_BASE_PATH = "/reset-password-requests";
            log.warn("No base path configured. Will use default value: " + DEFAULT_BASE_PATH);
            basePath = DEFAULT_BASE_PATH;
        }
        return basePath;
    }

    /**
     * Get forgot password request UUID. Overwrite this method in case you have a different query string parameter for the UUID
     * @param request HTTP request containing the UUID
     */
    protected String getForgotPasswordRequestUUID(HttpServletRequest request) {
        return request.getParameter(PW_RESET_ID);
    }

    /**
     * Send email to user requesting to reset the password
     * @param guid UUID which is part of the change password link
     * @return UUID
     */
    protected String sendEmail(String guid, String emailAddress) throws Exception {
        String emailSubject = "Reset password request needs your confirmation";

        String emailBody = generateEmailBody(guid);

        String from = getResourceConfigProperty("smtpFrom");
        String to =  emailAddress;

        String emailServer = getResourceConfigProperty(SMTP_HOST_PROPERTY_NAME);
        if (emailServer != null) {
            int port = Integer.parseInt(getResourceConfigProperty(SMTP_PORT_PROPERTY_NAME));
            org.wyona.yanel.core.util.MailUtil.send(emailServer, port, from, to, emailSubject, emailBody);
        } else {
            org.wyona.yanel.core.util.MailUtil.send(from, to, emailSubject, emailBody);
        }
        return guid;
    }

    /**
     *
     */
    private String getPersistentRequestPath(String guid) throws Exception {
        return getResetPasswordRequestsBasePath() + "/" + guid + ".xml";
    }

    /**
     * Generate email body
     */
    private String generateEmailBody(String uuid) throws Exception {
        String emailBody = "Please go to the following URL to reset password: <" + getURL(uuid) + ">.";
        String hrsValid = getResourceConfigProperty(HOURS_VALID_PROPERTY_NAME);
        if (hrsValid == null) {
            hrsValid = "" + DEFAULT_TOTAL_VALID_HRS;
        }
        emailBody = emailBody + "\n\nNOTE: This link is only available during the next " + hrsValid + " hours!";
        if (log.isDebugEnabled()) log.debug(emailBody);
        return emailBody;
    }
}
