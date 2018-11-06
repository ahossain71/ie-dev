package gov.cms.cciio.common.exception;

import java.io.Serializable;

/**
 * Used for raise Course Iteration related exceptions
 * @author Feilung Wong
 */
public class CourseIterationException extends MLMSException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1592769270453718042L;

    public CourseIterationException(int errorCode) {
        super(errorCode);
    }

    public CourseIterationException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public CourseIterationException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}
