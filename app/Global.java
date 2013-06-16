import java.util.Date;

import models.Lid;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
        InitialData.insert(app);
    }
    
    static class InitialData {
        
        public static void insert(Application app) {
//            if(Ebean.find(Lid.class).findRowCount() == 0) {
//                Lid.create(new Lid("Jan-Pascal van Best", "Gerrit de Blankenlaan 6", new Date()));
//            }
        }
        
    }
    
}


