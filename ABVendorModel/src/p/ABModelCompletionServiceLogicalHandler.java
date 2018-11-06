package p;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

public class ABModelCompletionServiceLogicalHandler implements LogicalHandler<LogicalMessageContext> 
{


   public boolean handleMessage(LogicalMessageContext context)
   {
      // TODO
      return true;
   }

   public boolean handleFault(LogicalMessageContext context)
   {
      // TODO 
      return true;
   }

   public void close(MessageContext context)
   {
      // TODO
   }

}