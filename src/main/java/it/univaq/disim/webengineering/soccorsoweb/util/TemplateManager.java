package it.univaq.disim.webengineering.soccorsoweb.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public class TemplateManager {

    private static Configuration cfg;

    // inizializzazione della configurazione di freemarker
    private static void init(ServletContext context) {
        if (cfg == null) {
            // imposto la versione (usa quella del jar in WEB-INF/lib)
            cfg = new Configuration(Configuration.VERSION_2_3_32);
            
            try {
                // dico a freemarker che i template stanno nella cartella /WEB-INF/templates
                java.io.File directoryTemplate = new java.io.File(context.getRealPath("/WEB-INF/templates"));
                cfg.setDirectoryForTemplateLoading(directoryTemplate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // standard serve sempre di dafault
            cfg.setDefaultEncoding("UTF-8");
            
            // gestione errori
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
        }
    }

    // metodo principale da chiamare nelle Servlet
    public static void process(String templateName, Map<String, Object> data, HttpServletResponse response, ServletContext context) {
        // se è la prima chiamata, inizializzo tutto 
        if (cfg == null) {
            init(context);
        }

        response.setContentType("text/html;charset=UTF-8");
        
        try {
            // prende il file .ftl
            Template template = cfg.getTemplate(templateName);
            
            // unisco il data model al template e stampo nell'output 
            template.process(data, response.getWriter());
            
        } catch (Exception e) {
            System.err.println("Errore: " + templateName);
            e.printStackTrace();
        }
    }
}