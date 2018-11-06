package gov.cms.cciio.common.auth;

import gov.cms.cciio.common.util.CommonUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Modified from java.util.logging.SimpleFormatter
 *
 */
public class LoginLogFormatter extends Formatter  {

    public LoginLogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format("{0, date} {0, time} ", //$NON-NLS-1$
                new Object[] { new Date(r.getMillis()) }));
        sb.append(r.getSourceClassName()).append(" "); //$NON-NLS-1$
        sb.append(r.getLevel().getName()).append(": "); //$NON-NLS-1$
        sb.append(formatMessage(r)).append(CommonUtil.EOL);
        if (null != r.getThrown()) {
            sb.append("Throwable occurred: "); //$NON-NLS-1$
            Throwable t = r.getThrown();
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                sb.append(sw.toString());
            } finally {
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        return sb.toString();
    }
}
