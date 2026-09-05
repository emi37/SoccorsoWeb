package it.univaq.disim.webengineering.soccorsoweb.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public class TemplateManager {

    private static Configuration cfg;

    // inizializzazione lazy della configurazione di freemarker
    private static void init(ServletContext context) {
        if (cfg == null) {
            // imposto la versione (usa quella del jar che metterai in WEB-INF/lib)
            cfg = new Configuration(Configuration.VERSION_2_3_32);
            
            // dico a freemarker di pescare i template dalla cartella /WEB-INF/templates
            cfg.setServletContextForTemplateLoading(context, "/WEB-INF/templates");
            
            // standard encoding
            cfg.setDefaultEncoding("UTF-8");
            
            // gestione errori: in dev sputa l'errore nell'html, in prod andrebbe cambiato in RETHROW_HANDLER
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
        }
    }

    // metodo principale da chiamare nelle Servlet
    public static void process(String templateName, Map<String, Object> data, HttpServletResponse response, ServletContext context) {
        // se è la prima chiamata, inizializzo il motore
        if (cfg == null) {
            init(context);
        }

        response.setContentType("text/html;charset=UTF-8");
        
        try {
            // pesco il file .ftl
            Template template = cfg.getTemplate(templateName);
            
            // unisco il data model al template e lo sparo direttamente nell'output stream della response
            template.process(data, response.getWriter());
            
        } catch (Exception e) {
            System.err.println("Errore grave durante il parsing del template: " + templateName);
            e.printStackTrace();
        }
    }
}