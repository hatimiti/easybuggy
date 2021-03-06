package org.t246osslab.easybuggy.vulnerabilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.t246osslab.easybuggy.utils.Closer;
import org.t246osslab.easybuggy.utils.HTTPResponseCreator;
import org.t246osslab.easybuggy.utils.MessageUtils;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/ognleijc" })
public class OGNLExpressionInjectionServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(OGNLExpressionInjectionServlet.class);

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        PrintWriter writer = null;
        try {
            Object value = null;
            boolean isValid = true;
            Locale locale = req.getLocale();
            OgnlContext ctx = new OgnlContext();
            String expression = req.getParameter("expression");
            if (expression == null || expression.equals("")) {
                isValid = false;
            } else {
                try {
                    Object expr = Ognl.parseExpression(expression.replaceAll("Math.", "@Math@"));
                    value = Ognl.getValue(expr, ctx);
                } catch (OgnlException e) {
                    isValid = false;
                    log.debug("Exception occurs: ", e);
                }
            }

            StringBuilder bodyHtml = new StringBuilder();
            bodyHtml.append("<form action=\"ognleijc\" method=\"post\">");
            bodyHtml.append(MessageUtils.getMsg("msg.enter.math.expression", locale));
            bodyHtml.append("<br>");
            bodyHtml.append("<br>");
            if (isValid) {
                bodyHtml.append("<input type=\"text\" name=\"expression\" size=\"80\" maxlength=\"300\" value=\""
                        + ESAPI.encoder().encodeForHTML(expression) + "\">");
            } else {
                bodyHtml.append("<input type=\"text\" name=\"expression\" size=\"80\" maxlength=\"300\">");
            }
            bodyHtml.append(" = ");
            if (isValid && value != null && NumberUtils.isNumber(value.toString())) {
                bodyHtml.append(value);
            }
            bodyHtml.append("<br>");
            bodyHtml.append("<br>");
            bodyHtml.append("<input type=\"submit\" value=\"" + MessageUtils.getMsg("label.calculate", locale) + "\">");
            bodyHtml.append("<br>");
            bodyHtml.append("<br>");
            bodyHtml.append(MessageUtils.getMsg("msg.note.enter.runtime.exec", locale));
            bodyHtml.append("</form>");
            HTTPResponseCreator.createSimpleResponse(res,
                    MessageUtils.getMsg("title.ognl.expression.injection.page", locale), bodyHtml.toString());

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        } finally {
            Closer.close(writer);
        }
    }
}
