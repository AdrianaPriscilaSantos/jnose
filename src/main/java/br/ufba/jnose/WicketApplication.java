package br.ufba.jnose;

import br.ufba.jnose.pages.HomePage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication {

    public static boolean COBERTURA_ON = false;

    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    @Override
    public void init() {
        super.init();
        this.getMarkupSettings().setStripWicketTags(true);
        this.getDebugSettings().setAjaxDebugModeEnabled(false);
    }
}
