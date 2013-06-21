import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import models.*;

import com.avaje.ebean.Ebean;

import controllers.Leden;
import controllers.Afschriften;

import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
        InitialData.insert(app);
    }
    
    static class InitialData {
        
        public static void insert(Application app) {
            if(Ebean.find(Lid.class).findRowCount() == 0) {
                System.out.println("Seeding members");
                try {
                    Leden.perform_csvimport(new File("data/leden.csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Seeding bankafschriften");
                try {
                    Afschriften.perform_csvimport(new File("data/afschriften.csv"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                System.out.println("Seeding contributiefacturen");
                int aantalFacturen = 0;
                for(int jaar=2009; jaar<=2013; jaar++) {
                    for( Lid lid: Lid.find.all()) {
                        Factuur factuur = new FactuurContributie(new Date(), lid, 20, jaar);
                        factuur.save();
                        aantalFacturen++;
                    }
                }
                System.out.println("Generated "+aantalFacturen+" facturen");

                Logger.info("Seeding payments");
                try {
                    Afschriften.perform_betaling_csvimport(new File("data/betalingen.csv"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
       /* 
                System.out.println("Creating fake bank accounts");
                int count=1;
                for(Lid lid: Lid.find.all()) {
                  lid.addRekening(new Integer(count*5462).toString());
                  lid.update(lid.id);
                  count++;
                  if(count>10) break;
                }

                System.out.println("Filling in known bank accounts");
                Lid jp = Lid.find.byId((long) 5);
                jp.addRekening("551242663");
                jp.update(jp.id);
                Lid aad = Lid.find.where().ilike("personen.name","%de Vogel%").findUnique();
                if (aad!=null) {
                    aad.addRekening("8061902");
                    aad.update(aad.id);
                }
                */
                
            }
        }
        
    }
    
}


